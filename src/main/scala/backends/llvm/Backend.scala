package backends.llvm

import instant.Absyn.*

import java.io.{File, FileWriter}

case class LLVMFileWriter(fileWriter: FileWriter, indent: Int) {
	def writeLine(line: String): Unit = {
		fileWriter.write("\t\t")
		fileWriter.write(line)
		fileWriter.write("\n")
	}

	def close(): Unit = fileWriter.close()
}

object Backend extends backends.Backend {
	override def compile(program: Program, fileName: String): Unit = {
		val fileWriter: FileWriter = new FileWriter(new File(s"$fileName.ll"))
		val mnemonicFileWriter = LLVMFileWriter(fileWriter, 2)
		try {
			fileWriter.write(
				s"""declare i32 @printf(i8*, ...)
					 |
					 |@intFormat = internal constant [4 x i8]
					 |  c"%d\\0A\\00"
					 |
					 |define i32 @main() {
					 |\tentry:
					 |\t\t%intFormatPtr = bitcast [4 x i8]* @intFormat to i8*
					 |""".stripMargin
			)
			compile(program, Context.empty, mnemonicFileWriter)
			fileWriter.write(
				s"""\t\tret i32 0
					 |}\n
					 |""".stripMargin
			)
		} finally {
			mnemonicFileWriter.close()
		}

		scala.sys.process.Process(s"llvm-as $fileName.ll").!
	}

	private def compile(program: Program, context: Context, fileWriter: LLVMFileWriter): Context = program match {
		case prog: Prog =>
			val statements = prog.liststmt_.toArray(Array[Stmt]()).toSeq
			statements.foldLeft(context){ (context, statement) => compile(statement, context, fileWriter) }
	}

	private def compile(statement: Stmt, context: Context, fileWriter: LLVMFileWriter): Context = statement match {
		case ass: SAss =>
			val contextAfterExpression = compile(ass.exp_, context, fileWriter)
			contextAfterExpression.pushVariable(ass.ident_, contextAfterExpression.peekOperand).popOperand
		case exp: SExp =>
			val contextAfterExpression = compile(exp.exp_, context, fileWriter)
			fileWriter.writeLine(s"call i32 (i8*, ...) @printf(i8* %intFormatPtr, i32 ${contextAfterExpression.peekOperand})")
			contextAfterExpression.popOperand
	}

	private type ExpOpType = ExpAdd | ExpSub | ExpMul | ExpDiv
	private case class ExpOp(exp1: Exp, exp2: Exp, op: String)

	private def translateOperation(operation: ExpOpType): ExpOp = operation match {
		case op: ExpAdd => ExpOp(op.exp_1, op.exp_2, "add")
		case op: ExpSub => ExpOp(op.exp_1, op.exp_2, "sub")
		case op: ExpMul => ExpOp(op.exp_1, op.exp_2, "mul")
		case op: ExpDiv => ExpOp(op.exp_1, op.exp_2, "sdiv")
	}

	private def compile(expression: Exp, context: Context, fileWriter: LLVMFileWriter): Context = expression match {
		case lit: ExpLit => context.pushOperand(Constant(lit.integer_))
		case vrr: ExpVar => context.pushOperand(context.variables(vrr.ident_))
		case op: ExpOpType =>
			val expOp = translateOperation(op)
			val context1 = compile(expOp.exp1, context, fileWriter)
			val context2 = compile(expOp.exp2, context1, fileWriter)
			val operand2 = context2.peekOperand
			val operand1 = context2.popOperand.peekOperand
			val resultValueSource = context2.nextRegister
			fileWriter.writeLine(s"$resultValueSource = ${expOp.op} i32 $operand1, $operand2")
			context2.popOperand.popOperand.pushOperand(resultValueSource)
	}
}

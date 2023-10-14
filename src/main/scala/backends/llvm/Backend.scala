package backends.llvm

import java.io.{File, FileWriter}

object Backend extends backends.Backend {
	private case class LLVMFileWriter(file: File) extends FileWriter(file) {
		def writeLine(line: String): Unit = write(s"\t\t$line\n")
	}

	override def compile(program: instant.Absyn.Program, fileName: String): Unit = {
		val fileWriter: LLVMFileWriter = LLVMFileWriter(new File(s"$fileName.ll"))
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
			compile(program, Context.empty, fileWriter)
			fileWriter.write(
				s"""\t\tret i32 0
					 |}\n
					 |""".stripMargin
			)
		} finally {
			fileWriter.close()
		}

		scala.sys.process.Process(s"llvm-as $fileName.ll").!
	}

	private def compile(program: instant.Absyn.Program, context: Context, fileWriter: LLVMFileWriter): Context = program match {
		case prog: instant.Absyn.Prog =>
			val statements = prog.liststmt_.toArray(Array[instant.Absyn.Stmt]()).toSeq
			statements.foldLeft(context) { (context, statement) => compile(statement, context, fileWriter) }
	}

	private def compile(statement: instant.Absyn.Stmt, context: Context, fileWriter: LLVMFileWriter): Context = statement match {
		case ass: instant.Absyn.SAss =>
			val contextAfterExpression = compile(translateBNFCExp(ass.exp_), context, fileWriter)
			val (operandSource, contextBeforeAssignment) = contextAfterExpression.popOperand
			contextBeforeAssignment.pushVariable(ass.ident_, operandSource)
		case exp: instant.Absyn.SExp =>
			val contextAfterExpression = compile(translateBNFCExp(exp.exp_), context, fileWriter)
			val (printedOperand, resultContext) = contextAfterExpression.popOperand
			fileWriter.writeLine(s"call i32 (i8*, ...) @printf(i8* %intFormatPtr, i32 $printedOperand)")
			resultContext
	}

	private def opCode: Op => String = {
		case Op.Add => "add"
		case Op.Sub => "sub"
		case Op.Mul => "mul"
		case Op.Div => "sdiv"
	}

	private def compile(expression: Exp, context: Context, fileWriter: LLVMFileWriter): Context = expression match {
		case ExpLit(value) => context.pushOperand(Constant(value))
		case ExpVar(name) => context.pushOperand(context.variables(name))
		case op: ExpOp =>
			val context1 = compile(op.exp1, context, fileWriter)
			val context2 = compile(op.exp2, context1, fileWriter)
			val (operand2, context3) = context2.popOperand
			val (operand1, context4) = context3.popOperand
			val resultValueSource = context4.nextRegister
			fileWriter.writeLine(s"$resultValueSource = ${opCode(op.op)} i32 $operand1, $operand2")
			context4.pushOperand(resultValueSource)
	}
}

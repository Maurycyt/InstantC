package backends.llvm

import java.io.File
import backends.Backend._

object Backend extends backends.Backend {
	override def doCompile(program: Prog, fileBasePath: String): Unit = {
		val fileWriter = BackendFileWriter(new File(s"$fileBasePath.ll"))
		try {
			fileWriter.write(
				s"""declare i32 @printf(i8*, ...)
					 |
					 |@intFormat = internal constant [4 x i8]
					 |\tc"%d\\0A\\00"
					 |
					 |define i32 @main() {
					 |entry:
					 |\t%intFormatPtr = bitcast [4 x i8]* @intFormat to i8*
					 |""".stripMargin
			)

			val statements = program.stmts
			statements.foldLeft(Context.empty) { (context, statement) => compile(statement, context, fileWriter) }

			fileWriter.write(
				s"""\tret i32 0
					 |}\n
					 |""".stripMargin
			)
		} finally {
			fileWriter.close()
		}
		println(s"Generated: $fileBasePath.ll")
		scala.sys.process.Process(s"llvm-as $fileBasePath.ll").!
		println(s"Generated: $fileBasePath.bc")
	}

	private def compile(statement: Stmt, context: Context, fileWriter: BackendFileWriter): Context = statement match {
		case SAss(name, exp) =>
			val contextAfterExpression = compile(exp, context, fileWriter)
			val (operandSource, contextBeforeAssignment) = contextAfterExpression.popOperand
			contextBeforeAssignment.pushVariable(name, operandSource)
		case SExp(exp) =>
			val contextAfterExpression = compile(exp, context, fileWriter)
			val (printedOperand, resultContext) = contextAfterExpression.popOperand
			fileWriter.writeLine(s"call i32 (i8*, ...) @printf(i8* %intFormatPtr, i32 $printedOperand)")
			resultContext
	}

	private def compile(expression: Exp, context: Context, fileWriter: BackendFileWriter): Context = expression match {
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

	private def opCode: Op => String = {
		case Op.Add => "add"
		case Op.Sub => "sub"
		case Op.Mul => "mul"
		case Op.Div => "sdiv"
	}
}

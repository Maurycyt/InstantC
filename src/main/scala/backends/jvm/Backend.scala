package backends.jvm

import java.io.File

object Backend extends backends.Backend {
	override def compile(program: instant.Absyn.Program, fileBasePath: String): Unit = {
		val fileBaseName = fileBasePath.reverse.takeWhile(_ != '/').reverse.takeWhile(_ != '.')
		val fileDirectory = fileBasePath.reverse.dropWhile(_ != '/').reverse
		val fileWriter = BackendFileWriter(new File(s"$fileBasePath.j"))
		try {
			val statements = getProgramStatements(program).map(optimizeStatement)
			val locals = statements.collect { case (_, SAss(name, _)) => name }.toSet.zipWithIndex.toMap
			val localsLimit = scala.math.max(1, locals.size)
			val stackLimit = (0 +: statements.map(_._1)).max

			fileWriter.write(
				s""".class $fileBaseName
					 |.super java/lang/Object
					 |
					 |.method <init>()V
					 |\taload_0
					 |\tinvokespecial java/lang/Object/<init>()V
					 |\treturn
					 |.end method
					 |
					 |.method public static main([Ljava/lang/String;)V
					 |\t.limit locals $localsLimit
					 |\t.limit stack $stackLimit
					 |""".stripMargin)

			statements.foreach { (_, stmt) => compile(stmt, locals, fileWriter) }

			fileWriter.write(
				s"""\treturn
					 |.end method
					 |""".stripMargin
			)
		} finally {
			fileWriter.close()
		}
		println(s"Generated: $fileBasePath.j")
		if (fileDirectory.nonEmpty) { // WHYYY
			scala.sys.process.Process(Seq("java", "-jar", "lib/jasmin.jar", "-d", fileDirectory, s"$fileBasePath.j")).!
		} else {
			scala.sys.process.Process(Seq("java", "-jar", "lib/jasmin.jar", s"$fileBasePath.j")).!
		}
	}

	private def optimizeStatement(stmt: Stmt): (Int, Stmt) = {
		val (height, optExp) = optimizeExp(stmt.exp)
		stmt match {
			case ass: SAss => (height, ass.copy(exp = optExp))
			case exp: SExp => (scala.math.max(2, height), exp.copy(exp = optExp)) // We need at least 2 for printing
		}
	}

	private def optimizeExp: Exp => (Int, Exp) = {
		case value: ExpVal => (1, value)
		case op: ExpOp =>
			val (height1, optimized1) = optimizeExp(op.exp1)
			val (height2, optimized2) = optimizeExp(op.exp2)
			if height1 >= height2 then (height1 + 1, op.copy(exp1 = optimized1, exp2 = optimized2)) else (height2, op.copy(exp1 = optimized2, exp2 = optimized1, flipped = true))
	}

	private def compile(stmt: Stmt, locals: Map[String, Int], fileWriter: BackendFileWriter): Unit = {
		compile (stmt.exp, locals, fileWriter)
		stmt match {
			case SAss(name, exp) =>
				fileWriter.writeLine(storeStr(locals(name)))
			case SExp(exp) =>
				fileWriter.writeLine("getstatic java/lang/System/out Ljava/io/PrintStream;")
				fileWriter.writeLine("swap")
				fileWriter.writeLine("invokevirtual java/io/PrintStream/println(I)V")
		}
	}

	private def compile(exp: Exp, locals: Map[String, Int], fileWriter: BackendFileWriter): Unit = exp match {
		case ExpLit(value) => fileWriter.writeLine(constStr(value))
		case ExpVar(name) => fileWriter.writeLine(loadStr(locals(name)))
		case ExpOp(exp1, exp2, op, flipped) =>
			compile(exp1, locals, fileWriter)
			compile(exp2, locals, fileWriter)
			if flipped && Seq(Op.Sub, Op.Div).contains(op) then fileWriter.writeLine("swap")
			fileWriter.writeLine(opCode(op))
	}

	private def storeStr(index: Int): String = "istore" + (if index <= 3 then "_" else " ") + index

	private def loadStr(index: Int): String = "iload" + (if index <= 3 then "_" else " ") + index

	private def constStr(value: Int) = value match {
		case -1 => "iconst_m1"
		case v if 0 <= v && v <= 5 => s"iconst_$v"
		case v if -(1 << 7) <= v && v < (1 << 7) => "bipush " + v
		case v if -(1 << 15) <= v && v < (1 << 15) => "sipush " + v
		case v => "ldc " + v
	}

	private def opCode: Op => String = {
		case Op.Add => "iadd"
		case Op.Sub => "isub"
		case Op.Mul => "imul"
		case Op.Div => "idiv"
	}
}

package backends

import backends.Backend.checkVars

import java.io.{File, FileWriter}
import io.circe.generic.extras.auto._
import io.circe.generic.extras.{semiauto, Configuration}
import io.circe.Decoder

trait Backend {
	final def compile(program: Backend.Prog, fileBasePath: String): Unit = {
		checkVars(program)
		doCompile(program, fileBasePath)
	}

	protected def doCompile(program: Backend.Prog, fileBasePath: String): Unit
}

object Backend {
	sealed trait Op
	object Op {
		object Add extends Op
		object Sub extends Op
		object Mul extends Op
		object Div extends Op
	}

	sealed trait Exp
	sealed trait ExpVal extends Exp
	case class ExpLit(value: Int) extends ExpVal
	case class ExpVar(name: String) extends ExpVal
	case class ExpOp(op: Op, exp1: Exp, exp2: Exp, flipped: Boolean = false) extends Exp

	sealed trait Stmt { def exp: Exp }
	case class SAss(name: String, exp: Exp) extends Stmt
	case class SExp(exp: Exp) extends Stmt

	case class Prog(stmts: Seq[Stmt])

	implicit val circeConfig: Configuration = Configuration.default.withDefaults
	implicit val progDecoder: Decoder[Prog] = semiauto.deriveConfiguredDecoder

	case class BackendFileWriter(file: File) extends FileWriter(file) {
		def writeLine(line: String): Unit = write(s"\t$line\n")
	}

	case class UndeclaredVariableException(name: String) extends Exception

	private def checkVars(program: Backend.Prog): Unit = {
		// We only check for undeclared variables.
		program.stmts.foldLeft(Set.empty[String]) { case (seenVars, stmt) =>
			checkVars(seenVars, stmt.exp)
			stmt match {
				case SAss(name, _) => seenVars + name
				case SExp(_) => seenVars
			}
		}
	}

	private def checkVars(seenVars: Set[String], exp: Exp): Unit = { exp match {
			case ExpLit(_) => ()
			case ExpVar(name) => if (!seenVars.contains(name)) throw UndeclaredVariableException(name) else ()
			case ExpOp(_, exp1, exp2, _) => checkVars(seenVars, exp1); checkVars(seenVars, exp2)
		}
	}
}

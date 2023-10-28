package backends

import java.io.{ File, FileWriter }
import io.circe.generic.extras.auto._
import io.circe.generic.extras.{ Configuration, semiauto }
import io.circe.Decoder

trait Backend {
	def compile(program: Backend.Prog, fileBasePath: String): Unit
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
}

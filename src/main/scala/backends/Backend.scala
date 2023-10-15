package backends

import java.io.{File, FileWriter}

trait Backend {
	def compile(program: instant.Absyn.Program, fileBasePath: String): Unit

	protected enum Op { case Add, Sub, Mul, Div }

	protected sealed trait Exp
	protected sealed trait ExpVal extends Exp
	protected case class ExpLit(value: Int) extends ExpVal
	protected case class ExpVar(name: String) extends ExpVal
	protected case class ExpOp(exp1: Exp, exp2: Exp, op: Op, flipped: Boolean = false) extends Exp

	protected sealed trait Stmt(val exp: Exp)
	protected case class SAss(name: String, override val exp: Exp) extends Stmt(exp)
	protected case class SExp(override val exp: Exp) extends Stmt(exp)

	private def translateBNFCExp: instant.Absyn.Exp => Exp = {
		case lit: instant.Absyn.ExpLit => ExpLit(lit.integer_)
		case vrr: instant.Absyn.ExpVar => ExpVar(vrr.ident_)
		case add: instant.Absyn.ExpAdd => ExpOp(translateBNFCExp(add.exp_1), translateBNFCExp(add.exp_2), Op.Add)
		case sub: instant.Absyn.ExpSub => ExpOp(translateBNFCExp(sub.exp_1), translateBNFCExp(sub.exp_2), Op.Sub)
		case mul: instant.Absyn.ExpMul => ExpOp(translateBNFCExp(mul.exp_1), translateBNFCExp(mul.exp_2), Op.Mul)
		case div: instant.Absyn.ExpDiv => ExpOp(translateBNFCExp(div.exp_1), translateBNFCExp(div.exp_2), Op.Div)
	}

	private def translateBNFCStmt: instant.Absyn.Stmt => Stmt = {
		case ass: instant.Absyn.SAss => SAss(ass.ident_, translateBNFCExp(ass.exp_))
		case exp: instant.Absyn.SExp => SExp(translateBNFCExp(exp.exp_))
	}

	protected def getProgramStatements: instant.Absyn.Program => Seq[Stmt] = {
		case prog: instant.Absyn.Prog => prog.liststmt_.toArray(Array[instant.Absyn.Stmt]()).toSeq.map(translateBNFCStmt)
	}

	protected case class BackendFileWriter(file: File) extends FileWriter(file) {
		def writeLine(line: String): Unit = write(s"\t$line\n")
	}
}

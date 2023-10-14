package backends

trait Backend {
	def compile(program: instant.Absyn.Program, fileBaseName: String): Unit

	protected enum Op { case Add, Sub, Mul, Div }
	
	protected sealed trait Exp
	protected sealed trait ExpVal extends Exp
	protected case class ExpLit(value: Int) extends ExpVal
	protected case class ExpVar(name: String) extends ExpVal
	protected case class ExpOp(exp1: Exp, exp2: Exp, op: Op) extends Exp

	protected def translateBNFCExp(BNFCExp: instant.Absyn.Exp): Exp = BNFCExp match {
		case lit: instant.Absyn.ExpLit => ExpLit(lit.integer_)
		case vrr: instant.Absyn.ExpVar => ExpVar(vrr.ident_)
		case add: instant.Absyn.ExpAdd => ExpOp(translateBNFCExp(add.exp_1), translateBNFCExp(add.exp_2), Op.Add)
		case sub: instant.Absyn.ExpSub => ExpOp(translateBNFCExp(sub.exp_1), translateBNFCExp(sub.exp_2), Op.Sub)
		case mul: instant.Absyn.ExpMul => ExpOp(translateBNFCExp(mul.exp_1), translateBNFCExp(mul.exp_2), Op.Mul)
		case div: instant.Absyn.ExpDiv => ExpOp(translateBNFCExp(div.exp_1), translateBNFCExp(div.exp_2), Op.Div)
	}
}

package backends.jvm

import instant.Absyn.*

object Backend extends backends.Backend {
	private def opCode: Op => String = {
		case Op.Add => "iadd"
		case Op.Sub => "isub"
		case Op.Mul => "imul"
		case Op.Div => "idiv"
	}

	private def optimizeExp(exp: Exp): (Int, Exp) = exp match {
		case value: ExpVal => (1, value)
		case op: ExpOp =>
			val (height1, optimized1) = optimizeExp(op.exp1)
			val (height2, optimized2) = optimizeExp(op.exp2)
			if height1 >= height2 then (height1 + 2, op.copy(exp1 = optimized1, exp2 = optimized2)) else (height2, op.copy(exp1 = optimized2, exp2 = optimized1))
	}

	override def compile(program: Program, fileBaseName: String): Unit = program match {
		case prog: Prog => ???
	}
}

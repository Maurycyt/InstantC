package backends.llvm

/**
 * Describes how to obtain the value of a variable or operand.
 */
sealed trait ValueSource

/**
 * Indicates that a value is constant and has not been assigned to a register.
 * @param value The value of the constant.
 */
case class Constant(value: Int) extends ValueSource {
	override def toString: String = value.toString
}

/**
 * Indicates that a value is stored in a virtual register.
 * @param rName The name of the register, e.g. "%r0".
 */
case class Register(rName: String) extends ValueSource {
	override def toString: String = rName
}

/**
 * Carries information about where the variables and unused operands are stored.
 * @param variables A map from the user's variable names to storage data.
 * @param operands A stack of the unused operands' storage data.
 */
case class Context(
	variables: Map[String, ValueSource],
	operands: List[ValueSource],
	lastRegisterIndex: Int
) {
	// TODO incorporate variable names into register names
	def nextRegister: Register = Register(s"%r$lastRegisterIndex")

	def pushVariable(name: String, source: ValueSource): Context =
		Context(variables + (name -> source), operands, lastRegisterIndex + (source match { case _: Register => 1; case _ => 0 }))

	def pushOperand(source: ValueSource): Context =
		Context(variables, source :: operands, lastRegisterIndex + (source match { case _: Register => 1; case _ => 0 }))

	def popOperand: (ValueSource, Context) = (operands.head, copy(operands = operands.tail))
}

object Context {
	val empty: Context = Context(Map.empty, List.empty, 0)
}

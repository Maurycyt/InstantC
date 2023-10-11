package backends.jvm

import instant.Absyn.Program

object Backend extends backends.Backend {
	override def compile(program: Program, fileBaseName: String): Unit = ???
}

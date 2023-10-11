package backends

import instant.Absyn.Program

trait Backend {
	def compile(program: Program, fileBaseName: String): Unit
}

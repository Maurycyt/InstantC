import backends.Backend

/**
 * Combines the Lexer and the Parser.
 */
object Lerser {
	def lerse(filePathString: String): Backend.Prog = {
		val progJSON = sys.process.Process(Seq("src/grammar/LerseInstant", filePathString)).!!
		import Backend.progDecoder
		io.circe.parser.decode[Backend.Prog](progJSON) match {
			case Left(e) => throw e
			case Right(prog) => prog
		}
	}
}

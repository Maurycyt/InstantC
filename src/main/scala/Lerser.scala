import instant.{instantLexer, instantParser}
import instant.Absyn.Program
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA

import java.util

/**
 * Combines the Lexer and the Parser.
 */
object Lerser {
	case class LerserException(msg: String, cause: Throwable) extends Exception(msg, cause)

	case class SyntaxError(message: String, row: Int, column: Int) extends RuntimeException

	object BNFCErrorListener extends ANTLRErrorListener {
		override def syntaxError(recognizer: Recognizer[_, _], offendingSymbol: Any, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException): Unit = {
			throw SyntaxError(msg, line, charPositionInLine)
		}
		override def reportAmbiguity(recognizer: Parser, dfa: DFA, startIndex: Int, stopIndex: Int, exact: Boolean, ambigAlts: util.BitSet, configs: ATNConfigSet): Unit = {
			throw RuntimeException("Unexpected ambiguity.")
		}
		override def reportAttemptingFullContext(recognizer: Parser, dfa: DFA, startIndex: Int, stopIndex: Int, conflictingAlts: util.BitSet, configs: ATNConfigSet): Unit = {}
		override def reportContextSensitivity(recognizer: Parser, dfa: DFA, startIndex: Int, stopIndex: Int, prediction: Int, configs: ATNConfigSet): Unit = {}
	}
	
	def lerse(filePathString: String): Program = {
		try {
			val l: instantLexer = new instantLexer(CharStreams.fromFileName(filePathString))
			l.addErrorListener(BNFCErrorListener)
			val p: instantParser = new instantParser(CommonTokenStream(l))
			p.addErrorListener(BNFCErrorListener)
			val pc: instantParser.Start_ProgramContext = p.start_Program
			pc.result
		} catch {
			case so: StackOverflowError => throw LerserException("Could not parse the program due to error in ANTLR. This is probably a stack overflow error:", so)
		}
	}
}

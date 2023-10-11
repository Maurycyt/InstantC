import Lerser.SyntaxError
import backends.*

def exitWithError(message: String, status: Int = 1): Unit = {
	if (message.nonEmpty) {
		System.err.println(message)
	}
	System.exit(status)
}

val targetToBackend: Map[String, Backend] = Map(
	"jvm" -> jvm.Backend,
	"llvm" -> llvm.Backend
)

@main
def main(target: String, inputPathString: String): Unit = {
	if (!targetToBackend.keySet.contains(target)) {
		System.err.println(s"Invalid target '$target'.")
		return
	}

	try {
		val ast = Lerser.lerse(inputPathString)
		// Take the second-to-last filename component, or the last one.
		val fileBaseName = inputPathString.split('.').reverse.take(2).last
		println(fileBaseName)
		targetToBackend(target).compile(ast, fileBaseName)
	} catch {
		case e: java.nio.file.NoSuchFileException =>
			exitWithError(s"File does not exist: '${e.getFile}'")
		case s: SyntaxError =>
			exitWithError("")
	}
}

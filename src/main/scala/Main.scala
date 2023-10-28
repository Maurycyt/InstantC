import backends._

object Main {
	private def exitWithError(message: String, status: Int = 1): Unit = {
		if (message.nonEmpty) {
			System.err.println(message)
		}
		System.exit(status)
	}

	private val targetToBackend: Map[String, Backend] = Map(
		"jvm" -> jvm.Backend,
		"llvm" -> llvm.Backend
	)

	def main(args: Array[String]): Unit = {
		val target = args(0)
		val inputPathString = args(1)
		if (!targetToBackend.keySet.contains(target)) {
			System.err.println(s"Invalid target '$target'.")
			return
		}

		try {
			val ast = Lerser.lerse(inputPathString)
			val fileBasePath = inputPathString.take(((x: Int) => if (x > 0) x else inputPathString.length)(inputPathString.lastIndexOf('.')))
			targetToBackend(target).compile(ast, fileBasePath)
		} catch {
			case e: java.nio.file.NoSuchFileException =>
				exitWithError(s"File does not exist: '${e.getFile}'")
		}
	}
}

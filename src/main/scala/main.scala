import scala.io.Source

@main
def main(target: String, inputFilePath: String): Unit =
	println(s"Target: $target")
	val bufferedSource = Source.fromFile(inputFilePath)
	val inputString = try bufferedSource.mkString finally bufferedSource.close
	println(s"Got input: $inputString")
	instant.Test.main(Array.empty)

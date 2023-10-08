import sbt.nio.file.FileTreeView

import scala.sys.process.Process

ThisBuild / version := "1"
ThisBuild / scalaVersion := "3.3.1"

libraryDependencies += "org.antlr" % "antlr4" % "4.12.0"

lazy val root = (project in file("."))
  .settings(
	  // Name of the project
    name := "InstantC",

	  // BNFC generates instant.Test which contains a main method.
	  // This disambiguates the main class.
	  Compile / mainClass := Some("main"),

	  // Makes `sbt run` redirect I/O, including Ctrl+D properly.
	  run / fork := true,
	  run / connectInput := true,

	  // Generates the lexer, parser, and AST .java files before compilation, if necessary.
    Compile / sourceGenerators += Def.task {
	    val extraClasspath = (Compile / dependencyClasspath).value.map(_.data).mkString(".:", ":", "")

	    val cachedFunction = FileFunction.cached(
		    streams.value.cacheDirectory / "grammar"
	    ) { (in: Set[File]) =>
		    Process("bnfc -m --java-antlr src/grammar/Instant.cf -o src/grammar").!
		    Process("make -C src/grammar", None, "CLASSPATH" -> extraClasspath).!

		    val generatedFilesRootGlob = Glob("src/grammar/instant")
		    val generatedAbstractSyntaxFilesGlob = generatedFilesRootGlob / "Absyn" / *
		    val generatedParserFilesGlob = generatedFilesRootGlob / *

		    val allGrammarSources = FileTreeView.default.list(Seq(generatedAbstractSyntaxFilesGlob, generatedParserFilesGlob))
			    .map(_._1.toFile).filter(_.isFile).toSet
		    val generatedJavaGrammarSources = allGrammarSources.filter(_.toString.endsWith(".java"))
		    generatedJavaGrammarSources
	    }

	    val inputFile = file("src/grammar/Instant.cf")

      cachedFunction(Set(inputFile)).toSeq
    },
  )

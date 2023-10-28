ThisBuild / version := "1"
ThisBuild / scalaVersion := "2.13.10"

libraryDependencies += "io.circe" %% "circe-generic" % "0.14.5"
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.5"
libraryDependencies += "io.circe" %% "circe-generic-extras" % "0.14.3"

lazy val root = (project in file("."))
	.settings(
		// Name of the project
		name := "InstantC",

		// Makes `sbt run` redirect I/O, including Ctrl+D, properly.
		run / fork := true,
		run / connectInput := true,

		// Generates the lexer, parser, and AST .java files before compilation, if necessary.
		Compile / sourceGenerators += Def.task {
			val extraClasspath = (Compile / dependencyClasspath).value.map(_.data).mkString(".:", ":", "")
			IO.write(Path("dependencies-classpath.cp").asFile, extraClasspath)

			def startScriptSource(target: String): String =
				s"""#!/bin/bash
					 |make -s MAIN_ARGS="$target $$1" run
					 |""".stripMargin

			Seq("jvm", "llvm").foreach { target =>
				IO.write(Path(s"insc_$target").asFile, startScriptSource(target))
			}

			Seq()
		}
	)
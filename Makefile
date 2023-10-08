.PHONY: run
run: build.sbt
	sbt run

.PHONY: build
build: build.sbt
	sbt compile

.PHONY: clean
clean:
	rm -rf target project/target project/project/target src/grammar/Makefile src/grammar/instant
MAIN_CLASSPATH=target/scala-3.3.1/classes/
INCLUDE_CLASSPATH=-cp ${MAIN_CLASSPATH} -cp `cat dependencies-classpath.cp`
SBT_FLAGS=-Dsbt.io.implicit.relative.glob.conversion=allow
SBT=sbt ${SBT_FLAGS}
MAIN_ARGS=

target/scala-3.3.1/classes/main.class:
	make build

.PHONY: run
run: ${MAIN_CLASSPATH}main.class
	scala ${INCLUDE_CLASSPATH} main ${MAIN_ARGS}

.PHONY: build
build: build.sbt
	${SBT} compile
	chmod +x insc_jvm insc_llvm

.PHONY: clean
clean:
	rm -rf target project/target project/project/target src/grammar/Makefile src/grammar/instant dependencies-classpath.cp insc_jvm insc_llvm
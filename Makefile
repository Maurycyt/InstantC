BNFC_TARGET=src/grammar/generated
BNFC_FLAGS=-m -o ${BNFC_TARGET} src/grammar/Instant.cf
MAIN_CLASSPATH=target/scala-2.13/classes/
INCLUDE_CLASSPATH=-cp ${MAIN_CLASSPATH} -cp `cat dependencies-classpath.cp`
SBT=sbt

MAIN_ARGS= # To be supplied by the calling script

${MAIN_CLASSPATH}Main.class:
	make build

src/grammar/LerseInstant: src/grammar/LerseInstant.hs
	/home/students/inf/PUBLIC/MRJP/bin/bnfc ${BNFC_FLAGS} || bnfc ${BNFC_FLAGS}
	make -C ${BNFC_TARGET} LexInstant.hs *.hs
	ghc -i${BNFC_TARGET} src/grammar/LerseInstant

.PHONY: run
run: ${MAIN_CLASSPATH}Main.class
	-scala ${INCLUDE_CLASSPATH} -J-Xss128m Main ${MAIN_ARGS}

.PHONY: build
build: build.sbt src/grammar/LerseInstant
	${SBT} compile
	@chmod +x insc_jvm insc_llvm

.PHONY: clean
clean:
	rm -rf target project/target project/project/target
	rm -rf src/grammar/generated
	rm -rf src/grammar/LerseInstant.hi src/grammar/LerseInstant.o src/grammar/LerseInstant
	rm -rf insc_jvm insc_llvm

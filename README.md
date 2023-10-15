# Table of Contents

1. [InstantC Specification](#instantc-specification)
   1. [Language Specification](#language-specification)
   2. [Aim of this Project](#aim-of-this-project)
2. [Building and Running](#building-and-running)

# InstantC Specification

## Language Specification

The Instant language is a (very) minimalistic language which supports integer constants and variables, as well as the four basic calculator operations. The LBNF syntax of Instant is detailed in [Instant.cf](src/grammar/Instant.cf), and looks as follows:

```lbnf
Prog. Program ::= [Stmt] ;
SAss. Stmt ::= Ident "=" Exp;
SExp. Stmt ::= Exp ;
separator Stmt ";" ;

ExpAdd.            Exp1   ::= Exp2 "+"  Exp1 ;
ExpSub.            Exp2   ::= Exp2 "-"  Exp3 ;
ExpMul.            Exp3   ::= Exp3 "*"  Exp4 ;
ExpDiv.            Exp3   ::= Exp3 "/"  Exp4 ;
ExpLit.            Exp4   ::= Integer ;
ExpVar.            Exp4   ::= Ident ;
coercions Exp 4;
```

An Instant program consists of a list of statements. Each statement can be either an assignment, or a bare expression. In both cases, the expression in the statement is evaluated. Then, if the statement is an assignment, the value of the expression is assigned to the variable to the left of the assignment operator. Otherwise, the value of the expression is printed to standard output.

For example:
```
1 + 1
a = 3
a * 10
```
will print
```
2
30
```

## Aim of this Project

The aim of this project is to introduce the two models of stack-based and register-based virtual machines (JVM and LLVM respectively). The InstantC compiler is capable of producing Jasmin mnemonics which get assembled into Java classes, as well as LLVM mnemonics and bitcode which can be assembled further or interpreted by `lli`.

The project does *not* aim to introduce optimizations in the code, like substituting `2 + 3` with `5`. The only optimization it is allowed (and desired) to make is reducing the operand stack limit in the JVM by manipulating the order in which arguments are computed.

# Building and Running

You need `bnfc` and `sbt` installed in your environment.

To build the compiler, simply run `make` in the root directory. This will generate all the BNFC sources and build the compiler itself. It will create two executable files, `insc_llvm` and `insc_jvm`, which take a file as their only argument and create the corresponding `.ll` and `.bc` or `.j` and `.class` files in the same directory as the given file. To execute the files, use `lli` or `java` respectively.

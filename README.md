# Table of Contents

1. [InstantC Specification](#instantc-specification)
   1. [Language Specification](#language-specification)
   2. [Aim of this Project](#aim-of-this-project)
2. [Building and Running](#building-and-running)
3. [Development Remarks](#development-remarks)

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

# Development Remarks

I chose Scala for this project because I was convinced that it would be a perfect tool for the purpose of writing a compiler. Indeed, there are just over 250 lines of logic written in Scala, with a few extra lines added to make everything work, like the build system (`build.sbt`), and some Haskell which parses the input Instant code and turns it into JSON so that it can then be parsed in Scala. For comparison, this same project was completed by other students in other languages, with some C++ implementations reaching 700 or even 1000 lines.

But wait: JSON? Generated by Haskell? Why? Why not write the whole thing in Haskell then? Well, while I appreciate Haskell's approach to programming and its benefits, I feel like it lacks the flexibility I might need to solve problems which appear late into development. That is why I chose to write the compiler in a language with fantastic support for both functional and imperative programming, although I didn't use the imperative side in this project. Nonetheless, I needed to check if Scala would be a good fit for the much bigger future project of a [Latte compiler](https://github.com/Maurycyt/LatteC), with extensions and optimizations. I believe it passed the test.

And I had to use Haskell because this was the simplest way to parse Instant programs. Originally I planned to write the project in Scala 3 using [BNFC](https://github.com/BNFC/bnfc)'s Java backend, and use the Java classes from Scala. This worked, but not with the default Java backend, only with ANTLR. However, for inputs with highly nested expressions, the parsing lasted forever, hogged 2GB of RAM, before crashing with a stack overflow error (in fact it seems that at the time of writing, many of BNFC's backends suffer from serious memory problems). After some investigation, I decided to switch to Haskell and pass the parsed Instant programs to Scala via JSON with [Circe](https://github.com/circe/circe). This turned out to be surprisingly problematic, because at the time of writing, recursive ADTs do not work well in Circe in Scala 3. Consequently, I had to switch to Scala 2, where after increasing the stack size appropriately, very large inputs are now accepted without further hiccups.

Edit to add:  
It is worth adding that all of these troubles would have been avoided had I used ANTLR directly from the beginning, instead of via BNFC. In fact, that is exactly what I am doing in the [Latte compiler](https://github.com/Maurycyt/LatteC), but I wasn't familiar enough with the technology back when working on InstantC to use it then.

Tests are not included because they are community-created in a separate project.

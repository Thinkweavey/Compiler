# compiler-lab-java

Java skeleton project for three-stage compiler lab:

1. Lexical analysis
2. LR(1) syntax analysis
3. Syntax-directed translation and three-address code generation

## Quick Start

- Build: `mvn clean test`
- Run: `mvn -q exec:java -Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication`

## Package Overview

- `app`: application entry and pipeline
- `common`: shared models (token, position, error)
- `lexer`: lexical analyzer contracts and implementation placeholders
- `parser`: parser contracts and implementation placeholders
- `semantic`: semantic analysis contracts and implementation placeholders
- `ir`: IR generation contracts and implementation placeholders
- `symbol`: symbol table contracts and basic implementation placeholder


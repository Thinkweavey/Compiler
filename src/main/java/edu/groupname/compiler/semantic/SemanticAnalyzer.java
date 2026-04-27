package edu.groupname.compiler.semantic;

import edu.groupname.compiler.parser.ParserResult;

public interface SemanticAnalyzer {
    SemanticResult analyze(ParserResult parserResult);
}


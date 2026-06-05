package compiler.semantic;

import compiler.parser.ParserResult;

public interface SemanticAnalyzer {
    SemanticResult analyze(ParserResult parserResult);
}


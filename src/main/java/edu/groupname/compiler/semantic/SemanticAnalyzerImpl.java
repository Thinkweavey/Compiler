package edu.groupname.compiler.semantic;

import edu.groupname.compiler.parser.ParserResult;

import java.util.List;

public class SemanticAnalyzerImpl implements SemanticAnalyzer {
    @Override
    public SemanticResult analyze(ParserResult parserResult) {
        return new SemanticResult(List.of());
    }
}


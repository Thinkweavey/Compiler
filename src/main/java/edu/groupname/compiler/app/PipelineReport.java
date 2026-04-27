package edu.groupname.compiler.app;

import edu.groupname.compiler.ir.IRProgram;
import edu.groupname.compiler.lexer.LexicalAnalyzerResult;
import edu.groupname.compiler.parser.ParserResult;
import edu.groupname.compiler.semantic.SemanticResult;

public record PipelineReport(
        LexicalAnalyzerResult lexicalResult,
        ParserResult parserResult,
        SemanticResult semanticResult,
        IRProgram irProgram
) {
}


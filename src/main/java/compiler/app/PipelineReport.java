package compiler.app;

import compiler.ir.IRProgram;
import compiler.lexer.LexicalAnalyzerResult;
import compiler.parser.ParserResult;
import compiler.semantic.SemanticResult;

public record PipelineReport(
        LexicalAnalyzerResult lexicalResult,
        ParserResult parserResult,
        SemanticResult semanticResult,
        IRProgram irProgram
) {
}


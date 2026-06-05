package compiler.app;

import compiler.ir.IRGenerator;
import compiler.ir.IRProgram;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.parser.ParserResult;
import compiler.semantic.SemanticAnalyzer;
import compiler.semantic.SemanticResult;

import java.util.List;
import java.util.Map;

public class CompilerPipeline {
    private final Lexer lexer;
    private final Parser parser;
    private final SemanticAnalyzer semanticAnalyzer;
    private final IRGenerator irGenerator;

    public CompilerPipeline(Lexer lexer, Parser parser, SemanticAnalyzer semanticAnalyzer, IRGenerator irGenerator) {
        this.lexer = lexer;
        this.parser = parser;
        this.semanticAnalyzer = semanticAnalyzer;
        this.irGenerator = irGenerator;
    }

    public PipelineReport compile(String sourceCode) {
        var lexResult = lexer.analyze(sourceCode);
        if (lexResult.hasErrors()) {
            return new PipelineReport(
                    lexResult,
                    emptyParserResult(),
                    emptySemanticResult(),
                    emptyIrProgram()
            );
        }

        var parserResult = parser.parse(lexResult.tokens());
        if (!parserResult.accepted() || parserResult.hasErrors()) {
            return new PipelineReport(lexResult, parserResult, emptySemanticResult(), emptyIrProgram());
        }

        var semanticResult = semanticAnalyzer.analyze(parserResult);
        var irProgram = irGenerator.generate(semanticResult);
        return new PipelineReport(lexResult, parserResult, semanticResult, irProgram);
    }

    private static ParserResult emptyParserResult() {
        return new ParserResult(false, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static SemanticResult emptySemanticResult() {
        return new SemanticResult(List.of(), List.of(), List.of(), Map.of(), List.of());
    }

    private static IRProgram emptyIrProgram() {
        return new IRProgram(List.of());
    }
}


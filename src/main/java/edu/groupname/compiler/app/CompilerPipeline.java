package edu.groupname.compiler.app;

import edu.groupname.compiler.ir.IRGenerator;
import edu.groupname.compiler.lexer.Lexer;
import edu.groupname.compiler.parser.Parser;
import edu.groupname.compiler.semantic.SemanticAnalyzer;

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
        var parserResult = parser.parse(lexResult.tokens());
        var semanticResult = semanticAnalyzer.analyze(parserResult);
        var irProgram = irGenerator.generate(semanticResult);
        return new PipelineReport(lexResult, parserResult, semanticResult, irProgram);
    }
}


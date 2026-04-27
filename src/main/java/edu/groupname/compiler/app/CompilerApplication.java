package edu.groupname.compiler.app;

import edu.groupname.compiler.ir.IRGeneratorImpl;
import edu.groupname.compiler.lexer.LexerImpl;
import edu.groupname.compiler.parser.LR1Parser;
import edu.groupname.compiler.semantic.SemanticAnalyzerImpl;

public final class CompilerApplication {
    private CompilerApplication() {
    }

    public static void main(String[] args) {
        CompilerPipeline pipeline = new CompilerPipeline(
                new LexerImpl(),
                new LR1Parser(),
                new SemanticAnalyzerImpl(),
                new IRGeneratorImpl()
        );

        String demoCode = "{ int a; a = 1; }";
        PipelineReport report = pipeline.compile(demoCode);
        System.out.println("Token count: " + report.lexicalResult().tokens().size());
        System.out.println("Parser accepted: " + report.parserResult().accepted());
        System.out.println("IR count: " + report.irProgram().instructions().size());
    }
}


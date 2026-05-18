package edu.groupname.compiler.app;

import edu.groupname.compiler.ir.IRGeneratorImpl;
import edu.groupname.compiler.ir.IRProgram;
import edu.groupname.compiler.lexer.LexerImpl;
import edu.groupname.compiler.parser.LR1Parser;
import edu.groupname.compiler.semantic.SemanticAnalyzerImpl;

public final class CompilerApplication {
    private CompilerApplication() {
    }

    public static void main(String[] args) throws Exception {
        LR1Parser parser = new LR1Parser();
        CompilerPipeline pipeline = new CompilerPipeline(
                new LexerImpl(),
                parser,
                new SemanticAnalyzerImpl(),
                new IRGeneratorImpl()
        );

        String source = ExperimentDemo.loadSource(args);
        PipelineReport report = pipeline.compile(source);
        ExperimentDemo.printReport(report, parser, args);

        if (!report.lexicalResult().hasErrors() && report.parserResult().accepted()) {
            System.out.println();
            System.out.println("========== 后续阶段（实验三预留） ==========");
            System.out.println("IR 指令条数: " + report.irProgram().instructions().size());
        }
    }
}


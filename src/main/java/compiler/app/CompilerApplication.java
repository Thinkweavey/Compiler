package compiler.app;

import compiler.ir.IRGeneratorImpl;
import compiler.ir.IRProgram;
import compiler.lexer.LexerImpl;
import compiler.parser.LR1Parser;
import compiler.semantic.SemanticAnalyzerImpl;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class CompilerApplication {
    private CompilerApplication() {
    }

    public static void main(String[] args) throws Exception {
        configureConsoleUtf8();
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
    }

    /** Use UTF-8 on Windows consoles where the default charset is not UTF-8. */
    private static void configureConsoleUtf8() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // Keep JVM default streams.
        }
    }
}


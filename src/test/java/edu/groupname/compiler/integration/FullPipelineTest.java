package edu.groupname.compiler.integration;

import edu.groupname.compiler.app.CompilerPipeline;
import edu.groupname.compiler.ir.IRGeneratorImpl;
import edu.groupname.compiler.lexer.LexerImpl;
import edu.groupname.compiler.parser.LR1Parser;
import edu.groupname.compiler.semantic.SemanticAnalyzerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FullPipelineTest {
    @Test
    void pipelineReturnsReport() {
        CompilerPipeline pipeline = new CompilerPipeline(
                new LexerImpl(),
                new LR1Parser(),
                new SemanticAnalyzerImpl(),
                new IRGeneratorImpl()
        );
        var report = pipeline.compile("{ int a; a = 1; }");
        assertNotNull(report);
    }
}


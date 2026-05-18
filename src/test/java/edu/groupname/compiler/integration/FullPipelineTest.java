package edu.groupname.compiler.integration;

import edu.groupname.compiler.app.CompilerPipeline;
import edu.groupname.compiler.ir.IRGeneratorImpl;
import edu.groupname.compiler.lexer.LexerImpl;
import edu.groupname.compiler.parser.LR1Parser;
import edu.groupname.compiler.semantic.SemanticAnalyzerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FullPipelineTest {
    private final CompilerPipeline pipeline = new CompilerPipeline(
            new LexerImpl(),
            new LR1Parser(),
            new SemanticAnalyzerImpl(),
            new IRGeneratorImpl()
    );

    @Test
    void pipelineReturnsReport() {
        var report = pipeline.compile("{ int a; a = 1; }");
        assertNotNull(report);
        assertTrue(report.parserResult().accepted());
    }

    @Test
    void pipelineShortCircuitsOnLexicalError() {
        var report = pipeline.compile("{ int @a; }");
        assertTrue(report.lexicalResult().hasErrors());
        assertFalse(report.parserResult().accepted());
        assertTrue(report.parserResult().trace().isEmpty());
    }

    @Test
    void pipelineShortCircuitsOnSyntaxError() {
        var report = pipeline.compile("{ int a; ");
        assertFalse(report.parserResult().accepted());
        assertTrue(report.irProgram().instructions().isEmpty());
    }
}


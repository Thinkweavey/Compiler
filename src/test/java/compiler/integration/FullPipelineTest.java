package compiler.integration;

import compiler.app.CompilerPipeline;
import compiler.ir.IRGeneratorImpl;
import compiler.lexer.LexerImpl;
import compiler.parser.LR1Parser;
import compiler.semantic.SemanticAnalyzerImpl;
import org.junit.jupiter.api.Test;

import compiler.ir.Opcode;

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
        assertFalse(report.irProgram().instructions().isEmpty());
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

    @Test
    void pipelineKeepsQuadruplesWhenSemanticError() {
        var report = pipeline.compile("""
                { int a; a = 1; break; }
                """);
        assertTrue(report.semanticResult().hasErrors());
        assertFalse(report.irProgram().instructions().isEmpty());
        assertTrue(report.irProgram().instructions().stream().anyMatch(q -> q.op() == Opcode.ASSIGN));
    }
}


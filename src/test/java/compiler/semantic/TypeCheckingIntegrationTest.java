package compiler.semantic;

import compiler.app.CompilerPipeline;
import compiler.ir.IRGeneratorImpl;
import compiler.lexer.LexerImpl;
import compiler.parser.LR1Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeCheckingIntegrationTest {
    private final CompilerPipeline pipeline = new CompilerPipeline(
            new LexerImpl(),
            new LR1Parser(),
            new SemanticAnalyzerImpl(),
            new IRGeneratorImpl()
    );

    @Test
    void allowsIntToIntAssignmentThroughIdentifier() {
        var report = pipeline.compile("{ int a; int b; b = 1; a = b; }");
        assertFalse(report.semanticResult().hasErrors(), () -> report.semanticResult().errors().toString());
    }

    @Test
    void rejectsFloatAssignedToInt() {
        var report = pipeline.compile("{ int a; a = 2.5; }");
        assertTrue(report.semanticResult().hasErrors());
        assertTrue(report.semanticResult().errors().stream()
                .anyMatch(e -> e.message().contains("类型不匹配") || e.message().contains("类型非法")));
    }

    @Test
    void rejectsIntFloatMixInExpression() {
        var report = pipeline.compile("{ float x; int i; x = i + 1.0; }");
        assertTrue(report.semanticResult().hasErrors());
    }

    @Test
    void allowsFloatUnaryMinus() {
        var report = pipeline.compile("{ float x; x = -2.5; }");
        assertFalse(report.semanticResult().hasErrors(), () -> report.semanticResult().errors().toString());
        assertTrue(report.irProgram().instructions().stream()
                .anyMatch(q -> q.op() == compiler.ir.Opcode.SUB));
    }
}

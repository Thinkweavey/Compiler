package compiler.semantic;

import compiler.app.CompilerPipeline;
import compiler.ir.IRGeneratorImpl;
import compiler.lexer.LexerImpl;
import compiler.parser.LR1Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayBoundsTest {
    private final CompilerPipeline pipeline = new CompilerPipeline(
            new LexerImpl(),
            new LR1Parser(),
            new SemanticAnalyzerImpl(),
            new IRGeneratorImpl()
    );

    @Test
    void allowsIndexWithinBounds() {
        var report = pipeline.compile("{ int[10] arr; arr[9] = 1; }");
        assertFalse(report.semanticResult().hasErrors(), () -> report.semanticResult().errors().toString());
    }

    @Test
    void rejectsOutOfBoundsIndex() {
        var report = pipeline.compile("{ int[10] arr; arr[10] = 1; }");
        assertTrue(report.semanticResult().hasErrors());
        assertTrue(report.semanticResult().errors().stream()
                .anyMatch(e -> e.message().contains("越界")));
    }
}

package compiler.semantic;

import compiler.lexer.LexerImpl;
import compiler.parser.LR1Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BreakSemanticTest {
    private final SemanticAnalyzerImpl analyzer = new SemanticAnalyzerImpl();
    private final LR1Parser parser = new LR1Parser();
    private final LexerImpl lexer = new LexerImpl();

    @Test
    void rejectsBreakOutsideLoop() {
        var result = analyzer.analyze(parser.parse(lexer.analyze("""
                { int a; break; }
                """).tokens()));
        assertTrue(result.hasErrors());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("break")));
    }

    @Test
    void acceptsBreakInsideWhile() {
        var result = analyzer.analyze(parser.parse(lexer.analyze("""
                { int i; i = 0; while (i < 3) { break; } }
                """).tokens()));
        assertTrue(result.errors().isEmpty(), () -> result.errors().toString());
    }
}

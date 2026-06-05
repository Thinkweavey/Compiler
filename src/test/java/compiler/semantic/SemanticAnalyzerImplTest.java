package compiler.semantic;

import compiler.lexer.LexerImpl;
import compiler.parser.LR1Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticAnalyzerImplTest {
    private final SemanticAnalyzerImpl analyzer = new SemanticAnalyzerImpl();
    private final LR1Parser parser = new LR1Parser();
    private final LexerImpl lexer = new LexerImpl();

    @Test
    void acceptsValidProgramFromParserActions() {
        var parse = parser.parse(lexer.analyze("{ int a; a = 1; }").tokens());
        var result = analyzer.analyze(parse);

        assertTrue(parse.accepted(), () -> String.valueOf(parse.errors()));
        assertFalse(result.hasErrors(), () -> result.errors().stream()
                .map(e -> e.message())
                .toList()
                .toString());
        assertEquals(1, result.symbols().size());
        assertEquals("a", result.symbols().get(0).name());
        assertEquals("int", result.symbols().get(0).typeName());
    }

    @Test
    void detectsDuplicateDefinitionUndefinedAndTypeMismatch() {
        SemanticResult result = analyzer.analyzeActions(java.util.List.of(
                "DECLARE a int VARIABLE",
                "DECLARE a int VARIABLE",
                "ASSIGN b int",
                "ASSIGN a bool"
        ));

        assertTrue(result.hasErrors());
        assertEquals(3, result.errors().size());
    }

    @Test
    void detectsUndefinedAfterScopeExit() {
        SemanticResult result = analyzer.analyzeActions(java.util.List.of(
                "ENTER_SCOPE",
                "DECLARE b int VARIABLE",
                "EXIT_SCOPE",
                "USE b"
        ));

        assertTrue(result.hasErrors());
        assertEquals("使用了未定义标识符: b", result.errors().get(0).message());
    }

    @Test
    void shortCircuitsWhenParserFailed() {
        var result = analyzer.analyze(new compiler.parser.ParserResult(
                false, java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of()
        ));
        assertFalse(result.hasErrors());
        assertTrue(result.symbols().isEmpty());
    }
}

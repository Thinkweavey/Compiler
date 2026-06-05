package compiler.parser;

import compiler.lexer.LexerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LR1ParserTest {
    private final LR1Parser parser = new LR1Parser();
    private final LexerImpl lexer = new LexerImpl();

    @Test
    void acceptsBasicBlock() {
        var result = parser.parse(lexer.analyze("{ int a; a = 1; }").tokens());
        assertTrue(result.accepted());
        assertFalse(result.trace().isEmpty());
    }

    @Test
    void acceptsIfElseAndWhile() {
        String source = """
                {
                  int x;
                  if (x < 0) x = 0;
                  else x = 1;
                  while (x < 10) x = x + 1;
                }
                """;
        var result = parser.parse(lexer.analyze(source).tokens());
        assertTrue(result.accepted());
    }

    @Test
    void acceptsDoWhileAndBreak() {
        String source = """
                {
                  int i;
                  i = 0;
                  do i = i + 1; while (i < 3);
                  break;
                }
                """;
        var result = parser.parse(lexer.analyze(source).tokens());
        assertTrue(result.accepted());
    }

    @Test
    void rejectsSyntaxErrorInput() {
        var result = parser.parse(lexer.analyze("{ int a; } }").tokens());
        assertFalse(result.accepted());
        assertFalse(result.errors().isEmpty());
    }

    @Test
    void parsingTableHasNoUnresolvedConflicts() {
        assertFalse(parser.parsingTable().hasConflict());
    }

    @Test
    void parsingTableIsSharedAcrossParserInstances() {
        LR1Parser another = new LR1Parser();
        assertTrue(parser.parsingTable() == another.parsingTable());
    }

    @Test
    void resolvesDanglingElseByShift() {
        String source = "{ if (a) if (b) a = 1; else a = 2; }";
        var result = parser.parse(lexer.analyze(source).tokens());
        assertTrue(result.accepted());
    }
}

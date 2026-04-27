package edu.groupname.compiler.lexer;

import edu.groupname.compiler.common.error.CompileError;
import edu.groupname.compiler.common.error.LexicalError;
import edu.groupname.compiler.common.token.Token;
import edu.groupname.compiler.common.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexerImplTest {
    private final Lexer lexer = new LexerImpl();

    @Test
    void analyzeProducesContractTokensForBasicProgram() {
        LexicalAnalyzerResult result = lexer.analyze("{ int a; a = 1; }");

        assertFalse(result.hasErrors());
        assertEquals(
                List.of("{", "int", "a", ";", "a", "=", "1", ";", "}", "<EOF>"),
                result.tokens().stream().map(Token::lexeme).toList()
        );
        assertEquals(TokenType.EOF, result.tokens().get(result.tokens().size() - 1).type());
    }

    @Test
    void analyzeSkipsWhitespaceAndComments() {
        String source = """
                // declare value
                { /* block comment */
                  int a;
                }
                """;

        LexicalAnalyzerResult result = lexer.analyze(source);

        assertFalse(result.hasErrors());
        assertEquals(
                List.of("{", "int", "a", ";", "}", "<EOF>"),
                result.tokens().stream().map(Token::lexeme).toList()
        );
    }

    @Test
    void analyzeReportsIllegalCharacterWithPosition() {
        LexicalAnalyzerResult result = lexer.analyze("{ int @a; }");

        assertTrue(result.hasErrors());
        CompileError error = result.errors().get(0);
        assertInstanceOf(LexicalError.class, error);
        assertTrue(error.message().contains("@"));
        assertEquals(1, error.position().line());
        assertEquals(7, error.position().column());
    }

    @Test
    void analyzeDistinguishesBoolKeywordAndBooleanLiterals() {
        LexicalAnalyzerResult result = lexer.analyze("bool flag; flag = true; flag = false;");
        List<Token> tokens = result.tokens();

        assertFalse(result.hasErrors());
        assertEquals(
                List.of("bool", "flag", ";", "flag", "=", "true", ";", "flag", "=", "false", ";", "<EOF>"),
                tokens.stream().map(Token::lexeme).toList()
        );
        assertEquals(TokenType.KEYWORD, tokens.get(0).type());
        assertEquals("bool", tokens.get(0).lexeme());
        assertEquals(TokenType.BOOLEAN_LITERAL, tokens.get(5).type());
        assertEquals("true", tokens.get(5).lexeme());
        assertEquals(TokenType.BOOLEAN_LITERAL, tokens.get(9).type());
        assertEquals("false", tokens.get(9).lexeme());
    }

    @Test
    void analyzeDistinguishesIntegerAndRealLiterals() {
        LexicalAnalyzerResult result = lexer.analyze("a = 1; b = 2.5;");
        List<Token> tokens = result.tokens();

        assertFalse(result.hasErrors());
        assertEquals(TokenType.INTEGER_LITERAL, tokens.get(2).type());
        assertEquals("1", tokens.get(2).lexeme());
        assertEquals(TokenType.REAL_LITERAL, tokens.get(6).type());
        assertEquals("2.5", tokens.get(6).lexeme());
    }

    @Test
    void analyzeAlwaysAppendsSingleEofPerCall() {
        LexicalAnalyzerResult first = lexer.analyze("int a;");
        LexicalAnalyzerResult second = lexer.analyze("int b;");

        assertEquals(1, first.tokens().stream().filter(token -> token.type() == TokenType.EOF).count());
        assertEquals(1, second.tokens().stream().filter(token -> token.type() == TokenType.EOF).count());
    }
}

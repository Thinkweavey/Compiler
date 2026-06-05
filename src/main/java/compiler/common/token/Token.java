package compiler.common.token;

import compiler.common.position.SourcePosition;

public record Token(TokenType type, String lexeme, SourcePosition position) {
    public static Token eof(SourcePosition position) {
        return new Token(TokenType.EOF, "<EOF>", position);
    }
}


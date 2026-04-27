package edu.groupname.compiler.common.token;

import edu.groupname.compiler.common.position.SourcePosition;

public record Token(TokenType type, String lexeme, SourcePosition position) {
    public static Token eof(SourcePosition position) {
        return new Token(TokenType.EOF, "<EOF>", position);
    }
}


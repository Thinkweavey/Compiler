package edu.groupname.compiler.lexer;

import edu.groupname.compiler.common.position.SourcePosition;
import edu.groupname.compiler.common.token.Token;

import java.util.ArrayList;
import java.util.List;

public class LexerImpl implements Lexer {
    @Override
    public LexicalAnalyzerResult analyze(String sourceCode) {
        List<Token> tokens = new ArrayList<>();
        tokens.add(Token.eof(SourcePosition.UNKNOWN));
        return new LexicalAnalyzerResult(tokens, List.of());
    }
}


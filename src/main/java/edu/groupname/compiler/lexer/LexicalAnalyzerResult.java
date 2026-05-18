package edu.groupname.compiler.lexer;

import edu.groupname.compiler.common.error.CompileError;
import edu.groupname.compiler.common.token.Token;
import edu.groupname.compiler.symbol.Symbol;

import java.util.List;

public record LexicalAnalyzerResult(List<Token> tokens, List<CompileError> errors, List<Symbol> symbols) {
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}


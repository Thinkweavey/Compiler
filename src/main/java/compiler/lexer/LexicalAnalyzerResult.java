package compiler.lexer;

import compiler.common.error.CompileError;
import compiler.common.token.Token;
import compiler.symbol.Symbol;

import java.util.List;

public record LexicalAnalyzerResult(List<Token> tokens, List<CompileError> errors, List<Symbol> symbols) {
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}


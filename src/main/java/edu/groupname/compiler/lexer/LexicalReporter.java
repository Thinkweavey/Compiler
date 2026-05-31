package edu.groupname.compiler.lexer;

import edu.groupname.compiler.common.token.Token;
import edu.groupname.compiler.common.token.TokenCategoryLabel;
import edu.groupname.compiler.common.token.TokenType;
import edu.groupname.compiler.symbol.Symbol;

import java.util.List;
import java.util.stream.Collectors;

public final class LexicalReporter {
    private LexicalReporter() {
    }

    public static List<String> formatTokenTuples(List<Token> tokens) {
        return tokens.stream()
                .filter(token -> token.type() != TokenType.EOF)
                .map(LexicalReporter::formatTuple)
                .collect(Collectors.toList());
    }

    public static String formatTuple(Token token) {
        return "(" + token.lexeme() + ", " + TokenCategoryLabel.of(token.type()) + ")";
    }

    public static List<String> formatSymbolTable(List<Symbol> symbols) {
        return symbols.stream()
                .map(LexicalReporter::formatSymbolEntry)
                .collect(Collectors.toList());
    }

    private static String formatSymbolEntry(Symbol symbol) {
        if (symbol.kind() == edu.groupname.compiler.symbol.SymbolKind.ARRAY && symbol.arrayLength() != null) {
            return "(" + symbol.name() + ", " + symbol.typeName() + ", " + symbol.kind() + ", len=" + symbol.arrayLength() + ")";
        }
        return "(" + symbol.name() + ", " + symbol.typeName() + ", " + symbol.kind() + ")";
    }
}

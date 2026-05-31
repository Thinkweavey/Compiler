package edu.groupname.compiler.lexer;

import edu.groupname.compiler.common.token.Token;
import edu.groupname.compiler.common.token.TokenType;
import edu.groupname.compiler.symbol.ScopedSymbolTable;
import edu.groupname.compiler.symbol.SymbolKind;

import java.util.List;
import java.util.Set;

/**
 * 词法阶段后处理：根据 {@code type id;} / {@code type[num] id;} 为符号表填入 int/float/bool 等类型。
 */
public final class LexicalDeclarationBinder {
    private static final Set<String> BASIC_TYPES = Set.of("int", "float", "bool");

    private LexicalDeclarationBinder() {
    }

    public static void bindDeclarations(List<Token> tokens, ScopedSymbolTable symbolTable) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type() != TokenType.KEYWORD || !BASIC_TYPES.contains(token.lexeme())) {
                continue;
            }
            String baseType = token.lexeme();
            int next = i + 1;
            if (next >= tokens.size()) {
                continue;
            }
            if (tokens.get(next).type() == TokenType.IDENTIFIER) {
                symbolTable.bindDeclaration(tokens.get(next).lexeme(), baseType, SymbolKind.VARIABLE);
                i = next;
                continue;
            }
            if (isDelimiter(tokens.get(next), "[")) {
                int lengthTokenIndex = next + 1;
                Integer arrayLength = null;
                if (lengthTokenIndex < tokens.size()
                        && tokens.get(lengthTokenIndex).type() == TokenType.INTEGER_LITERAL) {
                    arrayLength = Integer.parseInt(tokens.get(lengthTokenIndex).lexeme());
                }
                int afterBracket = skipArraySuffix(tokens, next + 1);
                if (afterBracket < tokens.size() && tokens.get(afterBracket).type() == TokenType.IDENTIFIER) {
                    String name = tokens.get(afterBracket).lexeme();
                    symbolTable.bindDeclaration(name, baseType + "[]", SymbolKind.ARRAY, arrayLength);
                    i = afterBracket;
                }
            }
        }
    }

    private static int skipArraySuffix(List<Token> tokens, int index) {
        int i = index;
        while (i < tokens.size() && tokens.get(i).type() == TokenType.INTEGER_LITERAL) {
            i++;
        }
        if (i < tokens.size() && isDelimiter(tokens.get(i), "]")) {
            return i + 1;
        }
        return index;
    }

    private static boolean isDelimiter(Token token, String lexeme) {
        return token.type() == TokenType.DELIMITER && lexeme.equals(token.lexeme());
    }
}

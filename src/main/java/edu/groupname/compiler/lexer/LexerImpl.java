package edu.groupname.compiler.lexer;

import edu.groupname.compiler.common.position.SourcePosition;
import edu.groupname.compiler.common.error.CompileError;
import edu.groupname.compiler.common.error.LexicalError;
import edu.groupname.compiler.common.token.KeywordTable;
import edu.groupname.compiler.common.token.Token;
import edu.groupname.compiler.common.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class LexerImpl implements Lexer {
    private static final String EOF_LEXEME = "<EOF>";

    @Override
    public LexicalAnalyzerResult analyze(String sourceCode) {
        List<Token> tokens = new ArrayList<>();
        List<CompileError> errors = new ArrayList<>();
        String source = sourceCode == null ? "" : sourceCode;

        int index = 0;
        int line = 1;
        int column = 1;

        while (index < source.length()) {
            char ch = source.charAt(index);

            if (Character.isWhitespace(ch)) {
                if (ch == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
                index++;
                continue;
            }

            if (ch == '/' && index + 1 < source.length()) {
                char next = source.charAt(index + 1);
                if (next == '/') {
                    index += 2;
                    column += 2;
                    while (index < source.length() && source.charAt(index) != '\n') {
                        index++;
                        column++;
                    }
                    continue;
                }
                if (next == '*') {
                    SourcePosition commentStart = new SourcePosition(line, column);
                    index += 2;
                    column += 2;
                    boolean closed = false;
                    while (index < source.length()) {
                        char current = source.charAt(index);
                        if (current == '\n') {
                            line++;
                            column = 1;
                            index++;
                            continue;
                        }
                        if (current == '*' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                            index += 2;
                            column += 2;
                            closed = true;
                            break;
                        }
                        index++;
                        column++;
                    }
                    if (!closed) {
                        errors.add(new LexicalError("Unterminated block comment", commentStart));
                    }
                    continue;
                }
            }

            SourcePosition position = new SourcePosition(line, column);

            if (isIdentifierStart(ch)) {
                int start = index;
                while (index < source.length() && isIdentifierPart(source.charAt(index))) {
                    index++;
                    column++;
                }
                String lexeme = source.substring(start, index);
                if ("true".equals(lexeme) || "false".equals(lexeme)) {
                    tokens.add(new Token(TokenType.BOOLEAN_LITERAL, lexeme, position));
                } else if (KeywordTable.isKeyword(lexeme)) {
                    tokens.add(new Token(TokenType.KEYWORD, lexeme, position));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, lexeme, position));
                }
                continue;
            }

            if (Character.isDigit(ch)) {
                int start = index;
                while (index < source.length() && Character.isDigit(source.charAt(index))) {
                    index++;
                    column++;
                }

                TokenType numberType = TokenType.INTEGER_LITERAL;
                if (index < source.length() && source.charAt(index) == '.') {
                    if (index + 1 < source.length() && Character.isDigit(source.charAt(index + 1))) {
                        numberType = TokenType.REAL_LITERAL;
                        index++;
                        column++;
                        while (index < source.length() && Character.isDigit(source.charAt(index))) {
                            index++;
                            column++;
                        }
                    } else {
                        errors.add(new LexicalError("Malformed real literal: '.' must be followed by digits", new SourcePosition(line, column)));
                    }
                }

                tokens.add(new Token(numberType, source.substring(start, index), position));
                continue;
            }

            if (index + 1 < source.length()) {
                String twoChars = source.substring(index, index + 2);
                if (isDoubleCharOperator(twoChars)) {
                    tokens.add(new Token(TokenType.OPERATOR, twoChars, position));
                    index += 2;
                    column += 2;
                    continue;
                }
            }

            if (isSingleCharOperator(ch)) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(ch), position));
                index++;
                column++;
                continue;
            }

            if (isDelimiter(ch)) {
                tokens.add(new Token(TokenType.DELIMITER, String.valueOf(ch), position));
                index++;
                column++;
                continue;
            }

            errors.add(new LexicalError("Illegal character: '" + ch + "'", position));
            index++;
            column++;
        }

        SourcePosition eofPosition = new SourcePosition(line, column);
        if (tokens.isEmpty() || tokens.get(tokens.size() - 1).type() != TokenType.EOF) {
            tokens.add(new Token(TokenType.EOF, EOF_LEXEME, eofPosition));
        }

        return new LexicalAnalyzerResult(List.copyOf(tokens), List.copyOf(errors));
    }

    private static boolean isIdentifierStart(char ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    private static boolean isIdentifierPart(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    private static boolean isDoubleCharOperator(String op) {
        return "==".equals(op)
                || "!=".equals(op)
                || "<=".equals(op)
                || ">=".equals(op)
                || "&&".equals(op)
                || "||".equals(op);
    }

    private static boolean isSingleCharOperator(char ch) {
        return ch == '+'
                || ch == '-'
                || ch == '*'
                || ch == '/'
                || ch == '='
                || ch == '<'
                || ch == '>'
                || ch == '!';
    }

    private static boolean isDelimiter(char ch) {
        return ch == '{'
                || ch == '}'
                || ch == '('
                || ch == ')'
                || ch == '['
                || ch == ']'
                || ch == ';';
    }
}


package edu.groupname.compiler.common.token;

import java.util.Set;

public final class KeywordTable {
    private static final Set<String> KEYWORDS = Set.of(
            "if", "else", "while", "do", "break", "true", "false",
            "int", "float", "bool", "program"
    );

    private KeywordTable() {
    }

    public static boolean isKeyword(String lexeme) {
        return KEYWORDS.contains(lexeme);
    }
}


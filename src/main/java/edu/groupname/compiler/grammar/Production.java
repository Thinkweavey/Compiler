package edu.groupname.compiler.grammar;

import java.util.Arrays;
import java.util.List;

public record Production(String left, List<String> right) {
    public static Production of(String left, String... rightSymbols) {
        return new Production(left, Arrays.asList(rightSymbols));
    }
}


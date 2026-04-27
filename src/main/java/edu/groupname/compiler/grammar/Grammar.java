package edu.groupname.compiler.grammar;

import java.util.List;
import java.util.Set;

public record Grammar(String startSymbol, Set<String> terminals, Set<String> nonTerminals, List<Production> productions) {
}


package edu.groupname.compiler.symbol;

import java.util.Optional;

public interface SymbolTable {
    void define(Symbol symbol);

    Optional<Symbol> resolve(String name);
}


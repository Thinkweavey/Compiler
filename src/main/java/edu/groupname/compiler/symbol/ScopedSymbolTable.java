package edu.groupname.compiler.symbol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ScopedSymbolTable implements SymbolTable {
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    @Override
    public void define(Symbol symbol) {
        symbols.put(symbol.name(), symbol);
    }

    @Override
    public Optional<Symbol> resolve(String name) {
        return Optional.ofNullable(symbols.get(name));
    }
}


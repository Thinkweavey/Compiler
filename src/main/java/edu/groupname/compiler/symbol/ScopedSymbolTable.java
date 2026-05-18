package edu.groupname.compiler.symbol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScopedSymbolTable implements SymbolTable {
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    @Override
    public void define(Symbol symbol) {
        symbols.put(symbol.name(), symbol);
    }

    /**
     * 按首次出现顺序登记标识符（词法阶段符号表）。
     */
    public void registerIdentifier(String name) {
        if (!symbols.containsKey(name)) {
            symbols.put(name, new Symbol(name, "待声明", SymbolKind.VARIABLE));
        }
    }

    public List<Symbol> symbolsInOrder() {
        return List.copyOf(symbols.values());
    }

    @Override
    public Optional<Symbol> resolve(String name) {
        return Optional.ofNullable(symbols.get(name));
    }
}


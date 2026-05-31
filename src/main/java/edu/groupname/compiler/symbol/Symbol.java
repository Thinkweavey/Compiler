package edu.groupname.compiler.symbol;

public record Symbol(String name, String typeName, SymbolKind kind, Integer arrayLength) {
    public Symbol(String name, String typeName, SymbolKind kind) {
        this(name, typeName, kind, null);
    }
}

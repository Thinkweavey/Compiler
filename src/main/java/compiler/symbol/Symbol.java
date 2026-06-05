package compiler.symbol;

public record Symbol(String name, String typeName, SymbolKind kind, Integer arrayLength, Integer scopeLevel) {
    public Symbol(String name, String typeName, SymbolKind kind) {
        this(name, typeName, kind, null, null);
    }

    public Symbol(String name, String typeName, SymbolKind kind, Integer arrayLength) {
        this(name, typeName, kind, arrayLength, null);
    }
}

package edu.groupname.compiler.symbol;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScopedSymbolTable implements SymbolTable {
    private final LinkedList<Map<String, Symbol>> scopes = new LinkedList<>();
    private final List<Symbol> definitionOrder = new LinkedList<>();

    public ScopedSymbolTable() {
        scopes.push(new LinkedHashMap<>());
    }

    @Override
    public void define(Symbol symbol) {
        Map<String, Symbol> currentScope = scopes.peek();
        if (currentScope == null) {
            throw new IllegalStateException("No active scope for define.");
        }
        currentScope.put(symbol.name(), symbol);
        definitionOrder.add(symbol);
    }

    /**
     * 按首次出现顺序登记标识符（词法阶段符号表）。
     */
    public void registerIdentifier(String name) {
        Map<String, Symbol> currentScope = scopes.peek();
        if (currentScope != null && !currentScope.containsKey(name)) {
            currentScope.put(name, new Symbol(name, "待声明", SymbolKind.VARIABLE));
            definitionOrder.add(currentScope.get(name));
        }
    }

    /**
     * 根据声明语句绑定标识符类型（词法后处理或扫描 int/float/bool id; 模式）。
     */
    public void bindDeclaration(String name, String typeName, SymbolKind kind) {
        bindDeclaration(name, typeName, kind, null);
    }

    public void bindDeclaration(String name, String typeName, SymbolKind kind, Integer arrayLength) {
        Symbol updated = new Symbol(name, typeName, kind, arrayLength);
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.put(name, updated);
                replaceInDefinitionOrder(name, updated);
                return;
            }
        }
        define(updated);
    }

    private void replaceInDefinitionOrder(String name, Symbol updated) {
        for (int i = 0; i < definitionOrder.size(); i++) {
            if (definitionOrder.get(i).name().equals(name)) {
                definitionOrder.set(i, updated);
                return;
            }
        }
        definitionOrder.add(updated);
    }

    @Override
    public Optional<Symbol> resolve(String name) {
        for (Map<String, Symbol> scope : scopes) {
            Symbol symbol = scope.get(name);
            if (symbol != null) {
                return Optional.of(symbol);
            }
        }
        return Optional.empty();
    }

    public void enterScope() {
        scopes.push(new LinkedHashMap<>());
    }

    public void exitScope() {
        if (scopes.size() <= 1) {
            return;
        }
        scopes.pop();
    }

    public boolean isDefinedInCurrentScope(String name) {
        Map<String, Symbol> currentScope = scopes.peek();
        return currentScope != null && currentScope.containsKey(name);
    }

    public List<Symbol> symbolsInOrder() {
        return List.copyOf(definitionOrder);
    }

    /** @deprecated 使用 {@link #symbolsInOrder()} */
    public List<Symbol> snapshotSymbols() {
        return symbolsInOrder();
    }
}

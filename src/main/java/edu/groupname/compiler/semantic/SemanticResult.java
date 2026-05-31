package edu.groupname.compiler.semantic;

import edu.groupname.compiler.common.error.CompileError;
import edu.groupname.compiler.ir.Quadruple;
import edu.groupname.compiler.symbol.Symbol;

import java.util.List;
import java.util.Map;

public record SemanticResult(
        List<CompileError> errors,
        List<Symbol> symbols,
        List<String> semanticActions,
        Map<String, String> expressionTypes,
        List<Quadruple> quadruples
) {
    public SemanticResult {
        errors = List.copyOf(errors);
        symbols = List.copyOf(symbols);
        semanticActions = List.copyOf(semanticActions);
        expressionTypes = Map.copyOf(expressionTypes);
        quadruples = List.copyOf(quadruples);
    }

    public SemanticResult(List<CompileError> errors) {
        this(errors, List.of(), List.of(), Map.of(), List.of());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

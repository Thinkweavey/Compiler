package edu.groupname.compiler.parser;

import edu.groupname.compiler.common.error.CompileError;
import edu.groupname.compiler.ir.Quadruple;

import java.util.List;

public record ParserResult(
        boolean accepted,
        List<ParseTraceStep> trace,
        List<CompileError> errors,
        List<Integer> reducedProductionIndexes,
        List<String> semanticActions,
        List<Quadruple> quadruples
) {
    public ParserResult {
        semanticActions = List.copyOf(semanticActions);
        quadruples = List.copyOf(quadruples);
    }
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}


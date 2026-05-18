package edu.groupname.compiler.parser;

import edu.groupname.compiler.common.error.CompileError;

import java.util.List;

public record ParserResult(
        boolean accepted,
        List<ParseTraceStep> trace,
        List<CompileError> errors,
        List<Integer> reducedProductionIndexes
) {
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}


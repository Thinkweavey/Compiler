package edu.groupname.compiler.semantic;

import edu.groupname.compiler.common.error.CompileError;

import java.util.List;

public record SemanticResult(List<CompileError> errors) {
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}


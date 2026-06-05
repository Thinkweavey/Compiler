package compiler.common.error;

import compiler.common.position.SourcePosition;

public final class SemanticError extends CompileError {
    public SemanticError(String message, SourcePosition position) {
        super(ErrorStage.SEMANTIC, message, position);
    }
}


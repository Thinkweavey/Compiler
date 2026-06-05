package compiler.common.error;

import compiler.common.position.SourcePosition;

public final class SyntaxError extends CompileError {
    public SyntaxError(String message, SourcePosition position) {
        super(ErrorStage.SYNTAX, message, position);
    }
}


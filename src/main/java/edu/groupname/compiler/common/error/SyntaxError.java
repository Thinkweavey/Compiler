package edu.groupname.compiler.common.error;

import edu.groupname.compiler.common.position.SourcePosition;

public final class SyntaxError extends CompileError {
    public SyntaxError(String message, SourcePosition position) {
        super(ErrorStage.SYNTAX, message, position);
    }
}


package edu.groupname.compiler.common.error;

import edu.groupname.compiler.common.position.SourcePosition;

public final class LexicalError extends CompileError {
    public LexicalError(String message, SourcePosition position) {
        super(ErrorStage.LEXICAL, message, position);
    }
}


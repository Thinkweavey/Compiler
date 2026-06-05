package compiler.common.error;

import compiler.common.position.SourcePosition;

public final class LexicalError extends CompileError {
    public LexicalError(String message, SourcePosition position) {
        super(ErrorStage.LEXICAL, message, position);
    }
}


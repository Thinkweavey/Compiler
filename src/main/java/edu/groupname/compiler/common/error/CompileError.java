package edu.groupname.compiler.common.error;

import edu.groupname.compiler.common.position.SourcePosition;

public class CompileError {
    private final ErrorStage stage;
    private final String message;
    private final SourcePosition position;

    public CompileError(ErrorStage stage, String message, SourcePosition position) {
        this.stage = stage;
        this.message = message;
        this.position = position;
    }

    public ErrorStage stage() {
        return stage;
    }

    public String message() {
        return message;
    }

    public SourcePosition position() {
        return position;
    }
}


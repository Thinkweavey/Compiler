package compiler.common.position;

public record SourcePosition(int line, int column) {
    public static final SourcePosition UNKNOWN = new SourcePosition(-1, -1);
}


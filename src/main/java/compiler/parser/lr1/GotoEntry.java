package compiler.parser.lr1;

public record GotoEntry(int nextState) {
    public String display() {
        return Integer.toString(nextState);
    }
}


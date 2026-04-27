package edu.groupname.compiler.parser.lr1;

public record ActionEntry(ActionType type, int target) {
    public enum ActionType {
        SHIFT,
        REDUCE,
        ACCEPT,
        ERROR
    }
}


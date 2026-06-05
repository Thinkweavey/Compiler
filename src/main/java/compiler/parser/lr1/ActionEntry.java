package compiler.parser.lr1;

public record ActionEntry(ActionType type, int target) {
    public static ActionEntry shift(int state) {
        return new ActionEntry(ActionType.SHIFT, state);
    }

    public static ActionEntry reduce(int productionIndex) {
        return new ActionEntry(ActionType.REDUCE, productionIndex);
    }

    public static ActionEntry accept() {
        return new ActionEntry(ActionType.ACCEPT, -1);
    }

    public static ActionEntry error() {
        return new ActionEntry(ActionType.ERROR, -1);
    }

    public String display() {
        return switch (type) {
            case SHIFT -> "s" + target;
            case REDUCE -> "r" + target;
            case ACCEPT -> "acc";
            case ERROR -> "err";
        };
    }

    public enum ActionType {
        SHIFT,
        REDUCE,
        ACCEPT,
        ERROR
    }
}


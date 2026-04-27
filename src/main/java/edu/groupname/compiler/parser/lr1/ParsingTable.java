package edu.groupname.compiler.parser.lr1;

import java.util.HashMap;
import java.util.Map;

public class ParsingTable {
    private final Map<String, ActionEntry> action = new HashMap<>();
    private final Map<String, GotoEntry> goTo = new HashMap<>();

    public void putAction(int state, String terminal, ActionEntry entry) {
        action.put(key(state, terminal), entry);
    }

    public void putGoto(int state, String nonTerminal, GotoEntry entry) {
        goTo.put(key(state, nonTerminal), entry);
    }

    public ActionEntry getAction(int state, String terminal) {
        return action.getOrDefault(key(state, terminal), new ActionEntry(ActionEntry.ActionType.ERROR, -1));
    }

    public GotoEntry getGoto(int state, String nonTerminal) {
        return goTo.get(key(state, nonTerminal));
    }

    private String key(int state, String symbol) {
        return state + "::" + symbol;
    }
}


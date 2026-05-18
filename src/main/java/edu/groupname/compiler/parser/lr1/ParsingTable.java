package edu.groupname.compiler.parser.lr1;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParsingTable {
    private final Map<String, ActionEntry> action = new HashMap<>();
    private final Map<String, GotoEntry> goTo = new HashMap<>();
    private final Map<String, String> conflicts = new LinkedHashMap<>();

    public void putAction(int state, String terminal, ActionEntry entry) {
        String mapKey = key(state, terminal);
        ActionEntry existing = action.get(mapKey);
        if (existing != null && !existing.equals(entry)) {
            ActionEntry resolved = resolveShiftReduceConflict(terminal, existing, entry);
            if (resolved != null) {
                action.put(mapKey, resolved);
                return;
            }
            conflicts.put(mapKey, existing.display() + " -> " + entry.display());
        }
        action.put(mapKey, entry);
    }

    /**
     * 悬空 else：对 lookahead 为 else 的 shift/reduce 冲突，优先移进（绑定最近 if）。
     */
    private static ActionEntry resolveShiftReduceConflict(String terminal, ActionEntry existing, ActionEntry incoming) {
        if (!"else".equals(terminal)) {
            return null;
        }
        boolean existingShift = existing.type() == ActionEntry.ActionType.SHIFT;
        boolean incomingShift = incoming.type() == ActionEntry.ActionType.SHIFT;
        if (existingShift && !incomingShift) {
            return existing;
        }
        if (incomingShift && !existingShift) {
            return incoming;
        }
        return null;
    }

    public void putGoto(int state, String nonTerminal, GotoEntry entry) {
        String mapKey = key(state, nonTerminal);
        GotoEntry existing = goTo.get(mapKey);
        if (existing != null && !existing.equals(entry)) {
            conflicts.put(mapKey, existing.display() + " -> " + entry.display());
        }
        goTo.put(mapKey, entry);
    }

    public ActionEntry getAction(int state, String terminal) {
        return action.getOrDefault(key(state, terminal), ActionEntry.error());
    }

    public GotoEntry getGoto(int state, String nonTerminal) {
        return goTo.get(key(state, nonTerminal));
    }

    public Map<String, ActionEntry> actionEntries() {
        return Map.copyOf(action);
    }

    public Map<String, GotoEntry> gotoEntries() {
        return Map.copyOf(goTo);
    }

    public Map<String, String> conflicts() {
        return Map.copyOf(conflicts);
    }

    public boolean hasConflict() {
        return !conflicts.isEmpty();
    }

    public int actionCount() {
        return action.size();
    }

    public int gotoCount() {
        return goTo.size();
    }

    public List<String> exportRows() {
        List<String> actionRows = action.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> formatTableRow("ACTION", entry.getKey(), entry.getValue().display()))
                .toList();
        List<String> gotoRows = goTo.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> formatTableRow("GOTO", entry.getKey(), entry.getValue().display()))
                .toList();
        List<String> conflictRows = conflicts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> formatTableRow("CONFLICT", entry.getKey(), entry.getValue()))
                .toList();
        return List.copyOf(
                List.of(
                        actionRows,
                        gotoRows,
                        conflictRows
                ).stream().flatMap(List::stream).collect(Collectors.toList())
        );
    }

    public String toPrettyString() {
        return String.join(System.lineSeparator(), exportRows());
    }

    private String key(int state, String symbol) {
        return state + "::" + symbol;
    }

    /** 使用 (状态, 符号) 形式，避免 [ ] 与方括号定界符混淆。 */
    private static String formatTableRow(String kind, String mapKey, String value) {
        int sep = mapKey.indexOf("::");
        if (sep < 0) {
            return kind + "(" + mapKey + ") = " + value;
        }
        String state = mapKey.substring(0, sep);
        String symbol = mapKey.substring(sep + 2);
        return kind + "(" + state + ", " + symbol + ") = " + value;
    }
}


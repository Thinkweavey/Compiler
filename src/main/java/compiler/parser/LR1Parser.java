package compiler.parser;

import compiler.common.error.CompileError;
import compiler.common.error.SyntaxError;
import compiler.common.position.SourcePosition;
import compiler.common.token.Token;
import compiler.grammar.Grammar;
import compiler.grammar.Production;
import compiler.ir.IrBuilder;
import compiler.ir.Quadruple;
import compiler.parser.lr1.ActionEntry;
import compiler.parser.lr1.GotoEntry;
import compiler.parser.lr1.ParsingTable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LR1Parser implements Parser {
    private static final String EPSILON = "ε";
    private static final String END_MARKER = "$";
    private static final String AUGMENTED_START = "program'";

    private final Grammar grammar;
    /** 课程文法 LR(1) 分析表只构建一次，多个 {@link LR1Parser} 实例共享。 */
    private static final ParsingTable PARSING_TABLE = buildParsingTableForGrammar(Grammar.defaultLabGrammar());
    private final ParsingTable parsingTable = PARSING_TABLE;

    public LR1Parser() {
        this(Grammar.defaultLabGrammar());
    }

    private LR1Parser(Grammar grammar) {
        this.grammar = grammar;
    }

    @Override
    public ParserResult parse(List<Token> tokens) {
        List<ParseTraceStep> trace = new ArrayList<>();
        List<CompileError> errors = new ArrayList<>();
        List<Integer> reduced = new ArrayList<>();
        List<String> semanticActions = new ArrayList<>();
        List<Quadruple> quadruples = new ArrayList<>();
        SemanticContext semanticContext = new SemanticContext();

        List<String> input = tokens.stream().map(this::mapTokenToTerminal).toList();
        int cursor = 0;

        Deque<Integer> stateStack = new ArrayDeque<>();
        Deque<String> symbolStack = new ArrayDeque<>();
        Deque<SemanticAttribute> valueStack = new ArrayDeque<>();
        stateStack.push(0);
        symbolStack.push(END_MARKER);
        valueStack.push(SemanticAttribute.empty());
        while (true) {
            int state = stateStack.peek();
            String lookahead = cursor < input.size() ? input.get(cursor) : END_MARKER;
            ActionEntry action = parsingTable.getAction(state, lookahead);
            String actionLabel = action.display();
            trace.add(new ParseTraceStep(
                    formatStateStack(stateStack),
                    formatSymbolStack(symbolStack),
                    remainingInput(input, cursor),
                    actionLabel
            ));
            if (action.type() == ActionEntry.ActionType.SHIFT) {
                if ("{".equals(lookahead)) {
                    semanticContext.enterScope();
                    semanticActions.add("ENTER_SCOPE");
                }
                if ("}".equals(lookahead)) {
                    semanticContext.exitScope();
                    semanticActions.add("EXIT_SCOPE");
                }
                if ("while".equals(lookahead) || "do".equals(lookahead)) {
                    semanticContext.ir().pushBreakTarget(semanticContext.ir().freshLabel());
                }
                symbolStack.push(lookahead);
                if (END_MARKER.equals(lookahead)) {
                    valueStack.push(SemanticAttribute.empty());
                } else {
                    Token shifted = tokens.get(cursor);
                    valueStack.push(SemanticAttribute.fromShift(shifted, lookahead));
                }
                stateStack.push(action.target());
                cursor++;
                continue;
            }
            if (action.type() == ActionEntry.ActionType.REDUCE) {
                int productionIndex = action.target();
                if (productionIndex < 0 || productionIndex >= grammar.productions().size()) {
                    SourcePosition pos = cursor < tokens.size() ? tokens.get(cursor).position() : SourcePosition.UNKNOWN;
                    errors.add(new SyntaxError("规约产生式索引非法: r" + productionIndex, pos));
                    return new ParserResult(false, trace, errors, reduced, semanticActions, quadruples);
                }
                Production p = grammar.productions().get(productionIndex);
                List<SemanticAttribute> rhsAttributes = new ArrayList<>();
                for (int i = 0; i < p.right().size(); i++) {
                    if (!symbolStack.isEmpty()) {
                        symbolStack.pop();
                    }
                    if (!valueStack.isEmpty()) {
                        rhsAttributes.add(valueStack.pop());
                    }
                    if (stateStack.size() > 1) {
                        stateStack.pop();
                    }
                }
                Collections.reverse(rhsAttributes);
                SemanticReduceHandler.ReduceOutcome outcome =
                        SemanticReduceHandler.reduce(productionIndex, rhsAttributes, semanticContext);
                semanticActions.addAll(outcome.actions());
                if (productionIndex == 0) {
                    quadruples = new ArrayList<>(outcome.attribute().code());
                }
                int gotoBase = stateStack.peek();
                GotoEntry gotoEntry = parsingTable.getGoto(gotoBase, p.left());
                if (gotoEntry == null) {
                    SourcePosition pos = cursor < tokens.size() ? tokens.get(cursor).position() : SourcePosition.UNKNOWN;
                    errors.add(new SyntaxError(
                            "缺少 GOTO 项: (" + gotoBase + ", " + p.left() + ")",
                            pos
                    ));
                    return new ParserResult(false, trace, errors, reduced, semanticActions, quadruples);
                }
                symbolStack.push(p.left());
                valueStack.push(outcome.attribute());
                stateStack.push(gotoEntry.nextState());
                reduced.add(productionIndex);
                continue;
            }
            if (action.type() == ActionEntry.ActionType.ACCEPT) {
                return new ParserResult(true, trace, List.of(), reduced, semanticActions, quadruples);
            }
            SourcePosition pos = cursor < tokens.size() ? tokens.get(cursor).position() : SourcePosition.UNKNOWN;
            errors.add(new SyntaxError("语法错误，当前符号: " + lookahead, pos));
            return new ParserResult(false, trace, errors, reduced, semanticActions, quadruples);
        }
    }

    private String mapTokenToTerminal(Token token) {
        return switch (token.type()) {
            case IDENTIFIER -> "id";
            case INTEGER_LITERAL -> "num";
            case REAL_LITERAL -> "real";
            case BOOLEAN_LITERAL -> token.lexeme();
            case KEYWORD, OPERATOR, DELIMITER -> token.lexeme();
            case EOF -> END_MARKER;
            case UNKNOWN -> token.lexeme();
        };
    }

    private String remainingInput(List<String> input, int cursor) {
        if (cursor >= input.size()) {
            return END_MARKER;
        }
        return input.subList(cursor, input.size())
                .stream()
                .collect(Collectors.joining(" "));
    }

    private String formatStateStack(Deque<Integer> stack) {
        List<Integer> states = new ArrayList<>(stack);
        Collections.reverse(states);
        return new StringBuilder("[")
                .append(states.stream().map(String::valueOf).collect(Collectors.joining(", ")))
                .append("]")
                .toString();
    }

    private String formatSymbolStack(Deque<String> stack) {
        List<String> symbols = new ArrayList<>(stack);
        Collections.reverse(symbols);
        return new StringBuilder("[")
                .append(symbols.stream().collect(Collectors.joining(", ")))
                .append("]")
                .toString();
    }

    public ParsingTable parsingTable() {
        return parsingTable;
    }

    public List<String> exportedParsingTableRows() {
        return parsingTable.exportRows();
    }

    private static ParsingTable buildParsingTableForGrammar(Grammar grammar) {
        LR1Parser builder = new LR1Parser(grammar);
        ParsingTable table = new ParsingTable();
        Map<String, Set<String>> first = builder.computeFirstSets();

        Set<LR1Item> startState = builder.closure(Set.of(new LR1Item(-1, 0, END_MARKER)), first);
        List<Set<LR1Item>> states = new ArrayList<>();
        Map<Set<LR1Item>, Integer> stateIds = new LinkedHashMap<>();
        Map<String, Integer> transitions = new HashMap<>();
        states.add(startState);
        stateIds.put(startState, 0);

        for (int i = 0; i < states.size(); i++) {
            Set<LR1Item> state = states.get(i);
            for (String symbol : grammar.symbols()) {
                Set<LR1Item> next = builder.goTo(state, symbol, first);
                if (next.isEmpty()) {
                    continue;
                }
                Integer id = stateIds.get(next);
                if (id == null) {
                    id = states.size();
                    states.add(next);
                    stateIds.put(next, id);
                }
                transitions.put(i + "::" + symbol, id);
            }
        }

        for (int i = 0; i < states.size(); i++) {
            Set<LR1Item> state = states.get(i);
            for (LR1Item item : state) {
                if (item.productionIndex() == -1 && item.dotPosition() == 1 && END_MARKER.equals(item.lookahead())) {
                    table.putAction(i, END_MARKER, ActionEntry.accept());
                    continue;
                }
                Production production = item.productionIndex() == -1
                        ? new Production(AUGMENTED_START, List.of(grammar.startSymbol()))
                        : grammar.productions().get(item.productionIndex());
                if (item.dotPosition() < production.right().size()) {
                    String nextSymbol = production.right().get(item.dotPosition());
                    Integer target = transitions.get(i + "::" + nextSymbol);
                    if (target == null) {
                        continue;
                    }
                    if (grammar.terminals().contains(nextSymbol)) {
                        table.putAction(i, nextSymbol, ActionEntry.shift(target));
                    } else if (grammar.nonTerminals().contains(nextSymbol)) {
                        table.putGoto(i, nextSymbol, new GotoEntry(target));
                    }
                } else if (item.productionIndex() >= 0) {
                    table.putAction(i, item.lookahead(), ActionEntry.reduce(item.productionIndex()));
                }
            }
        }
        return table;
    }

    private Set<LR1Item> closure(Set<LR1Item> kernel, Map<String, Set<String>> first) {
        Set<LR1Item> closure = new LinkedHashSet<>(kernel);
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<LR1Item> snapshot = new LinkedHashSet<>(closure);
            for (LR1Item item : snapshot) {
                Production p = productionOf(item.productionIndex());
                if (item.dotPosition() >= p.right().size()) {
                    continue;
                }
                String next = p.right().get(item.dotPosition());
                if (!grammar.nonTerminals().contains(next)) {
                    continue;
                }
                List<String> beta = new ArrayList<>();
                for (int i = item.dotPosition() + 1; i < p.right().size(); i++) {
                    beta.add(p.right().get(i));
                }
                beta.add(item.lookahead());
                Set<String> lookaheads = firstOfSequence(beta, first);
                for (int pi = 0; pi < grammar.productions().size(); pi++) {
                    Production candidate = grammar.productions().get(pi);
                    if (!candidate.left().equals(next)) {
                        continue;
                    }
                    for (String la : lookaheads) {
                        if (EPSILON.equals(la)) {
                            continue;
                        }
                        LR1Item newItem = new LR1Item(pi, 0, la);
                        if (closure.add(newItem)) {
                            changed = true;
                        }
                    }
                }
            }
        }
        return closure;
    }

    private Set<LR1Item> goTo(Set<LR1Item> state, String symbol, Map<String, Set<String>> first) {
        Set<LR1Item> moved = new LinkedHashSet<>();
        for (LR1Item item : state) {
            Production p = productionOf(item.productionIndex());
            if (item.dotPosition() < p.right().size() && symbol.equals(p.right().get(item.dotPosition()))) {
                moved.add(new LR1Item(item.productionIndex(), item.dotPosition() + 1, item.lookahead()));
            }
        }
        if (moved.isEmpty()) {
            return Set.of();
        }
        return closure(moved, first);
    }

    private Map<String, Set<String>> computeFirstSets() {
        Map<String, Set<String>> first = new LinkedHashMap<>();
        for (String t : grammar.terminals()) {
            first.put(t, new LinkedHashSet<>(Set.of(t)));
        }
        for (String nt : grammar.nonTerminals()) {
            first.putIfAbsent(nt, new LinkedHashSet<>());
        }
        first.put(END_MARKER, new LinkedHashSet<>(Set.of(END_MARKER)));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.productions()) {
                Set<String> lhsFirst = first.computeIfAbsent(p.left(), ignored -> new LinkedHashSet<>());
                if (p.right().isEmpty()) {
                    if (lhsFirst.add(EPSILON)) {
                        changed = true;
                    }
                    continue;
                }
                boolean allNullable = true;
                for (String symbol : p.right()) {
                    Set<String> sf = first.computeIfAbsent(symbol, ignored -> new LinkedHashSet<>());
                    for (String candidate : sf) {
                        if (!EPSILON.equals(candidate) && lhsFirst.add(candidate)) {
                            changed = true;
                        }
                    }
                    if (!sf.contains(EPSILON)) {
                        allNullable = false;
                        break;
                    }
                }
                if (allNullable && lhsFirst.add(EPSILON)) {
                    changed = true;
                }
            }
        }
        return first;
    }

    private Set<String> firstOfSequence(List<String> sequence, Map<String, Set<String>> first) {
        Set<String> result = new LinkedHashSet<>();
        if (sequence.isEmpty()) {
            result.add(EPSILON);
            return result;
        }
        boolean allNullable = true;
        for (String symbol : sequence) {
            Set<String> sf = first.getOrDefault(symbol, Set.of(symbol));
            for (String s : sf) {
                if (!EPSILON.equals(s)) {
                    result.add(s);
                }
            }
            if (!sf.contains(EPSILON)) {
                allNullable = false;
                break;
            }
        }
        if (allNullable) {
            result.add(EPSILON);
        }
        return result;
    }

    private Production productionOf(int productionIndex) {
        if (productionIndex == -1) {
            return new Production(AUGMENTED_START, List.of(grammar.startSymbol()));
        }
        return grammar.productions().get(productionIndex);
    }

    private record LR1Item(int productionIndex, int dotPosition, String lookahead) {
    }
}


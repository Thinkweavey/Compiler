package compiler.grammar;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record Grammar(String startSymbol, Set<String> terminals, Set<String> nonTerminals, List<Production> productions) {
    public static Grammar defaultLabGrammar() {
        Set<String> terminals = Set.of(
                "{", "}", "(", ")", "[", "]", ";",
                "id", "num", "real", "true", "false",
                "if", "else", "while", "do", "break",
                "int", "float", "bool",
                "=", "||", "&&", "==", "!=", "<", "<=", ">=", ">", "+", "-", "*", "/", "!",
                "$"
        );
        Set<String> nonTerminals = Set.of(
                "program", "block", "decls", "decl", "type", "basic", "stmts", "stmt",
                "loc", "boolExpr", "join", "equality", "rel", "expr", "term", "unary", "factor"
        );
        List<Production> productions = List.of(
                Production.of("program", "block"),
                Production.of("block", "{", "decls", "stmts", "}"),
                Production.of("decls", "decls", "decl"),
                Production.of("decls"),
                Production.of("decl", "type", "id", ";"),
                Production.of("type", "type", "[", "num", "]"),
                Production.of("type", "basic"),
                Production.of("basic", "int"),
                Production.of("basic", "float"),
                Production.of("basic", "bool"),
                Production.of("stmts", "stmts", "stmt"),
                Production.of("stmts"),
                Production.of("stmt", "loc", "=", "boolExpr", ";"),
                Production.of("stmt", "if", "(", "boolExpr", ")", "stmt"),
                Production.of("stmt", "if", "(", "boolExpr", ")", "stmt", "else", "stmt"),
                Production.of("stmt", "while", "(", "boolExpr", ")", "stmt"),
                Production.of("stmt", "do", "stmt", "while", "(", "boolExpr", ")", ";"),
                Production.of("stmt", "break", ";"),
                Production.of("stmt", "block"),
                Production.of("loc", "loc", "[", "boolExpr", "]"),
                Production.of("loc", "id"),
                Production.of("boolExpr", "boolExpr", "||", "join"),
                Production.of("boolExpr", "join"),
                Production.of("join", "join", "&&", "equality"),
                Production.of("join", "equality"),
                Production.of("equality", "equality", "==", "rel"),
                Production.of("equality", "equality", "!=", "rel"),
                Production.of("equality", "rel"),
                Production.of("rel", "expr", "<", "expr"),
                Production.of("rel", "expr", "<=", "expr"),
                Production.of("rel", "expr", ">=", "expr"),
                Production.of("rel", "expr", ">", "expr"),
                Production.of("rel", "expr"),
                Production.of("expr", "expr", "+", "term"),
                Production.of("expr", "expr", "-", "term"),
                Production.of("expr", "term"),
                Production.of("term", "term", "*", "unary"),
                Production.of("term", "term", "/", "unary"),
                Production.of("term", "unary"),
                Production.of("unary", "!", "unary"),
                Production.of("unary", "-", "unary"),
                Production.of("unary", "factor"),
                Production.of("factor", "(", "boolExpr", ")"),
                Production.of("factor", "loc"),
                Production.of("factor", "num"),
                Production.of("factor", "real"),
                Production.of("factor", "true"),
                Production.of("factor", "false")
        );
        return new Grammar("program", terminals, nonTerminals, productions);
    }

    public Set<String> symbols() {
        LinkedHashSet<String> symbols = new LinkedHashSet<>();
        symbols.addAll(nonTerminals);
        symbols.addAll(terminals);
        return symbols;
    }
}


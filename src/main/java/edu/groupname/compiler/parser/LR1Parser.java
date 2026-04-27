package edu.groupname.compiler.parser;

import edu.groupname.compiler.common.token.Token;

import java.util.List;

public class LR1Parser implements Parser {
    @Override
    public ParserResult parse(List<Token> tokens) {
        ParseTraceStep init = new ParseTraceStep("[0]", "[$]", "input", "TODO: SHIFT/REDUCE");
        return new ParserResult(true, List.of(init), List.of());
    }
}


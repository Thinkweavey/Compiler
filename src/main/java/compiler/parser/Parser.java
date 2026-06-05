package compiler.parser;

import compiler.common.token.Token;

import java.util.List;

public interface Parser {
    ParserResult parse(List<Token> tokens);
}


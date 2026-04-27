package edu.groupname.compiler.parser;

import edu.groupname.compiler.common.token.Token;

import java.util.List;

public interface Parser {
    ParserResult parse(List<Token> tokens);
}


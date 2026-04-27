package edu.groupname.compiler.parser;

public record ParseTraceStep(String stateStack, String symbolStack, String input, String action) {
}


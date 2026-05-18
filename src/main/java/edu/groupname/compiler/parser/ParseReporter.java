package edu.groupname.compiler.parser;

import java.util.List;
import java.util.stream.Collectors;

public final class ParseReporter {
    private ParseReporter() {
    }

    public static List<String> formatParsingTable(List<String> tableRows) {
        return tableRows;
    }

    public static List<String> formatTrace(List<ParseTraceStep> trace) {
        return trace.stream()
                .map(ParseReporter::formatStep)
                .collect(Collectors.toList());
    }

    public static String formatStep(ParseTraceStep step) {
        return String.format(
                "状态栈=%s | 符号栈=%s | 剩余输入=%s | 动作=%s",
                step.stateStack(),
                step.symbolStack(),
                step.input(),
                step.action()
        );
    }
}

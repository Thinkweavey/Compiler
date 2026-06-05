package compiler.parser;

import compiler.common.position.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SemanticActionTexts {
    private SemanticActionTexts() {
    }

    public static String format(SourcePosition position, String... tokens) {
        List<String> parts = new ArrayList<>(List.of(tokens));
        if (position != null && position != SourcePosition.UNKNOWN) {
            parts.add("@" + position.line() + ":" + position.column());
        }
        return parts.stream().collect(Collectors.joining(" "));
    }

    public static SourcePosition parsePosition(String[] parts) {
        for (String part : parts) {
            if (part.startsWith("@") && part.contains(":")) {
                String body = part.substring(1);
                int colon = body.indexOf(':');
                if (colon > 0) {
                    int line = Integer.parseInt(body.substring(0, colon));
                    int column = Integer.parseInt(body.substring(colon + 1));
                    return new SourcePosition(line, column);
                }
            }
        }
        return SourcePosition.UNKNOWN;
    }
}

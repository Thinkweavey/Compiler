package compiler.semantic;

import compiler.common.report.ReportTableFormatter;
import compiler.symbol.Symbol;
import compiler.symbol.SymbolKind;

import java.util.ArrayList;
import java.util.List;

/** 实验三：语义分析阶段符号表输出。 */
public final class SemanticReporter {
    private SemanticReporter() {
    }

    public static List<String> formatSymbolTable(List<Symbol> symbols) {
        if (symbols.isEmpty()) {
            return List.of();
        }
        List<String[]> rows = new ArrayList<>();
        int index = 1;
        for (Symbol symbol : symbols) {
            rows.add(new String[]{
                    String.valueOf(index++),
                    symbol.name(),
                    symbol.typeName(),
                    symbol.kind().name()
            });
        }
        return ReportTableFormatter.format(
                new String[]{"No", "Name", "Type", "Kind"},
                rows
        );
    }
}

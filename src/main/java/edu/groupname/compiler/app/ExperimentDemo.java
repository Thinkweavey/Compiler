package edu.groupname.compiler.app;

import edu.groupname.compiler.lexer.LexicalAnalyzerResult;
import edu.groupname.compiler.lexer.LexicalReporter;
import edu.groupname.compiler.parser.LR1Parser;
import edu.groupname.compiler.parser.ParseReporter;
import edu.groupname.compiler.parser.ParserResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 实验一、实验二演示输出：单词二元组、符号表、LR(1) 分析表、分析栈跟踪。
 */
public final class ExperimentDemo {
    private ExperimentDemo() {
    }

    public static void printReport(PipelineReport report, LR1Parser parser, String[] args) {
        String[] cliArgs = args == null ? new String[0] : args;
        LexicalAnalyzerResult lexical = report.lexicalResult();
        ParserResult syntax = report.parserResult();

        System.out.println("========== 实验一：词法分析 ==========");
        System.out.println("--- (单词名称, 单词类别) ---");
        for (String line : LexicalReporter.formatTokenTuples(lexical.tokens())) {
            System.out.println(line);
        }

        System.out.println("--- 符号表（按出现顺序） ---");
        if (lexical.symbols().isEmpty()) {
            System.out.println("(无标识符)");
        } else {
            for (String line : LexicalReporter.formatSymbolTable(lexical.symbols())) {
                System.out.println(line);
            }
        }

        if (lexical.hasErrors()) {
            System.out.println("--- 词法错误 ---");
            lexical.errors().forEach(error -> System.out.println(error.message() + " @ " + error.position()));
        }

        System.out.println();
        System.out.println("========== 实验二：LR(1) 语法分析 ==========");
        var table = parser.parsingTable();
        System.out.println("--- 语法分析表（ACTION / GOTO） ---");
        System.out.println("（ACTION " + table.actionCount() + " 项，GOTO " + table.gotoCount() + " 项；格式：KIND(状态, 符号)=动作）");
        boolean briefTable = shouldPrintBriefTable(cliArgs);
        List<String> tableRows = parser.exportedParsingTableRows().stream()
                .filter(row -> !row.startsWith("CONFLICT("))
                .toList();
        if (briefTable) {
            tableRows.stream().limit(30).forEach(System.out::println);
            if (tableRows.size() > 30) {
                System.out.println("... 省略 " + (tableRows.size() - 30) + " 行（完整表请去掉 --brief 或写入文件）");
            }
        } else {
            tableRows.forEach(System.out::println);
        }
        if (parser.parsingTable().hasConflict()) {
            System.out.println("警告：仍存在未消解冲突 -> " + parser.parsingTable().conflicts());
        }

        System.out.println("--- 分析过程（状态栈 / 符号栈 / 剩余输入 / 动作） ---");
        for (String line : ParseReporter.formatTrace(syntax.trace())) {
            System.out.println(line);
        }

        System.out.println("--- 分析结果 ---");
        System.out.println("accepted = " + syntax.accepted());
        if (syntax.hasErrors()) {
            syntax.errors().forEach(error -> System.out.println("语法错误: " + error.message() + " @ " + error.position()));
        }
    }

    private static boolean shouldPrintBriefTable(String[] args) {
        for (String arg : args) {
            if ("--brief".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    public static String loadSource(String[] args) throws Exception {
        if (args.length > 0 && !args[0].startsWith("--")) {
            return Files.readString(Path.of(args[0]));
        }
        return """
                { int a;
                  if (a >= 1) a = a + 1;
                  else a = 0;
                }
                """;
    }
}

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
        int phase = resolvePhase(cliArgs);
        LexicalAnalyzerResult lexical = report.lexicalResult();
        ParserResult syntax = report.parserResult();

        if (phase == 0 || phase == 1) {
            printExperimentOne(lexical);
        }

        if (phase == 0 || phase == 2) {
            printExperimentTwo(parser, syntax, cliArgs);
        }

        if (phase == 0 || phase == 3) {
            printExperimentThree(report, syntax);
        }
    }

    private static void printExperimentOne(LexicalAnalyzerResult lexical) {
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
    }

    private static void printExperimentTwo(LR1Parser parser, ParserResult syntax, String[] cliArgs) {
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

    private static void printExperimentThree(PipelineReport report, ParserResult syntax) {
        System.out.println();
        System.out.println("========== 实验三：语法制导翻译与中间代码 ==========");
        System.out.println("--- 语法分析过程（状态栈 / 符号栈 / 剩余输入 / 动作） ---");
        for (String line : ParseReporter.formatTrace(syntax.trace())) {
            System.out.println(line);
        }

        var semantic = report.semanticResult();
        if (semantic.hasErrors()) {
            System.out.println("--- 语义错误 ---");
            semantic.errors().forEach(error -> System.out.println(error.message()));
            System.out.println("--- 说明 ---");
            System.out.println("存在语义错误时，最终编译结果不采用下列中间代码；"
                    + "四元式仍列出，供对照语法分析阶段已生成的三地址码。");
        }

        System.out.println("--- 三地址码（四元式） ---");
        if (report.irProgram().instructions().isEmpty()) {
            System.out.println("(无中间代码)");
        } else {
            edu.groupname.compiler.ir.IrReporter.formatProgram(report.irProgram())
                    .forEach(System.out::println);
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

    /** 0 = 全部；1/2/3 = 仅输出对应实验章节（便于截图）。 */
    private static int resolvePhase(String[] args) {
        for (String arg : args) {
            if ("--exp1".equalsIgnoreCase(arg)) {
                return 1;
            }
            if ("--exp2".equalsIgnoreCase(arg)) {
                return 2;
            }
            if ("--exp3".equalsIgnoreCase(arg)) {
                return 3;
            }
        }
        return 0;
    }

    public static String loadSource(String[] args) throws Exception {
        for (String arg : args) {
            if (arg != null && !arg.startsWith("--")) {
                return Files.readString(Path.of(arg));
            }
        }
        return """
                { int a;
                  if (a >= 1) a = a + 1;
                  else a = 0;
                }
                """;
    }
}

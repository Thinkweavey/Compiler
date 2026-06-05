package compiler.ir;

import compiler.common.report.ReportTableFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中间代码输出：三地址码（6.3 节，带行号的 if/goto 形式）与四元式（6.4 节）。
 */
public final class IrReporter {
    private static final String EMPTY = "_";
    /** 三地址码指令地址：按生成顺序从 1 递增（对应 SDT 拼接后的指令序列下标 + 1）。 */
    private static final int THREE_ADDRESS_LINE_START = 1;

    private IrReporter() {
    }

    private record ConditionalJump(String left, Opcode relOp, String right, String label) {
    }

    /** 四元式 (op, arg1, arg2, result)，未用字段为 {@code _}。 */
    public static List<String> formatProgram(IRProgram program) {
        return program.instructions().stream().map(IrReporter::formatQuadruple).toList();
    }

    /**
     * 三地址码：指令地址从 1 起按生成顺序编号（规约阶段各非终结符 {@code code} 拼接后的顺序）；
     * 条件跳转 {@code if x < y goto n}，无条件 {@code goto n}，{@code n} 为目标指令地址。
     */
    public static List<String> formatThreeAddressProgram(IRProgram program) {
        List<Quadruple> raw = program.instructions();
        List<Object> units = new ArrayList<>();
        Map<String, Integer> labelToUnitIndex = new HashMap<>();

        for (int i = 0; i < raw.size(); i++) {
            Quadruple quadruple = raw.get(i);
            if (quadruple.op() == Opcode.LABEL) {
                labelToUnitIndex.put(quadruple.result(), units.size());
                continue;
            }
            if (isRelational(quadruple.op()) && i + 1 < raw.size()) {
                Quadruple next = raw.get(i + 1);
                if (next.op() == Opcode.IF_FALSE_GOTO && quadruple.result().equals(next.arg1())) {
                    units.add(new ConditionalJump(quadruple.arg1(), quadruple.op(), quadruple.arg2(), next.result()));
                    i++;
                    continue;
                }
            }
            units.add(quadruple);
        }

        List<String> lines = new ArrayList<>();
        for (int unitIndex = 0; unitIndex < units.size(); unitIndex++) {
            int lineNumber = THREE_ADDRESS_LINE_START + unitIndex;
            Object unit = units.get(unitIndex);
            String instruction;
            if (unit instanceof ConditionalJump jump) {
                instruction = "if "
                        + jump.left()
                        + " "
                        + negateRelational(jump.relOp())
                        + " "
                        + jump.right()
                        + " goto "
                        + resolveJumpTarget(jump.label(), labelToUnitIndex);
            } else {
                instruction = formatThreeAddressBody((Quadruple) unit, labelToUnitIndex);
            }
            if (!instruction.isEmpty()) {
                lines.add(lineNumber + ": " + instruction);
            }
        }
        return List.copyOf(lines);
    }

    /** 三地址码表：Addr / Instruction 列对齐。 */
    public static List<String> formatThreeAddressTable(IRProgram program) {
        List<String[]> rows = new ArrayList<>();
        for (String line : formatThreeAddressProgram(program)) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                rows.add(new String[]{
                        line.substring(0, colon).trim(),
                        line.substring(colon + 1).trim()
                });
            }
        }
        return ReportTableFormatter.format(new String[]{"Addr", "Instruction"}, rows);
    }

    /** 四元式表：No / op / arg1 / arg2 / result 列对齐。 */
    public static List<String> formatQuadrupleTable(IRProgram program) {
        List<String[]> rows = new ArrayList<>();
        int index = 1;
        for (Quadruple quadruple : program.instructions()) {
            rows.add(new String[]{
                    String.valueOf(index++),
                    quadrupleOp(quadruple.op()),
                    field(quadruple.arg1()),
                    field(quadruple.arg2()),
                    field(quadruple.result())
            });
        }
        return ReportTableFormatter.format(new String[]{"No", "op", "arg1", "arg2", "result"}, rows);
    }

    public static String formatQuadruple(Quadruple quadruple) {
        return "("
                + quadrupleOp(quadruple.op())
                + ", "
                + field(quadruple.arg1())
                + ", "
                + field(quadruple.arg2())
                + ", "
                + field(quadruple.result())
                + ")";
    }

    static String formatThreeAddressBody(Quadruple quadruple, Map<String, Integer> labelToUnitIndex) {
        return switch (quadruple.op()) {
            case LABEL -> "";
            case ASSIGN -> quadruple.result() + " = " + quadruple.arg1();
            case ADD, SUB, MUL, DIV ->
                    quadruple.result()
                            + " = "
                            + quadruple.arg1()
                            + " "
                            + arithmeticSymbol(quadruple.op())
                            + " "
                            + quadruple.arg2();
            case EQ, NE, LT, LE, GT, GE ->
                    quadruple.result()
                            + " = "
                            + quadruple.arg1()
                            + " "
                            + relationalSymbol(quadruple.op())
                            + " "
                            + quadruple.arg2();
            case ARRAY_LOAD -> quadruple.result() + " = " + quadruple.arg1() + "[" + quadruple.arg2() + "]";
            case ARRAY_STORE -> quadruple.result() + "[" + quadruple.arg2() + "] = " + quadruple.arg1();
            case GOTO -> "goto " + resolveJumpTarget(quadruple.result(), labelToUnitIndex);
            case IF_FALSE_GOTO ->
                    "if "
                            + quadruple.arg1()
                            + " == 0 goto "
                            + resolveJumpTarget(quadruple.result(), labelToUnitIndex);
        };
    }

    private static String resolveJumpTarget(String label, Map<String, Integer> labelToUnitIndex) {
        Integer targetUnit = labelToUnitIndex.get(label);
        if (targetUnit == null) {
            return EMPTY;
        }
        return String.valueOf(THREE_ADDRESS_LINE_START + targetUnit);
    }

    private static boolean isRelational(Opcode op) {
        return switch (op) {
            case EQ, NE, LT, LE, GT, GE -> true;
            default -> false;
        };
    }

    private static String negateRelational(Opcode op) {
        return switch (op) {
            case EQ -> "!=";
            case NE -> "==";
            case LT -> ">=";
            case LE -> ">";
            case GT -> "<=";
            case GE -> "<";
            default -> relationalSymbol(op);
        };
    }

    private static String quadrupleOp(Opcode op) {
        return switch (op) {
            case ASSIGN -> "=";
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case EQ -> "==";
            case NE -> "!=";
            case LT -> "<";
            case LE -> "<=";
            case GT -> ">";
            case GE -> ">=";
            case ARRAY_LOAD -> "=[]";
            case ARRAY_STORE -> "[]=";
            case IF_FALSE_GOTO -> "ifFalse";
            case GOTO -> "goto";
            case LABEL -> "label";
        };
    }

    private static String arithmeticSymbol(Opcode op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            default -> op.name();
        };
    }

    private static String relationalSymbol(Opcode op) {
        return switch (op) {
            case EQ -> "==";
            case NE -> "!=";
            case LT -> "<";
            case LE -> "<=";
            case GT -> ">";
            case GE -> ">=";
            default -> op.name();
        };
    }

    private static String field(String value) {
        return value == null || value.isEmpty() ? EMPTY : value;
    }
}

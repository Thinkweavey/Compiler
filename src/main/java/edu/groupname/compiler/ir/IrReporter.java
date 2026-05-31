package edu.groupname.compiler.ir;

import java.util.List;

public final class IrReporter {
    private IrReporter() {
    }

    public static List<String> formatProgram(IRProgram program) {
        return program.instructions().stream().map(IrReporter::formatQuadruple).toList();
    }

    public static String formatQuadruple(Quadruple quadruple) {
        return switch (quadruple.op()) {
            case LABEL -> "(" + quadruple.op() + ", , , " + quadruple.result() + ")";
            case ASSIGN -> "(" + quadruple.op() + ", " + quadruple.arg1() + ", , " + quadruple.result() + ")";
            case ARRAY_LOAD, ARRAY_STORE ->
                    "(" + quadruple.op()
                    + ", " + blank(quadruple.arg1())
                    + ", " + blank(quadruple.arg2())
                    + ", " + quadruple.result() + ")";
            case GOTO, IF_FALSE_GOTO ->
                    "(" + quadruple.op() + ", " + blank(quadruple.arg1()) + ", , " + quadruple.result() + ")";
            case EQ, NE, LT, LE, GT, GE ->
                    "(" + quadruple.op()
                    + ", " + blank(quadruple.arg1())
                    + ", " + blank(quadruple.arg2())
                    + ", " + quadruple.result() + ")";
            default -> "(" + quadruple.op()
                    + ", " + blank(quadruple.arg1())
                    + ", " + blank(quadruple.arg2())
                    + ", " + quadruple.result() + ")";
        };
    }

    private static String blank(String value) {
        return value == null || value.isEmpty() ? "" : value;
    }
}

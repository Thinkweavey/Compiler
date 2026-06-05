package compiler.ir;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/**
 * 三地址码/四元式构造辅助（临时变量、标签、break 目标）。
 */
public final class IrBuilder {
    private int tempCounter;
    private int labelCounter;
    private final Deque<String> breakTargets = new ArrayDeque<>();

    public String freshTemp() {
        return "t" + (++tempCounter);
    }

    public String freshLabel() {
        return "L" + (++labelCounter);
    }

    public void pushBreakTarget(String label) {
        breakTargets.push(label);
    }

    public void popBreakTarget() {
        if (!breakTargets.isEmpty()) {
            breakTargets.pop();
        }
    }

    public String currentBreakTarget() {
        return breakTargets.peek();
    }

    public static List<Quadruple> label(String name) {
        return List.of(new Quadruple(Opcode.LABEL, "", "", name));
    }

    public static List<Quadruple> assign(String source, String target) {
        return List.of(new Quadruple(Opcode.ASSIGN, source, "", target));
    }

    public static List<Quadruple> binop(Opcode opcode, String left, String right, String result) {
        return List.of(new Quadruple(opcode, left, right, result));
    }

    public static List<Quadruple> ifFalseGoto(String condition, String label) {
        return List.of(new Quadruple(Opcode.IF_FALSE_GOTO, condition, "", label));
    }

    public static List<Quadruple> gotoLabel(String label) {
        return List.of(new Quadruple(Opcode.GOTO, "", "", label));
    }

    /** 下标寻址：offset = index * elementSize; addr = base + offset */
    public static List<Quadruple> indexAddress(String base, String index, String elementSize, String offsetTemp, String addrTemp) {
        return List.of(
                new Quadruple(Opcode.MUL, index, elementSize, offsetTemp),
                new Quadruple(Opcode.ADD, base, offsetTemp, addrTemp)
        );
    }

    public static List<Quadruple> arrayLoad(String base, String index, String result) {
        return List.of(new Quadruple(Opcode.ARRAY_LOAD, base, index, result));
    }

    public static List<Quadruple> arrayStore(String value, String index, String base) {
        return List.of(new Quadruple(Opcode.ARRAY_STORE, value, index, base));
    }

    @SafeVarargs
    public static List<Quadruple> concat(List<Quadruple>... parts) {
        List<Quadruple> merged = new ArrayList<>();
        for (List<Quadruple> part : parts) {
            if (part != null && !part.isEmpty()) {
                merged.addAll(part);
            }
        }
        return List.copyOf(merged);
    }

    public static List<Quadruple> concatAll(Collection<List<Quadruple>> parts) {
        return concat(parts.toArray(List[]::new));
    }
}

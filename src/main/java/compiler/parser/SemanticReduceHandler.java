package compiler.parser;

import compiler.common.position.SourcePosition;
import compiler.ir.IrBuilder;
import compiler.ir.Opcode;
import compiler.ir.Quadruple;
import compiler.symbol.Symbol;
import compiler.symbol.SymbolKind;

import java.util.ArrayList;
import java.util.List;

/**
 * 语法制导翻译：在 LR 规约时生成语义动作与三地址码（实验三核心）。
 * 规约阶段维护 {@link SemanticContext} 符号表以推断标识符类型；完整语义检查在 {@link compiler.semantic.SemanticAnalyzerImpl}。
 */
public final class SemanticReduceHandler {
    private static final String ELEMENT_SIZE = "4";

    private SemanticReduceHandler() {
    }

    public record ReduceOutcome(SemanticAttribute attribute, List<String> actions) {
        public ReduceOutcome {
            actions = List.copyOf(actions);
        }

        public static ReduceOutcome passthrough(List<SemanticAttribute> rhs) {
            return new ReduceOutcome(lastMeaningful(rhs), List.of());
        }

        public static ReduceOutcome boolResult(List<SemanticAttribute> rhs, IrBuilder ir) {
            SemanticAttribute expr = lastMeaningful(rhs);
            String place = expr.place().isEmpty() ? ir.freshTemp() : expr.place();
            List<Quadruple> code = expr.code().isEmpty()
                    ? IrBuilder.assign("true", place)
                    : expr.code();
            return new ReduceOutcome(
                    SemanticAttribute.synthesized("bool", place, code, expr.position()),
                    List.of()
            );
        }

        private static SemanticAttribute lastMeaningful(List<SemanticAttribute> rhs) {
            for (int i = rhs.size() - 1; i >= 0; i--) {
                SemanticAttribute candidate = rhs.get(i);
                if (!candidate.typeName().isEmpty() || !candidate.code().isEmpty()) {
                    return candidate;
                }
            }
            return rhs.isEmpty() ? SemanticAttribute.empty() : rhs.get(rhs.size() - 1);
        }
    }

    public static ReduceOutcome reduce(int productionIndex, List<SemanticAttribute> rhs, SemanticContext ctx) {
        IrBuilder ir = ctx.ir();
        return switch (productionIndex) {
            case 0 -> reduceProgram(rhs);
            case 1 -> reduceBlock(rhs);
            case 2 -> reduceDeclsAppend(rhs);
            case 10 -> reduceStmtsAppend(rhs);
            case 11 -> ReduceOutcome.passthrough(List.of(SemanticAttribute.empty()));
            case 4 -> reduceDecl(rhs, ctx);
            case 5 -> reduceArrayType(rhs);
            case 6 -> reduceTypeBasic(rhs);
            case 7, 8, 9 -> reduceBasic(rhs);
            case 12 -> reduceAssignStmt(rhs);
            case 13 -> reduceIfStmt(rhs, ir);
            case 14 -> reduceIfElseStmt(rhs, ir);
            case 15 -> reduceWhileStmt(rhs, ir);
            case 16 -> reduceDoWhileStmt(rhs, ir);
            case 17 -> reduceBreakStmt(rhs, ir);
            case 19 -> reduceIndexedLoc(rhs, ir, ctx);
            case 20 -> reduceIdLoc(rhs, ctx);
            case 21, 23 -> reduceLogical(rhs, ir, productionIndex);
            case 25, 26, 28, 29, 30, 31 -> reduceBoolRelation(rhs, ir, productionIndex);
            case 33, 34 -> reduceArithmetic(rhs, ir);
            case 36, 37 -> reduceMulDiv(rhs, ir);
            case 39 -> reduceUnaryNot(rhs, ir);
            case 40 -> reduceUnaryMinus(rhs, ir);
            case 43 -> reduceFactorLoc(rhs, ir, ctx);
            case 44, 45, 46, 47 -> reduceFactorLiteral(rhs, productionIndex);
            default -> ReduceOutcome.passthrough(rhs);
        };
    }

    private static ReduceOutcome reduceProgram(List<SemanticAttribute> rhs) {
        return new ReduceOutcome(rhs.get(0), List.of());
    }

    private static ReduceOutcome reduceBlock(List<SemanticAttribute> rhs) {
        SemanticAttribute decls = rhs.get(1);
        SemanticAttribute stmts = rhs.get(2);
        List<Quadruple> code = IrBuilder.concat(decls.code(), stmts.code());
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), List.of());
    }

    private static ReduceOutcome reduceDeclsAppend(List<SemanticAttribute> rhs) {
        List<Quadruple> code = IrBuilder.concat(rhs.get(0).code(), rhs.get(1).code());
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), List.of());
    }

    private static ReduceOutcome reduceStmtsAppend(List<SemanticAttribute> rhs) {
        List<Quadruple> code = IrBuilder.concat(rhs.get(0).code(), rhs.get(1).code());
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), List.of());
    }

    private static ReduceOutcome reduceDecl(List<SemanticAttribute> rhs, SemanticContext ctx) {
        SemanticAttribute typeAttr = rhs.get(0);
        SemanticAttribute idAttr = rhs.get(1);
        SymbolKind kind = typeAttr.typeName().endsWith("[]") ? SymbolKind.ARRAY : SymbolKind.VARIABLE;
        String name = idAttr.lexeme();
        Integer length = parseArrayLength(typeAttr.arrayLength());
        if (!ctx.symbols().isDefinedInCurrentScope(name)) {
            ctx.symbols().define(new Symbol(name, typeAttr.typeName(), kind, length));
        }
        List<String> declareTokens = new ArrayList<>();
        declareTokens.add("DECLARE");
        declareTokens.add(name);
        declareTokens.add(typeAttr.typeName());
        declareTokens.add(kind.name());
        if (length != null) {
            declareTokens.add(String.valueOf(length));
        }
        List<String> actions = List.of(
                SemanticActionTexts.format(
                        idAttr.position(),
                        declareTokens.toArray(String[]::new)
                )
        );
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", List.of()), actions);
    }

    private static ReduceOutcome reduceArrayType(List<SemanticAttribute> rhs) {
        String baseType = rhs.get(0).typeName();
        String length = rhs.get(2).lexeme();
        SourcePosition position = preferPosition(rhs.get(0).position(), rhs.get(2).position());
        return new ReduceOutcome(
                SemanticAttribute.synthesizedType(baseType + "[]", length, position),
                List.of()
        );
    }

    private static ReduceOutcome reduceTypeBasic(List<SemanticAttribute> rhs) {
        return new ReduceOutcome(rhs.get(0), List.of());
    }

    private static ReduceOutcome reduceBasic(List<SemanticAttribute> rhs) {
        return new ReduceOutcome(rhs.get(0), List.of());
    }

    private static ReduceOutcome reduceAssignStmt(List<SemanticAttribute> rhs) {
        SemanticAttribute loc = rhs.get(0);
        SemanticAttribute value = rhs.get(2);
        String rhsType = effectiveType(value.typeName());
        List<Quadruple> code;
        List<String> actions;
        if (loc.indexed()) {
            code = IrBuilder.concat(
                    loc.code(),
                    value.code(),
                    IrBuilder.arrayStore(value.place(), loc.indexPlace(), loc.arrayBase())
            );
            actions = List.of(SemanticActionTexts.format(
                    loc.position(), "ASSIGN_INDEX", loc.arrayBase(), loc.indexPlace(), rhsType));
        } else {
            code = IrBuilder.concat(
                    value.code(),
                    IrBuilder.assign(value.place(), loc.place())
            );
            actions = List.of(SemanticActionTexts.format(
                    loc.position(), "ASSIGN", loc.lexeme(), rhsType));
        }
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), actions);
    }

    private static ReduceOutcome reduceIfStmt(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute condition = rhs.get(2);
        SemanticAttribute stmt = rhs.get(4);
        String falseLabel = ir.freshLabel();
        List<Quadruple> code = IrBuilder.concat(
                condition.code(),
                IrBuilder.ifFalseGoto(condition.place(), falseLabel),
                stmt.code(),
                IrBuilder.label(falseLabel)
        );
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), List.of());
    }

    private static ReduceOutcome reduceIfElseStmt(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute condition = rhs.get(2);
        SemanticAttribute thenStmt = rhs.get(4);
        SemanticAttribute elseStmt = rhs.get(6);
        String falseLabel = ir.freshLabel();
        String endLabel = ir.freshLabel();
        List<Quadruple> code = IrBuilder.concat(
                condition.code(),
                IrBuilder.ifFalseGoto(condition.place(), falseLabel),
                thenStmt.code(),
                IrBuilder.gotoLabel(endLabel),
                IrBuilder.label(falseLabel),
                elseStmt.code(),
                IrBuilder.label(endLabel)
        );
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), List.of());
    }

    private static ReduceOutcome reduceWhileStmt(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute condition = rhs.get(2);
        SemanticAttribute body = rhs.get(4);
        String startLabel = ir.freshLabel();
        String endLabel = ir.currentBreakTarget();
        List<Quadruple> code = IrBuilder.concat(
                IrBuilder.label(startLabel),
                condition.code(),
                IrBuilder.ifFalseGoto(condition.place(), endLabel),
                body.code(),
                IrBuilder.gotoLabel(startLabel),
                IrBuilder.label(endLabel)
        );
        ir.popBreakTarget();
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), List.of());
    }

    private static ReduceOutcome reduceDoWhileStmt(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute body = rhs.get(1);
        SemanticAttribute condition = rhs.get(4);
        String startLabel = ir.freshLabel();
        String endLabel = ir.currentBreakTarget();
        List<Quadruple> code = IrBuilder.concat(
                IrBuilder.label(startLabel),
                body.code(),
                condition.code(),
                IrBuilder.ifFalseGoto(condition.place(), endLabel),
                IrBuilder.gotoLabel(startLabel),
                IrBuilder.label(endLabel)
        );
        ir.popBreakTarget();
        return new ReduceOutcome(SemanticAttribute.synthesized("", "", code), List.of());
    }

    private static ReduceOutcome reduceBreakStmt(List<SemanticAttribute> rhs, IrBuilder ir) {
        SourcePosition position = rhs.get(0).position();
        String target = ir.currentBreakTarget();
        if (target == null || target.isEmpty()) {
            return new ReduceOutcome(
                    SemanticAttribute.synthesized("", "", List.of()),
                    List.of(SemanticActionTexts.format(position, "BREAK_INVALID"))
            );
        }
        List<Quadruple> code = IrBuilder.gotoLabel(target);
        return new ReduceOutcome(
                SemanticAttribute.synthesized("", "", code),
                List.of(SemanticActionTexts.format(position, "BREAK", target))
        );
    }

    private static ReduceOutcome reduceIndexedLoc(List<SemanticAttribute> rhs, IrBuilder ir, SemanticContext ctx) {
        SemanticAttribute base = rhs.get(0);
        SemanticAttribute index = rhs.get(2);
        String baseName = base.indexed() ? base.arrayBase() : base.lexeme();
        if (baseName.isEmpty()) {
            baseName = base.place();
        }
        String offset = ir.freshTemp();
        String addr = ir.freshTemp();
        List<Quadruple> code = IrBuilder.concat(
                base.code(),
                index.code(),
                IrBuilder.indexAddress(baseName, index.place(), ELEMENT_SIZE, offset, addr)
        );
        String elementType = lookupElementType(ctx, baseName);
        SourcePosition position = preferPosition(base.position(), index.position());
        List<String> actions = List.of(
                SemanticActionTexts.format(position, "USE", baseName),
                SemanticActionTexts.format(position, "INDEX", baseName, index.place(), index.typeName())
        );
        return new ReduceOutcome(
                new SemanticAttribute(baseName, elementType, addr, code, baseName, index.place(), position, ""),
                actions
        );
    }

    private static ReduceOutcome reduceFactorLoc(List<SemanticAttribute> rhs, IrBuilder ir, SemanticContext ctx) {
        SemanticAttribute loc = rhs.get(0);
        if (!loc.indexed()) {
            String type = lookupType(ctx, loc.lexeme());
            SemanticAttribute enriched = loc.withTypeName(type);
            return new ReduceOutcome(
                    enriched,
                    List.of(SemanticActionTexts.format(loc.position(), "USE", loc.lexeme()))
            );
        }
        String loaded = ir.freshTemp();
        List<Quadruple> code = IrBuilder.concat(
                loc.code(),
                IrBuilder.arrayLoad(loc.arrayBase(), loc.indexPlace(), loaded)
        );
        return new ReduceOutcome(
                new SemanticAttribute(loc.arrayBase(), loc.typeName(), loaded, code, loc.arrayBase(), loc.indexPlace(), loc.position(), ""),
                List.of(SemanticActionTexts.format(loc.position(), "USE", loc.arrayBase()))
        );
    }

    private static ReduceOutcome reduceIdLoc(List<SemanticAttribute> rhs, SemanticContext ctx) {
        SemanticAttribute id = rhs.get(0);
        String type = lookupType(ctx, id.lexeme());
        SemanticAttribute enriched = id.withTypeName(type);
        return new ReduceOutcome(
                enriched,
                List.of(SemanticActionTexts.format(id.position(), "USE", id.lexeme()))
        );
    }

    private static ReduceOutcome reduceLogical(List<SemanticAttribute> rhs, IrBuilder ir, int productionIndex) {
        SemanticAttribute left = rhs.get(0);
        SemanticAttribute right = rhs.get(2);
        String result = ir.freshTemp();
        SourcePosition position = preferPosition(left.position(), right.position());
        if (productionIndex == 21) {
            String evalRight = ir.freshLabel();
            String end = ir.freshLabel();
            List<Quadruple> code = IrBuilder.concat(
                    left.code(),
                    IrBuilder.ifFalseGoto(left.place(), evalRight),
                    IrBuilder.assign("true", result),
                    IrBuilder.gotoLabel(end),
                    IrBuilder.label(evalRight),
                    right.code(),
                    IrBuilder.assign(right.place(), result),
                    IrBuilder.label(end)
            );
            return exprOutcome("bool", result, code, position);
        }
        String falseLabel = ir.freshLabel();
        String end = ir.freshLabel();
        List<Quadruple> code = IrBuilder.concat(
                left.code(),
                IrBuilder.ifFalseGoto(left.place(), falseLabel),
                right.code(),
                IrBuilder.assign(right.place(), result),
                IrBuilder.gotoLabel(end),
                IrBuilder.label(falseLabel),
                IrBuilder.assign("false", result),
                IrBuilder.label(end)
        );
        return exprOutcome("bool", result, code, position);
    }

    private static ReduceOutcome reduceBoolRelation(List<SemanticAttribute> rhs, IrBuilder ir, int productionIndex) {
        SemanticAttribute left = rhs.get(0);
        SemanticAttribute right = rhs.get(2);
        String temp = ir.freshTemp();
        Opcode opcode = switch (productionIndex) {
            case 25 -> Opcode.EQ;
            case 26 -> Opcode.NE;
            case 28 -> Opcode.LT;
            case 29 -> Opcode.LE;
            case 30 -> Opcode.GE;
            case 31 -> Opcode.GT;
            default -> Opcode.EQ;
        };
        List<Quadruple> code = IrBuilder.concat(
                left.code(),
                right.code(),
                List.of(new Quadruple(opcode, left.place(), right.place(), temp))
        );
        return exprOutcome("bool", temp, code, preferPosition(left.position(), right.position()));
    }

    private static ReduceOutcome reduceArithmetic(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute left = rhs.get(0);
        SemanticAttribute operator = rhs.get(1);
        SemanticAttribute right = rhs.get(2);
        String resultType = combineNumeric(left.typeName(), right.typeName());
        String temp = ir.freshTemp();
        Opcode opcode = "-".equals(operator.lexeme()) ? Opcode.SUB : Opcode.ADD;
        List<Quadruple> code = IrBuilder.concat(
                left.code(),
                right.code(),
                IrBuilder.binop(opcode, left.place(), right.place(), temp)
        );
        return exprOutcome(resultType, temp, code, preferPosition(left.position(), right.position()));
    }

    private static ReduceOutcome reduceMulDiv(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute left = rhs.get(0);
        SemanticAttribute operator = rhs.get(1);
        SemanticAttribute right = rhs.get(2);
        String resultType = combineNumeric(left.typeName(), right.typeName());
        String temp = ir.freshTemp();
        Opcode opcode = "/".equals(operator.lexeme()) ? Opcode.DIV : Opcode.MUL;
        List<Quadruple> code = IrBuilder.concat(
                left.code(),
                right.code(),
                IrBuilder.binop(opcode, left.place(), right.place(), temp)
        );
        return exprOutcome(resultType, temp, code, preferPosition(left.position(), right.position()));
    }

    private static ReduceOutcome reduceUnaryNot(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute operand = rhs.get(1);
        String temp = ir.freshTemp();
        String falseLabel = ir.freshLabel();
        String endLabel = ir.freshLabel();
        List<Quadruple> code = IrBuilder.concat(
                operand.code(),
                IrBuilder.ifFalseGoto(operand.place(), falseLabel),
                IrBuilder.assign("false", temp),
                IrBuilder.gotoLabel(endLabel),
                IrBuilder.label(falseLabel),
                IrBuilder.assign("true", temp),
                IrBuilder.label(endLabel)
        );
        return exprOutcome("bool", temp, code, operand.position());
    }

    private static ReduceOutcome reduceUnaryMinus(List<SemanticAttribute> rhs, IrBuilder ir) {
        SemanticAttribute operand = rhs.get(1);
        String temp = ir.freshTemp();
        List<Quadruple> code = IrBuilder.concat(
                operand.code(),
                IrBuilder.binop(Opcode.SUB, "0", operand.place(), temp)
        );
        String resultType = switch (operand.typeName()) {
            case "int", "float" -> operand.typeName();
            default -> "error";
        };
        return exprOutcome(resultType, temp, code, operand.position());
    }

    private static ReduceOutcome reduceFactorLiteral(List<SemanticAttribute> rhs, int productionIndex) {
        SemanticAttribute literal = rhs.get(0);
        String type = switch (productionIndex) {
            case 44 -> "int";
            case 45 -> "float";
            case 46, 47 -> "bool";
            default -> SemanticAttribute.UNKNOWN_TYPE;
        };
        String place = literal.lexeme().isEmpty() ? literal.place() : literal.lexeme();
        return new ReduceOutcome(
                new SemanticAttribute(literal.lexeme(), type, place, List.of(), "", "", literal.position(), ""),
                List.of()
        );
    }

    private static Integer parseArrayLength(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static ReduceOutcome exprOutcome(String type, String place, List<Quadruple> code, SourcePosition position) {
        List<String> actions = new ArrayList<>();
        if (!SemanticAttribute.UNKNOWN_TYPE.equals(type) && !"error".equals(type)) {
            actions.add(SemanticActionTexts.format(position, "EXPR", place, type));
        }
        return new ReduceOutcome(SemanticAttribute.synthesized(type, place, code, position), actions);
    }

    private static String lookupType(SemanticContext ctx, String name) {
        return ctx.symbols().resolve(name)
                .map(Symbol::typeName)
                .orElse(SemanticAttribute.UNKNOWN_TYPE);
    }

    private static String lookupElementType(SemanticContext ctx, String baseName) {
        return ctx.symbols().resolve(baseName)
                .map(symbol -> {
                    if (symbol.typeName().endsWith("[]")) {
                        return symbol.typeName().substring(0, symbol.typeName().length() - 2);
                    }
                    return "error";
                })
                .orElse(SemanticAttribute.UNKNOWN_TYPE);
    }

    private static String effectiveType(String typeName) {
        if (typeName == null || typeName.isEmpty() || SemanticAttribute.UNKNOWN_TYPE.equals(typeName)) {
            return "error";
        }
        return typeName;
    }

    private static SourcePosition preferPosition(SourcePosition first, SourcePosition second) {
        if (second != null && second != SourcePosition.UNKNOWN) {
            return second;
        }
        if (first != null && first != SourcePosition.UNKNOWN) {
            return first;
        }
        return SourcePosition.UNKNOWN;
    }

    private static String combineNumeric(String left, String right) {
        if (SemanticAttribute.UNKNOWN_TYPE.equals(left)) {
            left = right;
        }
        if (SemanticAttribute.UNKNOWN_TYPE.equals(right)) {
            right = left;
        }
        if ("int".equals(left) && "int".equals(right)) {
            return "int";
        }
        if ("float".equals(left) && "float".equals(right)) {
            return "float";
        }
        if (SemanticAttribute.UNKNOWN_TYPE.equals(left) || SemanticAttribute.UNKNOWN_TYPE.equals(right)) {
            return SemanticAttribute.UNKNOWN_TYPE;
        }
        return "error";
    }
}

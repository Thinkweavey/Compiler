package compiler.parser;

import compiler.common.position.SourcePosition;
import compiler.common.token.Token;
import compiler.ir.Quadruple;

import java.util.List;

/**
 * LR 分析栈上的语义属性：类型、IR 占位符、数组访问元数据、已生成代码片段、源码位置、数组长度字面量。
 */
public record SemanticAttribute(
        String lexeme,
        String typeName,
        String place,
        List<Quadruple> code,
        String arrayBase,
        String indexPlace,
        SourcePosition position,
        String arrayLength
) {
    public static final String UNKNOWN_TYPE = "";

    public SemanticAttribute {
        code = List.copyOf(code);
        if (position == null) {
            position = SourcePosition.UNKNOWN;
        }
        if (arrayLength == null) {
            arrayLength = "";
        }
    }

    public boolean indexed() {
        return arrayBase != null && !arrayBase.isEmpty();
    }

    public SemanticAttribute withTypeName(String newTypeName) {
        return new SemanticAttribute(lexeme, newTypeName, place, code, arrayBase, indexPlace, position, arrayLength);
    }

    public static SemanticAttribute empty() {
        return new SemanticAttribute("", UNKNOWN_TYPE, "", List.of(), "", "", SourcePosition.UNKNOWN, "");
    }

    public static SemanticAttribute synthesized(String typeName, String place, List<Quadruple> code) {
        return new SemanticAttribute("", typeName, place, code, "", "", SourcePosition.UNKNOWN, "");
    }

    public static SemanticAttribute synthesized(
            String typeName,
            String place,
            List<Quadruple> code,
            SourcePosition position
    ) {
        return new SemanticAttribute("", typeName, place, code, "", "", position, "");
    }

    public static SemanticAttribute synthesizedType(String typeName, String arrayLength, SourcePosition position) {
        return new SemanticAttribute("", typeName, "", List.of(), "", "", position, arrayLength == null ? "" : arrayLength);
    }

    public static SemanticAttribute fromShift(Token token, String terminal) {
        SourcePosition position = token.position();
        return switch (terminal) {
            case "id" -> new SemanticAttribute(token.lexeme(), UNKNOWN_TYPE, token.lexeme(), List.of(), "", "", position, "");
            case "num" -> new SemanticAttribute(token.lexeme(), "int", token.lexeme(), List.of(), "", "", position, "");
            case "real" -> new SemanticAttribute(token.lexeme(), "float", token.lexeme(), List.of(), "", "", position, "");
            case "true", "false" -> new SemanticAttribute(token.lexeme(), "bool", token.lexeme(), List.of(), "", "", position, "");
            case "int", "float", "bool" -> new SemanticAttribute(terminal, terminal, "", List.of(), "", "", position, "");
            default -> new SemanticAttribute(token.lexeme(), UNKNOWN_TYPE, "", List.of(), "", "", position, "");
        };
    }
}

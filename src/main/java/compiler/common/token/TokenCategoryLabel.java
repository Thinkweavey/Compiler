package compiler.common.token;

public final class TokenCategoryLabel {
    private TokenCategoryLabel() {
    }

    public static String of(TokenType type) {
        return switch (type) {
            case KEYWORD -> "关键字";
            case IDENTIFIER -> "标识符";
            case INTEGER_LITERAL -> "整数";
            case REAL_LITERAL -> "实数";
            case BOOLEAN_LITERAL -> "布尔常数";
            case OPERATOR -> "运算符";
            case DELIMITER -> "分隔符";
            case EOF -> "文件结束";
            case UNKNOWN -> "未知";
        };
    }
}

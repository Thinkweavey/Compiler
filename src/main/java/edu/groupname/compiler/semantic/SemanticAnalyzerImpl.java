package edu.groupname.compiler.semantic;

import edu.groupname.compiler.common.error.CompileError;
import edu.groupname.compiler.common.error.SemanticError;
import edu.groupname.compiler.common.position.SourcePosition;
import edu.groupname.compiler.ir.Quadruple;
import edu.groupname.compiler.parser.ParserResult;
import edu.groupname.compiler.parser.SemanticActionTexts;
import edu.groupname.compiler.symbol.ScopedSymbolTable;
import edu.groupname.compiler.symbol.Symbol;
import edu.groupname.compiler.symbol.SymbolKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 实验三第二阶段：根据规约阶段产出的 {@link ParserResult#semanticActions()} 做符号/类型检查。
 * 三地址码在 LR 规约时由 {@link edu.groupname.compiler.parser.SemanticReduceHandler} 生成并放入
 * {@link ParserResult#quadruples()}，本类不重复生成 IR。
 */
public class SemanticAnalyzerImpl implements SemanticAnalyzer {
    @Override
    public SemanticResult analyze(ParserResult parserResult) {
        if (parserResult == null || !parserResult.accepted() || parserResult.hasErrors()) {
            return new SemanticResult(List.of());
        }
        return analyzeActions(parserResult.semanticActions(), parserResult.quadruples());
    }

    SemanticResult analyzeActions(List<String> semanticActions) {
        return analyzeActions(semanticActions, List.of());
    }

    SemanticResult analyzeActions(List<String> semanticActions, List<Quadruple> quadruples) {
        List<CompileError> errors = new ArrayList<>();
        Map<String, String> expressionTypes = new HashMap<>();
        ScopedSymbolTable symbolTable = new ScopedSymbolTable();

        for (String action : semanticActions) {
            applyAction(action, symbolTable, expressionTypes, errors);
        }

        return new SemanticResult(errors, symbolTable.symbolsInOrder(), semanticActions, expressionTypes, quadruples);
    }

    private void applyAction(
            String action,
            ScopedSymbolTable symbolTable,
            Map<String, String> expressionTypes,
            List<CompileError> errors
    ) {
        if (action == null || action.isBlank()) {
            return;
        }

        String[] parts = action.trim().split("\\s+");
        String op = parts[0].toUpperCase(Locale.ROOT);
        SourcePosition position = SemanticActionTexts.parsePosition(parts);
        switch (op) {
            case "ENTER_SCOPE" -> symbolTable.enterScope();
            case "EXIT_SCOPE" -> symbolTable.exitScope();
            case "DECLARE" -> handleDeclare(parts, symbolTable, errors, position);
            case "ASSIGN" -> handleAssign(parts, symbolTable, errors, position);
            case "ASSIGN_INDEX" -> handleAssignIndex(parts, symbolTable, errors, position);
            case "USE" -> handleUse(parts, symbolTable, errors, position);
            case "INDEX" -> handleIndex(parts, symbolTable, errors, position);
            case "BREAK_INVALID" -> errors.add(new SemanticError("break 语句只能出现在循环体内", position));
            case "BREAK" -> {
                // 合法 break，IR 已在规约阶段生成
            }
            case "EXPR" -> handleExpr(parts, expressionTypes);
            default -> {
                // 忽略未知动作
            }
        }
    }

    private void handleDeclare(
            String[] parts,
            ScopedSymbolTable symbolTable,
            List<CompileError> errors,
            SourcePosition position
    ) {
        List<String> payload = payloadTokens(parts);
        if (payload.size() < 2) {
            return;
        }
        String name = payload.get(0);
        String type = payload.get(1);
        SymbolKind kind = payload.size() >= 3 ? parseKind(payload.get(2)) : SymbolKind.VARIABLE;
        Integer arrayLength = null;
        if (payload.size() >= 4) {
            arrayLength = Integer.parseInt(payload.get(3));
        }
        if (symbolTable.isDefinedInCurrentScope(name)) {
            errors.add(new SemanticError("重复定义标识符: " + name, position));
            return;
        }
        symbolTable.define(new Symbol(name, type, kind, arrayLength));
    }

    private void handleAssign(
            String[] parts,
            ScopedSymbolTable symbolTable,
            List<CompileError> errors,
            SourcePosition position
    ) {
        List<String> payload = payloadTokens(parts);
        if (payload.isEmpty()) {
            return;
        }
        String name = payload.get(0);
        Optional<Symbol> symbol = symbolTable.resolve(name);
        if (symbol.isEmpty()) {
            errors.add(new SemanticError("使用了未定义标识符: " + name, position));
            return;
        }
        String actualType = payload.size() >= 2 ? payload.get(1) : "error";
        checkTypeMatch(name, symbol.get().typeName(), actualType, errors, position);
    }

    private void handleAssignIndex(
            String[] parts,
            ScopedSymbolTable symbolTable,
            List<CompileError> errors,
            SourcePosition position
    ) {
        List<String> payload = payloadTokens(parts);
        if (payload.size() < 1) {
            return;
        }
        String base = payload.get(0);
        Optional<Symbol> symbol = symbolTable.resolve(base);
        if (symbol.isEmpty()) {
            errors.add(new SemanticError("使用了未定义数组: " + base, position));
            return;
        }
        if (symbol.get().kind() != SymbolKind.ARRAY) {
            errors.add(new SemanticError("对非数组对象进行下标赋值: " + base, position));
        }
        if (payload.size() >= 3) {
            String elementType = elementTypeOf(symbol.get().typeName());
            checkTypeMatch(base + "[...]", elementType, payload.get(2), errors, position);
        }
    }

    private void handleIndex(
            String[] parts,
            ScopedSymbolTable symbolTable,
            List<CompileError> errors,
            SourcePosition position
    ) {
        List<String> payload = payloadTokens(parts);
        if (payload.isEmpty()) {
            return;
        }
        String base = payload.get(0);
        Optional<Symbol> symbol = symbolTable.resolve(base);
        if (symbol.isEmpty()) {
            errors.add(new SemanticError("使用了未定义标识符: " + base, position));
            return;
        }
        if (symbol.get().kind() != SymbolKind.ARRAY) {
            errors.add(new SemanticError("对非数组对象进行下标访问: " + base, position));
        }
        if (payload.size() >= 3) {
            String indexPlace = payload.get(1);
            String indexType = payload.get(2);
            if (!"int".equals(indexType) && !"bool".equals(indexType)) {
                errors.add(new SemanticError("数组下标应为整型或布尔表达式: " + base, position));
            }
            checkArrayBounds(symbol.get(), indexPlace, errors, position);
        }
    }

    private void handleUse(
            String[] parts,
            ScopedSymbolTable symbolTable,
            List<CompileError> errors,
            SourcePosition position
    ) {
        List<String> payload = payloadTokens(parts);
        if (payload.isEmpty()) {
            return;
        }
        String name = payload.get(0);
        if (symbolTable.resolve(name).isEmpty()) {
            errors.add(new SemanticError("使用了未定义标识符: " + name, position));
        }
    }

    private void handleExpr(String[] parts, Map<String, String> expressionTypes) {
        List<String> payload = payloadTokens(parts);
        if (payload.size() >= 2) {
            expressionTypes.put(payload.get(0), payload.get(1));
        }
    }

    private void checkTypeMatch(
            String name,
            String expectedType,
            String actualType,
            List<CompileError> errors,
            SourcePosition position
    ) {
        if (actualType.isEmpty() || "error".equals(actualType)) {
            errors.add(new SemanticError("赋值右侧表达式类型非法: " + name, position));
            return;
        }
        if (!expectedType.equals(actualType)) {
            errors.add(new SemanticError(
                    "赋值类型不匹配: " + name + ", 期望 " + expectedType + ", 实际 " + actualType,
                    position
            ));
        }
    }

    private static List<String> payloadTokens(String[] parts) {
        List<String> payload = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].startsWith("@")) {
                payload.add(parts[i]);
            }
        }
        return payload;
    }

    private void checkArrayBounds(Symbol symbol, String indexPlace, List<CompileError> errors, SourcePosition position) {
        if (symbol.arrayLength() == null || !isNonNegativeIntegerLiteral(indexPlace)) {
            return;
        }
        int index = Integer.parseInt(indexPlace);
        if (index < 0 || index >= symbol.arrayLength()) {
            errors.add(new SemanticError(
                    "数组下标越界: " + symbol.name() + "[" + index + "], 长度为 " + symbol.arrayLength(),
                    position
            ));
        }
    }

    private static boolean isNonNegativeIntegerLiteral(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String elementTypeOf(String arrayTypeName) {
        if (arrayTypeName.endsWith("[]")) {
            return arrayTypeName.substring(0, arrayTypeName.length() - 2);
        }
        return arrayTypeName;
    }

    private SymbolKind parseKind(String rawKind) {
        try {
            return SymbolKind.valueOf(rawKind.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return SymbolKind.VARIABLE;
        }
    }
}

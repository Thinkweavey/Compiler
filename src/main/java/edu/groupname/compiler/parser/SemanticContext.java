package edu.groupname.compiler.parser;

import edu.groupname.compiler.ir.IrBuilder;
import edu.groupname.compiler.symbol.ScopedSymbolTable;

/**
 * 语法分析阶段的语法制导上下文：IR 构造器 + 与语义阶段同步的作用域符号表（用于表达式类型推断）。
 */
public final class SemanticContext {
    private final IrBuilder irBuilder = new IrBuilder();
    private final ScopedSymbolTable symbolTable = new ScopedSymbolTable();

    public IrBuilder ir() {
        return irBuilder;
    }

    public ScopedSymbolTable symbols() {
        return symbolTable;
    }

    public void enterScope() {
        symbolTable.enterScope();
    }

    public void exitScope() {
        symbolTable.exitScope();
    }
}

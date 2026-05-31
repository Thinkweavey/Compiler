package edu.groupname.compiler.ir;

import edu.groupname.compiler.semantic.SemanticResult;

/**
 * 实验三第三阶段：输出规约阶段已生成的四元式（不在此重复语法制导）。
 */
public class IRGeneratorImpl implements IRGenerator {
    @Override
    public IRProgram generate(SemanticResult semanticResult) {
        if (semanticResult == null) {
            return new IRProgram(java.util.List.of());
        }
        return new IRProgram(semanticResult.quadruples());
    }
}

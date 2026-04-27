package edu.groupname.compiler.ir;

import edu.groupname.compiler.semantic.SemanticResult;

import java.util.List;

public class IRGeneratorImpl implements IRGenerator {
    @Override
    public IRProgram generate(SemanticResult semanticResult) {
        return new IRProgram(List.of());
    }
}


package compiler.ir;

import compiler.semantic.SemanticResult;

public interface IRGenerator {
    IRProgram generate(SemanticResult semanticResult);
}


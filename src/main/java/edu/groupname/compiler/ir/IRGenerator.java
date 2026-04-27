package edu.groupname.compiler.ir;

import edu.groupname.compiler.semantic.SemanticResult;

public interface IRGenerator {
    IRProgram generate(SemanticResult semanticResult);
}


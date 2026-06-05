package compiler.ir;

public enum Opcode {
    ASSIGN,
    ADD,
    SUB,
    MUL,
    DIV,
    /** 计算 base[index] 的地址或取值：result = load(base, index) */
    ARRAY_LOAD,
    /** 写入 base[index] = value：arg1=value, arg2=index, result=base */
    ARRAY_STORE,
    EQ,
    NE,
    LT,
    LE,
    GT,
    GE,
    IF_FALSE_GOTO,
    GOTO,
    LABEL
}


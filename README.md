# compiler-lab-java

福州大学编译原理课程 — 编译系统综合实验（三阶段合并）。

## 功能

1. 词法分析：二元组、注释/空白过滤、错误定位、符号表  
2. LR(1) 语法分析：完整分析表 + 分析栈跟踪  
3. 语法制导翻译：完整分析过程 + 三地址码（四元式）+ 语义错误检查  

## Quick Start

**Windows 若中文乱码**：先切 UTF-8 控制台，或直接用下方 `run.ps1`（已内置 `chcp 65001`）。

```powershell
cd Compiler
mvn clean test

# 推荐：单文件运行（自动 UTF-8）
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 samples/sample1_basic.src

# 仅实验一 / 二 / 三
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 samples/sample1_basic.src -Phase 1

# 实验三额外输出：语义动作、表达式类型
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 samples/sample1_basic.src -Phase 3 -Semantic
```

- 导出测试输出到 `docs/test-output/`：`powershell -ExecutionPolicy Bypass -File .\scripts\export-test-outputs.ps1`  

## Package Overview

- `compiler.app` — `CompilerApplication`, `CompilerPipeline`, `ExperimentDemo`  
- `compiler.lexer` — `LexerImpl`, `LexicalDeclarationBinder`, `SourceBuffer`  
- `compiler.parser` — `LR1Parser`, `SemanticReduceHandler`  
- `compiler.semantic` — `SemanticAnalyzerImpl`  
- `compiler.ir` — `IRGeneratorImpl`, `IrReporter`  
- `compiler.symbol` — `ScopedSymbolTable`  

## Samples

| 文件 | 说明 |
|------|------|
| sample1_basic.src | 基础赋值 |
| sample2_if_else.src | if-else |
| sample3_loop.src | while / do-while |
| sample4_break_error.src | 语义错误：break 越界 |
| sample5_array.src | 数组 |
| sample6_float.src | float / 实数 |
| sample7_bool.src | bool / 条件 |
| sample8_lexical_error.src | 词法错误：非法字符 `@` |

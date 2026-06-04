# compiler-lab-java

福州大学编译原理课程 — 编译系统综合实验（三阶段合并）。

## 功能

1. 词法分析：二元组、注释/空白过滤、错误定位、带类型的符号表  
2. LR(1) 语法分析：完整分析表 + 分析栈跟踪  
3. 语法制导翻译：完整分析过程 + 三地址码（四元式）+ 语义错误检查  

## Quick Start

**Windows 若中文乱码**：先切 UTF-8 控制台，或直接用下方 `run.ps1`（已内置 `chcp 65001`）。

```powershell
cd Compiler
mvn clean test

# 推荐：单文件运行（自动 UTF-8，避免乱码）
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 samples/sample1_basic.src

# 或手动设置编码后再运行
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
mvn -q exec:java "-Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication" "-Dexec.args=samples/sample1_basic.src"

# 实验三额外输出：语义动作列表、语义分析后符号表、表达式类型
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 samples/sample1_basic.src -Phase 3 -Semantic
# 或：mvn -q exec:java ... "-Dexec.args=samples/sample1_basic.src --exp3 --semantic"
```

- 全量演示（完整分析表，无 `--brief`）：`powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1`  
- 导出测试输出到 `docs/test-output/`：`.\scripts\export-test-outputs.ps1`  
- 实验报告与测试记录：`docs/实验报告.md`、`docs/实验测试记录.md`  
- 文法对照说明：`docs/文法与任务书对照说明.md`  

## Package Overview

- `app` — `CompilerApplication`, `CompilerPipeline`, `ExperimentDemo`  
- `lexer` — `LexerImpl`, `LexicalDeclarationBinder`, `SourceBuffer`  
- `parser` — `LR1Parser`, `SemanticReduceHandler`  
- `semantic` — `SemanticAnalyzerImpl`  
- `ir` — `IRGeneratorImpl`, `IrReporter`  
- `symbol` — `ScopedSymbolTable`  

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

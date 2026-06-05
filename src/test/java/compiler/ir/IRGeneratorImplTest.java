package compiler.ir;

import compiler.app.CompilerPipeline;
import compiler.lexer.LexerImpl;
import compiler.parser.LR1Parser;
import compiler.semantic.SemanticAnalyzerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IRGeneratorImplTest {
  private final CompilerPipeline pipeline = new CompilerPipeline(
          new LexerImpl(),
          new LR1Parser(),
          new SemanticAnalyzerImpl(),
          new IRGeneratorImpl()
  );

  @Test
  void generatesQuadruplesForAssignmentAndIf() {
    var report = pipeline.compile("""
            {
              int a;
              a = 1;
              if (a >= 1) a = a + 1;
            }
            """);
    assertFalse(report.semanticResult().hasErrors());
    assertFalse(report.irProgram().instructions().isEmpty());
    assertTrue(report.irProgram().instructions().stream()
            .anyMatch(q -> q.op() == Opcode.ASSIGN));
    assertTrue(report.irProgram().instructions().stream()
            .anyMatch(q -> q.op() == Opcode.IF_FALSE_GOTO || q.op() == Opcode.GE));
  }

  @Test
  void generatesArrayAccessAndControlFlow() {
    var report = pipeline.compile("""
            {
              int[10] arr;
              arr[0] = 1;
              while (arr[0] < 5) arr[0] = arr[0] + 1;
            }
            """);
    assertTrue(report.parserResult().accepted(), () -> String.valueOf(report.parserResult().errors()));
    assertFalse(report.semanticResult().hasErrors(), () -> report.semanticResult().errors().toString());
    var ops = report.irProgram().instructions().stream().map(q -> q.op()).toList();
    assertTrue(ops.contains(Opcode.ARRAY_STORE), () -> "ops=" + ops);
    assertTrue(ops.contains(Opcode.MUL));
    assertTrue(ops.contains(Opcode.ADD));
    assertTrue(ops.contains(Opcode.IF_FALSE_GOTO));
  }

  @Test
  void reportsThreeAddressAndQuadrupleSeparately() {
    var report = pipeline.compile("""
            {
              int a;
              a = 1;
            }
            """);
    var instructions = report.irProgram().instructions();
    assertFalse(instructions.isEmpty());
    var threeAddressTable = IrReporter.formatThreeAddressTable(report.irProgram());
    var quadrupleTable = IrReporter.formatQuadrupleTable(report.irProgram());
    assertTrue(threeAddressTable.get(0).contains("Addr"));
    assertTrue(threeAddressTable.stream().anyMatch(line -> line.contains(" = ")));
    assertTrue(quadrupleTable.get(0).contains("op"));
    assertTrue(quadrupleTable.stream().anyMatch(line -> line.trim().startsWith("1")));
    assertFalse(threeAddressTable.equals(quadrupleTable));
    assertEquals(instructions.size(), report.irProgram().instructions().size());
  }

  @Test
  void generatesQuadruplesForFloatAndBoolPrograms() {
    var floatReport = pipeline.compile("""
            { float x; x = 2.5; x = x + 1.0; }
            """);
    assertFalse(floatReport.semanticResult().hasErrors());
    assertTrue(floatReport.irProgram().instructions().stream().anyMatch(q -> q.op() == Opcode.ASSIGN));

    var boolReport = pipeline.compile("""
            { bool flag; flag = true; if (flag) flag = false; }
            """);
    assertFalse(boolReport.semanticResult().hasErrors());
    assertTrue(boolReport.irProgram().instructions().stream()
            .anyMatch(q -> q.op() == Opcode.IF_FALSE_GOTO));
  }
}

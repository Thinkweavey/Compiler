# -*- coding: utf-8 -*-
"""Patch gen_exp3_report.py from exp2 template into experiment 3 report generator."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / "gen_exp3_report.py"

PARSE_EXP3 = r'''
def parse_exp3_section(txt_path: Path) -> dict:
    lines = read_output_text(txt_path).splitlines()
    in_exp3 = False
    trace_rows: list[str] = []
    ir_rows: list[str] = []
    sem_errors: list[str] = []
    note_lines: list[str] = []
    in_ir = in_sem = False
    for line in lines:
        if line.startswith("========== \u5b9e\u9a8c\u4e09"):
            in_exp3 = True
            continue
        if not in_exp3:
            continue
        if line.strip() == "--- \u4e09\u5730\u5740\u7801\uff08\u56db\u5143\u5f0f\uff09 ---":
            in_ir, in_sem = True, False
            continue
        if line.strip() == "--- \u8bed\u4e49\u9519\u8bef ---":
            in_sem, in_ir = True, False
            continue
        if line.strip().startswith("--- \u8bf4\u660e ---"):
            in_sem = False
            continue
        if "\u5206\u6790\u8fc7\u7a0b" in line and "\u72b6\u6001\u6808" in line:
            in_ir = in_sem = False
            continue
        if line.startswith("\u72b6\u6001\u6808="):
            trace_rows.append(line.strip())
        elif in_ir and line.strip().startswith("("):
            ir_rows.append(line.strip())
        elif in_sem and line.strip() and not line.startswith("---"):
            sem_errors.append(line.strip())
        elif "\u5b58\u5728\u8bed\u4e49\u9519\u8bef" in line:
            note_lines.append(line.strip())
    return {
        "trace_head": trace_rows[:5],
        "trace_tail": trace_rows[-3:] if len(trace_rows) > 5 else [],
        "ir": ir_rows,
        "sem_errors": sem_errors,
        "note": "\n".join(note_lines),
    }
'''

COVER_DOC = 'COVER_DOC = DESKTOP / "\\u5b9e\\u9a8c\\u62a5\\u544a\\u5c01\\u9762.doc"\nBODY_TEMP = DESKTOP / "_exp3_body_temp.docx"\n'

EXP3_SAMPLES = """EXP3_SAMPLES = [
    ("sample2_if_else", "\\u6d4b\\u8bd5\\u7ec4 1\\uff1aif-else \\u4e0e\\u56db\\u5143\\u5f0f\\uff08sample2_if_else\\uff09"),
    ("sample3_loop", "\\u6d4b\\u8bd5\\u7ec4 2\\uff1awhile/do-while\\uff08sample3_loop\\uff09"),
    ("sample5_array", "\\u6d4b\\u8bd5\\u7ec4 3\\uff1a\\u6570\\u7ec4\\u4e0b\\u6807\\uff08sample5_array\\uff09"),
]
"""

API_ROWS = """API_ROWS = [
    ["CompilerPipeline", "PipelineReport compile(String source)", "sourceCode", "lex/parser/semantic/IR", "\\u5168\\u7a0b\\u7f16\\u8bd1"],
    ["LR1Parser", "ParserResult parse(List<Token>)", "tokens", "trace+quadruples", "LR(1)+\\u8bed\\u6cd5\\u5bfc\\u7ffb\\u8bd1"],
    ["SemanticReduceHandler", "ReduceOutcome reduce(...)", "productionIndex,rhs,ctx", "attr+actions", "\\u89c4\\u7ea6\\u8bed\\u4e49\\u52a8\\u4f5c"],
    ["SemanticAnalyzerImpl", "SemanticResult analyze(ParserResult)", "parserResult", "errors+quads", "\\u8bed\\u4e49\\u68c0\\u67e5"],
    ["IRGeneratorImpl", "IRProgram generate(SemanticResult)", "semanticResult", "IRProgram", "\\u8f93\\u51fa\\u56db\\u5143\\u5f0f"],
    ["IrReporter", "List<String> formatProgram(IRProgram)", "program", "formatted lines", "\\u4e09\\u5730\\u5740\\u7801\\u683c\\u5f0f"],
    ["ScopedSymbolTable", "void bindDeclaration(...)", "name,type,kind", "void", "\\u7b26\\u53f7\\u8868\\u7ed1\\u5b9a"],
    ["ExperimentDemo", "printReport(...)", "PipelineReport", "void", "\\u5b9e\\u9a8c\\u4e09\\u6f14\\u793a"],
]
"""

CALL_GRAPH = r'''
def generate_call_graph(path: Path) -> None:
    _setup_cjk_font()
    fig, ax = plt.subplots(figsize=(13, 9))
    ax.set_xlim(0, 13)
    ax.set_ylim(0, 9)
    ax.axis("off")
    nodes = [
        (6.5, 8.2, 3.4, 0.55, "CompilerApplication.main"),
        (6.5, 6.9, 3.0, 0.55, "CompilerPipeline\n.compile"),
        (2.0, 5.4, 2.3, 0.5, "LexerImpl\n.analyze"),
        (6.5, 5.4, 2.4, 0.5, "LR1Parser\n.parse"),
        (10.8, 5.4, 2.5, 0.5, "SemanticAnalyzer\nImpl.analyze"),
        (6.5, 4.0, 3.2, 0.5, "SemanticReduceHandler\n.reduce"),
        (6.5, 2.6, 2.8, 0.5, "IRGeneratorImpl\n.generate"),
        (10.8, 2.6, 2.4, 0.5, "IrReporter\n.formatProgram"),
        (10.8, 7.5, 2.4, 0.45, "ExperimentDemo\n.printReport"),
    ]
    for x, y, w, h, t in nodes:
        _draw_box(ax, x, y, w, h, t)
    _arrow(ax, 6.5, 7.9, 6.5, 7.15)
    _arrow(ax, 6.5, 6.65, 2.0, 5.65)
    _arrow(ax, 6.5, 6.65, 6.5, 5.65)
    _arrow(ax, 6.5, 6.65, 10.8, 5.65)
    _arrow(ax, 6.5, 5.15, 6.5, 4.25)
    _arrow(ax, 6.5, 3.75, 6.5, 2.85)
    _arrow(ax, 6.5, 2.35, 10.8, 2.85)
    _arrow(ax, 10.8, 7.25, 6.5, 7.15, rad=0.12)
    ax.set_title("\u56fe1  \u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\u6a21\u5757\u51fd\u6570\u8c03\u7528\u5173\u7cfb\u56fe", fontsize=12, pad=10)
    fig.tight_layout()
    fig.savefig(path, dpi=180, bbox_inches="tight", facecolor="white")
    plt.close(fig)
'''

FLOW_CHART = r'''
def generate_flow_chart(path: Path) -> None:
    _setup_cjk_font()
    fig, ax = plt.subplots(figsize=(8.0, 12.5))
    ax.set_xlim(0, 8.0)
    ax.set_ylim(0, 12.5)
    ax.axis("off")
    cx, sx = 2.4, 5.5
    _draw_box(ax, cx, 11.5, 4.2, 0.55, "\u6e90\u7a0b\u5e8f\u5b57\u7b26\u4e32")
    _draw_box(ax, cx, 10.3, 4.2, 0.55, "LexerImpl.analyze\n\u2192 Token \u5e8f\u5217")
    _draw_box(ax, cx, 9.1, 4.2, 0.7, "LR1Parser.parse\n\u89c4\u7ea6\u65f6 SemanticReduceHandler")
    _draw_diamond(ax, cx, 7.5, 2.8, 1.0, "\u8bed\u6cd5\n\u6210\u529f?")
    _draw_box(ax, cx, 6.0, 4.2, 0.65, "SemanticAnalyzerImpl\n\u8bed\u4e49\u68c0\u67e5")
    _draw_diamond(ax, cx, 4.5, 2.8, 1.0, "\u8bed\u4e49\n\u9519\u8bef?")
    _draw_box(ax, cx, 3.0, 4.2, 0.65, "IrReporter.formatProgram\n\u8f93\u51fa\u56db\u5143\u5f0f")
    _draw_box(ax, cx, 1.6, 4.2, 0.55, "PipelineReport / \u63a7\u5236\u53f0")
    side = (
        "\u89c4\u7ea6\u65f6\u751f\u6210\uff1a\n"
        "ASSIGN/ADD/GE/\nIF_FALSE_GOTO/\nGOTO/LABEL/\nARRAY_LOAD..."
    )
    _draw_box(ax, sx, 8.8, 2.8, 1.8, side, fc="#F5F5F5", ec="#666666", fs=8)
    _arrow(ax, cx, 11.25, cx, 10.6)
    _arrow(ax, cx, 10.05, cx, 9.45)
    _arrow(ax, cx + 1.2, 9.1, sx - 1.4, 9.0)
    _arrow(ax, cx, 8.75, cx, 8.0)
    _arrow(ax, cx, 7.0, cx, 6.35)
    _arrow(ax, cx, 5.65, cx, 4.95)
    _arrow(ax, cx, 4.0, cx, 3.35)
    _arrow(ax, cx, 2.65, cx, 1.9)
    ax.text(cx + 1.5, 7.5, "\u5426", fontsize=8)
    ax.text(cx + 1.5, 4.5, "\u662f", fontsize=8)
    ax.set_title("\u56fe2  CompilerPipeline \u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\u6d41\u7a0b", fontsize=12, pad=10)
    fig.tight_layout()
    fig.savefig(path, dpi=180, bbox_inches="tight", facecolor="white")
    plt.close(fig)
'''

MERGE_COVER = r'''
def merge_with_cover(cover_path: Path, body_path: Path, out_path: Path) -> bool:
    if not cover_path.exists():
        print(f"Cover missing: {cover_path}", file=sys.stderr)
        return False
    try:
        import win32com.client
        word = win32com.client.Dispatch("Word.Application")
        word.Visible = False
        doc = word.Documents.Open(str(cover_path.resolve()))
        end = doc.Content
        end.Collapse(0)
        end.InsertBreak(7)
        end.InsertFile(str(body_path.resolve()))
        doc.SaveAs2(str(out_path.resolve()), FileFormat=16)
        doc.Close(False)
        word.Quit()
        return True
    except Exception as exc:
        print(f"merge cover: {exc}", file=sys.stderr)
        return False
'''

BUILD_DOC = r'''
def build_document():
    e = _e1()
    Document = e.Document
    WD_ALIGN_PARAGRAPH = e.WD_ALIGN_PARAGRAPH
    Cm = e.Cm
    generate_call_graph(IMG_CALL)
    generate_flow_chart(IMG_FLOW)
    doc = Document()
    sec = doc.sections[0]
    sec.top_margin = sec.bottom_margin = Cm(2.5)
    sec.left_margin = Cm(2.8)
    sec.right_margin = Cm(2.5)
    t0 = doc.add_paragraph()
    t0.alignment = WD_ALIGN_PARAGRAPH.CENTER
    e.set_run_font(t0.add_run("\u5b9e\u9a8c\u4e09\uff1a\u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\u4e0e\u4e2d\u95f4\u4ee3\u7801\u5b9e\u9a8c"), "\u9ed1\u4f53", 16, True)
    doc.add_paragraph()

    e.add_heading(doc, "\u4e00\u3001\u5b9e\u9a8c\u76ee\u7684\u4e0e\u4efb\u52a1", 1)
    e.add_para(doc, "\u901a\u8fc7\u8bed\u6cd5\u5236\u5bfc\u6280\u672f\u5728 LR(1) \u81ea\u5e95\u5411\u8bed\u6cd5\u5206\u6790\u8fc7\u7a0b\u4e2d\u751f\u6210\u4e09\u5730\u5740\u7801\uff08\u56db\u5143\u5f0f\uff09\uff1b\u8f93\u51fa\u8bed\u6cd5\u5206\u6790\u8fc7\u7a0b\u4e0e\u4e2d\u95f4\u4ee3\u7801\u3002\u53c2\u8003\u6559\u6750\u56fe 6-36\u30016-37\u30016-43\u30016-46\u3002", indent=True)
    e.add_para(doc, "\u6587\u6cd5\u4e2d Loc\u2192loc[bool]|id\uff1b\u5de5\u7a0b bool \u4e3a boolExpr\u3002\u5b9e\u73b0\u5305\uff1aparser\u3001semantic\u3001ir\u3001symbol\u3002", indent=True)

    e.add_heading(doc, "\u4e8c\u3001\u529f\u80fd\u63cf\u8ff0", 1)
    e.add_bullets(doc, [
        "\u8f93\u51fa\u8bed\u6cd5\u5206\u6790\u8fc7\u7a0b\uff1a\u6bcf\u6b65\u201c\u72b6\u6001\u6808 | \u7b26\u53f7\u6808 | \u5269\u4f59\u8f93\u5165 | \u52a8\u4f5c\u201d\uff08\u4e0e\u5b9e\u9a8c\u4e8c\u76f8\u540c trace\uff09\u3002",
        "\u8f93\u51fa\u4e09\u5730\u5740\u7801\uff1a\u56db\u5143\u5f0f (op, arg1, arg2, result)\uff0c\u5982 ASSIGN\u3001ADD\u3001GE\u3001IF_FALSE_GOTO\u3001GOTO\u3001LABEL\u3001ARRAY_LOAD \u7b49\u3002",
        "\u8bed\u4e49\u9519\u8bef\u65f6\u8f93\u51fa\u9519\u8bef\u4fe1\u606f\uff1b\u4ecd\u5217\u51fa\u8bed\u6cd5\u9636\u6bb5\u5df2\u751f\u6210\u7684\u56db\u5143\u5f0f\u4f9b\u5bf9\u7167\u3002",
        "ExperimentDemo \u6253\u5370\u300c\u5b9e\u9a8c\u4e09\uff1a\u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\u4e0e\u4e2d\u95f4\u4ee3\u7801\u300d\u5c0f\u8282\u3002",
    ])

    e.add_heading(doc, "\u4e09\u3001\u7a0b\u5e8f\u7ed3\u6784\u63cf\u8ff0", 1)
    e.add_heading(doc, "3.1 \u51fd\u6570/\u65b9\u6cd5\u8bf4\u660e", 2)
    e.add_table(doc, ["\u7c7b", "\u51fd\u6570\u539f\u578b", "\u53c2\u6570\u542b\u4e49", "\u8fd4\u56de\u503c", "\u529f\u80fd"], API_ROWS, [2.0, 3.0, 3.2, 2.0, 2.8])
    e.add_heading(doc, "3.2 \u51fd\u6570\u8c03\u7528\u5173\u7cfb\u56fe", 2)
    e.add_picture(doc, IMG_CALL, "\u56fe1  \u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\u6a21\u5757\u51fd\u6570\u8c03\u7528\u5173\u7cfb\u56fe")
    e.add_heading(doc, "3.3 \u7a0b\u5e8f\u603b\u4f53\u6267\u884c\u6d41\u7a0b\u56fe", 2)
    e.add_picture(doc, IMG_FLOW, "\u56fe2  CompilerPipeline \u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\u6d41\u7a0b")

    e.add_heading(doc, "\u56db\u3001\u7b26\u53f7\u8868\u7684\u8bbe\u8ba1\u548c\u7ed3\u6784", 1)
    e.add_para(doc, "\u672c\u9636\u6bb5\u4f7f\u7528\u5206\u5c42\u7b26\u53f7\u8868\u652f\u6301\u8bed\u4e49\u5206\u6790\u4e0e\u7c7b\u578b\u7ed1\u5b9a\uff08\u4efb\u52a1\u4e66\u201c\u7b26\u53f7\u8868\u7684\u8bbe\u8ba1\u548c\u7ed3\u6784\u201d\uff09\u3002", indent=True)
    e.add_heading(doc, "4.1 ScopedSymbolTable", 2)
    e.add_bullets(doc, [
        "scopes\uff1a\u94fe\u5f0f\u6808\u5b58\u50a8\u5404\u5c42\u4f5c\u7528\u57df\u7684 name\u2192Symbol \u6620\u5c04\u3002",
        "definitionOrder\uff1a\u6309\u9996\u6b21\u51fa\u73b0\u987a\u5e8f\u8bb0\u5f55\u7b26\u53f7\uff08\u4f9b\u6253\u5370/\u68c0\u67e5\uff09\u3002",
        "bindDeclaration\uff1a\u58f0\u660e\u89c4\u7ea6\u65f6\u5c06 id \u7ed1\u5b9a\u4e3a int/float/bool \u6216\u6570\u7ec4\u7c7b\u578b\u3002",
    ])
    e.add_heading(doc, "4.2 SemanticContext", 2)
    e.add_table(doc, ["\u5b57\u6bb5", "\u542b\u4e49", "\u793a\u4f8b"], [
        ["symbolTable", "ScopedSymbolTable \u5b9e\u4f8b", "decl \u89c4\u7ea6\u65f6 bind"],
        ["ir", "IrBuilder", "\u751f\u6210 t1,L1 \u7b49"],
        ["quadruples", "List<Quadruple>", "\u7d2f\u79ef\u56db\u5143\u5f0f"],
    ], [2.2, 3.5, 3.3])
    e.add_para(doc, "Symbol(name, typeName, kind, arrayLength)\uff1bkind \u4e3a VARIABLE \u6216 ARRAY\u3002", indent=True)

    e.add_heading(doc, "\u4e94\u3001\u8bed\u6cd5\u5236\u5bfc\u7b97\u6cd5\u6982\u8981", 1)
    e.add_bullets(doc, [
        "S-\u5c5e\u6027\u8bed\u6cd5\u5236\u5bfc\uff1a\u6bcf\u6b21 reduce \u8c03\u7528 SemanticReduceHandler\uff0c\u5408\u6210 SemanticAttribute\uff08place/code/type\uff09\u3002",
        "stmt \u89c4\u7ea6\uff1a\u751f\u6210\u6761\u4ef6\u8df3\u8f6c\u4e0e\u8d4b\u503c\u56db\u5143\u5f0f\uff1bwhile/do-while \u751f\u6210 LABEL \u4e0e\u56de\u8fb9\u3002",
        "expr \u89c4\u7ea6\uff1a\u7b97\u672f/\u5173\u7cfb\u8fd0\u7b97\u5199\u5165\u4e34\u65f6\u53d8\u91cf tn\uff1b\u903b\u8f91\u8fd0\u7b97\u77ed\u8def\u6c42\u503c\u3002",
        "\u5b8c\u6574\u8bed\u4e49\u68c0\u67e5\u7531 SemanticAnalyzerImpl \u5728\u5206\u6790\u540e\u6267\u884c\uff08\u7c7b\u578b\u3001break \u4f4d\u7f6e\u7b49\uff09\u3002",
    ])

    e.add_heading(doc, "\u516d\u3001\u7a0b\u5e8f\u5173\u952e\u6e90\u4ee3\u7801", 1)
    e.add_para(doc, "\u4ee5\u4e0b\u6458\u81ea\u5de5\u7a0b\uff0c\u884c\u53f7\u4e0e\u4ed3\u5e93\u4e00\u81f4\u3002", indent=True)
    for title, rel, s, en in [
        ("6.1 SemanticReduceHandler.reduce", "edu/groupname/compiler/parser/SemanticReduceHandler.java", 55, 120),
        ("6.2 LR1Parser reduce hook", "edu/groupname/compiler/parser/LR1Parser.java", 95, 135),
        ("6.3 IrBuilder", "edu/groupname/compiler/ir/IrBuilder.java", 1, 57),
        ("6.4 IrReporter", "edu/groupname/compiler/ir/IrReporter.java", 1, 39),
        ("6.5 SemanticAnalyzerImpl", "edu/groupname/compiler/semantic/SemanticAnalyzerImpl.java", 1, 55),
        ("6.6 IRGeneratorImpl", "edu/groupname/compiler/ir/IRGeneratorImpl.java", 1, 16),
        ("6.7 ScopedSymbolTable", "edu/groupname/compiler/symbol/ScopedSymbolTable.java", 1, 55),
    ]:
        e.add_heading(doc, title, 2)
        e.add_code_block(doc, java_lines(rel, s, en))

    e.add_heading(doc, "\u4e03\u3001\u6d4b\u8bd5\u6570\u636e\u4e0e\u8fd0\u884c\u7ed3\u679c\uff08\u22653 \u7ec4\uff09", 1)
    e.add_para(doc, "\u6765\u81ea docs/test-output\uff08export-test-outputs.ps1 \u5bfc\u51fa\u7684\u5b9e\u9a8c\u4e09\u8282\uff09\u3002", indent=True)
    for base, title in EXP3_SAMPLES:
        data = parse_exp3_section(TEST_OUTPUT_DIR / f"{base}.txt")
        e.add_heading(doc, title, 2)
        e.add_para(doc, f"\u8f93\u5165\uff08samples/{base}.src\uff09\uff1a", bold=True)
        e.add_code_block(doc, read_sample_source(base))
        e.add_para(doc, "\u8bed\u6cd5\u5206\u6790\u8fc7\u7a0b\uff08\u524d\u6bb5 \u2026 \u672b\u6bb5\uff09\uff1a", bold=True)
        trace = "\n".join(data["trace_head"])
        if data["trace_tail"]:
            trace += "\n...\n" + "\n".join(data["trace_tail"])
        e.add_code_block(doc, trace)
        e.add_para(doc, "\u4e09\u5730\u5740\u7801\uff08\u56db\u5143\u5f0f\uff09\uff1a", bold=True)
        e.add_code_block(doc, "\n".join(data["ir"]) if data["ir"] else "(\u65e0)")
        if data["sem_errors"]:
            e.add_para(doc, "\u8bed\u4e49\u9519\u8bef\uff1a", bold=True)
            e.add_code_block(doc, "\n".join(data["sem_errors"]))
        e.add_para(doc, "\u7ed3\u8bba\uff1a\u8bed\u6cd5\u5206\u6790\u901a\u8fc7\uff0c\u56db\u5143\u5f0f\u5df2\u751f\u6210\u3002", indent=True)

    e.add_heading(doc, "\u516b\u3001\u5b9e\u9a8c\u603b\u7ed3", 1)
    e.add_bullets(doc, [
        "\u5b8c\u6210\u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\uff1a\u5728 LR \u89c4\u7ea6\u65f6\u751f\u6210\u56db\u5143\u5f0f\u5e76\u8f93\u51fa\u5206\u6790\u8fc7\u7a0b\u3002",
        "\u652f\u6301\u8d4b\u503c\u3001if-else\u3001while/do-while\u3001\u6570\u7ec4\u4e0b\u6807\u7b49\u8bed\u53e5\u7684\u4e09\u5730\u5740\u7801\u3002",
        "\u7b26\u53f7\u8868\u5728\u58f0\u660e\u89c4\u7ea6\u4e0e\u8bed\u4e49\u5206\u6790\u4e2d\u7ef4\u62a4\u7c7b\u578b\u4fe1\u606f\uff0c\u4e3a\u7efc\u5408\u7f16\u8bd1\u5668\u6253\u4e0b\u57fa\u7840\u3002",
    ])
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    e.set_run_font(p.add_run("\u6559\u6750\uff1a\u7f16\u8bd1\u539f\u7406\uff08\u7b2c 2 \u7248\uff09\uff0cAlfred V. Aho \u7b49\u8457\uff0c\u8d75\u5efa\u534e\u7b49\u8bd1\uff0c\u673a\u68b0\u5de5\u4e1a\u51fa\u7248\u793e"), size=10)
    return doc
'''

MAIN = r'''
def main() -> int:
    _setup_cjk_font()
    DESKTOP.mkdir(parents=True, exist_ok=True)
    doc = build_document()
    doc.save(str(BODY_TEMP))
    print(f"Body: {BODY_TEMP}")
    if merge_with_cover(COVER_DOC, BODY_TEMP, OUTPUT_DOCX):
        print(f"Saved: {OUTPUT_DOCX}")
    else:
        doc.save(str(OUTPUT_DOCX))
        print(f"Saved (no cover merge): {OUTPUT_DOCX}")
    if save_as_doc(OUTPUT_DOCX, OUTPUT_DOC):
        print(f"Saved: {OUTPUT_DOC}")
    for p in (IMG_CALL, IMG_FLOW, BODY_TEMP):
        try:
            p.unlink(missing_ok=True)
        except OSError:
            pass
    return 0
'''


def replace_block(text: str, start_marker: str, end_marker: str, new_block: str) -> str:
    i = text.find(start_marker)
    j = text.find(end_marker, i)
    if i < 0 or j < 0:
        raise ValueError(f"markers not found: {start_marker} {end_marker}")
    return text[:i] + new_block.strip() + "\n\n" + text[j:]


def main() -> None:
    text = TARGET.read_text(encoding="utf-8")
    # remove exp2-only blocks
    for marker in ["SYNTAX_ERROR_SOURCE", "SYNTAX_ERROR_TRACE", "SYNTAX_ERROR_RESULT"]:
        if marker in text:
            start = text.find(marker)
            end = text.find("\n\n", start)
            if end > start:
                text = text[:start] + text[end + 2 :]

    text = text.replace("EXP2_SAMPLES", "EXP3_SAMPLES_OLD_REMOVE")
    if "EXP3_SAMPLES_OLD_REMOVE" in text:
        i = text.find("EXP3_SAMPLES_OLD_REMOVE")
        j = text.find("]", i) + 1
        text = text[:i] + EXP3_SAMPLES + text[j:]

    if "COVER_DOC" not in text:
        text = text.replace(
            "DESKTOP = Path",
            COVER_DOC + "DESKTOP = Path",
            1,
        )

    text = replace_block(text, "def parse_exp3_section", "_e1_mod = None", PARSE_EXP3)

    text = replace_block(
        text,
        "def generate_call_graph",
        "def build_document",
        CALL_GRAPH + "\n" + FLOW_CHART + "\n" + API_ROWS + "\n",
    )

    if "def merge_with_cover" not in text:
        text = text.replace("def save_as_doc", MERGE_COVER + "\ndef save_as_doc")

    text = replace_block(text, "def build_document():", "def save_as_doc", BUILD_DOC)

    text = replace_block(text, "def main() -> int:", 'if __name__ == "__main__":', MAIN)

    text = text.replace("Build Experiment 3 Word report by reusing exp1 docx helpers.", "Build Experiment 3 SDT report.")
    text = "".join(c for c in text if c in "\t\n\r" or ord(c) >= 32)
    compile(text, str(TARGET), "exec")
    TARGET.write_text(text, encoding="utf-8")
    print("patched", TARGET)


if __name__ == "__main__":
    main()

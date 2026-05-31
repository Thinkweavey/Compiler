# -*- coding: utf-8 -*-
"""Build Experiment 2 Word report by reusing exp1 docx helpers."""
from __future__ import annotations

import importlib.util
import re
import sys
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.patches import FancyArrowPatch, FancyBboxPatch

PROJECT_ROOT = Path(__file__).resolve().parent.parent
SCRIPTS_DIR = Path(__file__).resolve().parent
JAVA_ROOT = PROJECT_ROOT / "src" / "main" / "java"
TEST_OUTPUT_DIR = PROJECT_ROOT / "docs" / "test-output"
SAMPLES_DIR = PROJECT_ROOT / "samples"
DESKTOP = Path(r"c:\Users\Cc\Desktop")
OUTPUT_DOCX = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.docx"
OUTPUT_DOC = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.doc"
IMG_CALL = DESKTOP / "_exp2_call_graph.png"
IMG_FLOW = DESKTOP / "_exp2_flow_chart.png"

EXP2_SAMPLES = [
    ("sample1_basic", "\u6d4b\u8bd5\u7ec4 1\uff1a\u57fa\u7840\u58f0\u660e\uff08sample1_basic\uff09"),
    ("sample2_if_else", "\u6d4b\u8bd5\u7ec4 2\uff1aif-else\uff08sample2_if_else\uff09"),
    ("sample3_loop", "\u6d4b\u8bd5\u7ec4 3\uff1awhile / do-while\uff08sample3_loop\uff09"),
]
SYNTAX_ERROR_SOURCE = "{ int a; } }"
SYNTAX_ERROR_TRACE = [
    "\u72b6\u6001\u6808=[0, 3, 4, 6, 22, 38] | \u7b26\u53f7\u6808=[$, {, decls, type, id, ;] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=r4",
    "\u72b6\u6001\u6808=[0, 3, 4, 8] | \u7b26\u53f7\u6808=[$, {, decls, decl] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=r2",
    "\u72b6\u6001\u6808=[0, 3, 4] | \u7b26\u53f7\u6808=[$, {, decls] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=r11",
    "\u72b6\u6001\u6808=[0, 3, 4, 5] | \u7b26\u53f7\u6808=[$, {, decls, stmts] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=s21",
    "\u72b6\u6001\u6808=[0, 3, 4, 5, 21] | \u7b26\u53f7\u6808=[$, {, decls, stmts, }] | \u5269\u4f59\u8f93\u5165=} $ | \u52a8\u4f5c=err",
]
SYNTAX_ERROR_RESULT = (
    "accepted = false\n"
    "\u8bed\u6cd5\u9519\u8bef: \u8bed\u6cd5\u9519\u8bef\uff0c\u5f53\u524d\u7b26\u53f7: } "
    "@ SourcePosition[line=1, column=12]"
)


def _load_exp1():
    spec = importlib.util.spec_from_file_location(
        "exp1_helpers", SCRIPTS_DIR / "generate_exp1_word.py"
    )
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def java_lines(rel: str, start: int, end: int) -> str:
    return "\n".join(
        (JAVA_ROOT / rel).read_text(encoding="utf-8").splitlines()[start - 1 : end]
    )


def read_sample_source(base: str) -> str:
    return (SAMPLES_DIR / f"{base}.src").read_text(encoding="utf-8").rstrip()


def read_output_text(path: Path) -> str:
    raw = path.read_bytes()
    if raw.startswith(b"\xff\xfe"):
        return raw.decode("utf-16-le")
    if raw.startswith(b"\xfe\xff"):
        return raw.decode("utf-16-be")
    if raw.startswith(b"\xef\xbb\xbf"):
        return raw.decode("utf-8-sig")
    return raw.decode("utf-8")


def parse_exp2_section(txt_path: Path) -> dict:
    lines = read_output_text(txt_path).splitlines()
    in_exp2 = False
    in_table = in_trace = False
    table_summary, table_rows, trace_rows, accepted = "", [], [], ""
    for line in lines:
        if line.startswith("========== \u5b9e\u9a8c\u4e8c"):
            in_exp2 = True
            continue
        if in_exp2 and line.startswith("========== \u5b9e\u9a8c\u4e09"):
            break
        if not in_exp2:
            continue
        if "ACTION" in line and "GOTO" in line and "\u9879" in line:
            table_summary = line.strip()
            in_table = True
            continue
        if in_table and line.startswith("ACTION("):
            if len(table_rows) < 12:
                table_rows.append(line.strip())
            continue
        if "\u5206\u6790\u8fc7\u7a0b" in line and "\u72b6\u6001\u6808" in line:
            in_trace, in_table = True, False
            continue
        if in_trace and line.startswith("\u72b6\u6001\u6808="):
            trace_rows.append(line.strip())
        if line.strip().startswith("accepted"):
            accepted = line.strip()
            in_trace = False
        if in_trace and line.startswith("\u8bed\u6cd5\u9519\u8bef"):
            accepted += "\n" + line.strip()
    return {
        "table_summary": table_summary,
        "table_rows": table_rows,
        "trace_head": trace_rows[:6],
        "trace_tail": trace_rows[-3:] if len(trace_rows) > 6 else [],
        "accepted": accepted,
    }


_e1_mod = None


def _e1():
    global _e1_mod
    if _e1_mod is None:
        _e1_mod = _load_exp1()
    return _e1_mod


def _setup_cjk_font() -> None:
    _e1()._setup_cjk_font()


def _draw_box(ax, x, y, w, h, text, fc="#E8F4FC", ec="#2E75B6", fs=9):
    _e1()._draw_box(ax, x, y, w, h, text, fc=fc, ec=ec, fs=fs)


def _draw_diamond(ax, x, y, w, h, text, fc="#FFF4E5", ec="#C55A11"):
    _e1()._draw_diamond(ax, x, y, w, h, text, fc=fc, ec=ec)


def _arrow(ax, x1, y1, x2, y2, text=None, rad=0.0):
    style = f"arc3,rad={rad}" if rad else "arc3,rad=0.0"
    ax.add_patch(
        FancyArrowPatch(
            (x1, y1),
            (x2, y2),
            arrowstyle="->",
            mutation_scale=12,
            linewidth=1.0,
            color="#444444",
            connectionstyle=style,
        )
    )
    if text:
        ax.text((x1 + x2) / 2, (y1 + y2) / 2 + 0.12, text, ha="center", va="bottom", fontsize=8)


def generate_call_graph(path: Path) -> None:
    _setup_cjk_font()
    fig, ax = plt.subplots(figsize=(12, 8.5))
    ax.set_xlim(0, 12)
    ax.set_ylim(0, 8.5)
    ax.axis("off")
    nodes = [
        (6.0, 7.5, 3.2, 0.55, "CompilerApplication.main"),
        (2.5, 6.0, 2.4, 0.5, "CompilerPipeline\n.compile"),
        (6.0, 6.0, 2.4, 0.5, "LR1Parser.parse"),
        (9.5, 6.0, 2.6, 0.5, "ExperimentDemo\n.printReport"),
        (2.5, 4.5, 2.2, 0.5, "LexerImpl.analyze"),
        (9.5, 4.8, 2.8, 0.45, "exportedParsing\nTableRows"),
        (9.5, 3.6, 2.6, 0.45, "ParseReporter\n.formatTrace"),
        (1.0, 2.2, 2.2, 0.45, "buildParsingTable\n(static)"),
        (3.2, 2.2, 1.9, 0.45, "computeFirstSets"),
        (5.2, 2.2, 1.5, 0.45, "closure"),
        (7.0, 2.2, 1.4, 0.45, "goTo"),
        (8.8, 2.2, 2.0, 0.45, "getAction / getGoto"),
    ]
    for x, y, w, h, t in nodes:
        _draw_box(ax, x, y, w, h, t)
    _arrow(ax, 6.0, 7.2, 2.5, 6.25)
    _arrow(ax, 6.0, 7.2, 6.0, 6.25)
    _arrow(ax, 6.0, 7.2, 9.5, 6.25)
    _arrow(ax, 2.5, 5.75, 2.5, 4.75)
    _arrow(ax, 6.0, 5.75, 1.0, 2.45)
    _arrow(ax, 6.0, 5.75, 8.8, 2.45)
    _arrow(ax, 1.0, 1.95, 3.2, 1.95)
    _arrow(ax, 3.2, 1.95, 5.2, 1.95)
    _arrow(ax, 5.2, 1.95, 7.0, 1.95)
    _arrow(ax, 9.5, 5.75, 9.5, 5.05)
    _arrow(ax, 9.5, 5.75, 9.5, 3.85)
    ax.set_title("\u56fe1  LR(1) \u8bed\u6cd5\u5206\u6790\u6a21\u5757\u51fd\u6570\u8c03\u7528\u5173\u7cfb\u56fe", fontsize=12, pad=10)
    fig.tight_layout()
    fig.savefig(path, dpi=180, bbox_inches="tight", facecolor="white")
    plt.close(fig)


def generate_flow_chart(path: Path) -> None:
    _setup_cjk_font()
    fig, ax = plt.subplots(figsize=(7.8, 12))
    ax.set_xlim(0, 7.8)
    ax.set_ylim(0, 12)
    ax.axis("off")
    cx, bx = 2.5, 5.9
    _draw_box(ax, cx, 11.0, 4.0, 0.6, "\u8f93\u5165\uff1aList<Token>")
    _draw_box(ax, cx, 9.8, 4.0, 0.75, "\u521d\u59cb\u5316 stateStack/symbolStack\npush(0), push($)")
    _draw_diamond(ax, cx, 8.4, 3.0, 1.0, "\u67e5 ACTION\n(state, lookahead)")
    _draw_box(ax, cx, 6.9, 4.0, 0.55, "\u8bb0\u5f55 ParseTraceStep")
    _draw_box(ax, cx, 5.7, 4.0, 0.55, "\u8fd4\u56de ParserResult")
    actions = (
        "\u52a8\u4f5c\u5206\u652f\uff1a\n"
        "\u00b7 SHIFT\uff1a\u538b\u5165\u7b26\u53f7\u4e0e\u72b6\u6001\n"
        "\u00b7 REDUCE\uff1a\u5f39\u6808\u3001GOTO\n"
        "\u00b7 ACCEPT\uff1a\u5206\u6790\u6210\u529f\n"
        "\u00b7 ERROR\uff1aSyntaxError"
    )
    _draw_box(ax, bx, 8.1, 3.3, 2.35, actions, fc="#F5F5F5", ec="#666666", fs=8)
    _arrow(ax, cx, 10.7, cx, 10.15)
    _arrow(ax, cx, 9.45, cx, 8.9)
    _arrow(ax, cx + 1.55, 8.4, bx - 1.65, 8.1)
    _arrow(ax, bx, 6.9, cx - 1.55, 8.4 + 0.55, rad=-0.22)
    ax.text(cx + 0.2, 7.9, "\u5faa\u73af", fontsize=8)
    _arrow(ax, cx, 8.4 - 0.55, cx, 6.9 + 0.35)
    ax.text(cx + 0.2, 7.5, "\u7ed3\u675f", fontsize=8)
    _arrow(ax, cx, 6.9 - 0.35, cx, 5.7 + 0.35)
    ax.set_title("\u56fe2  LR1Parser.parse \u603b\u4f53\u6267\u884c\u6d41\u7a0b\u56fe", fontsize=12, pad=10)
    fig.tight_layout()
    fig.savefig(path, dpi=180, bbox_inches="tight", facecolor="white")
    plt.close(fig)


API_ROWS = [
    ["Parser", "ParserResult parse(List<Token> tokens)", "tokens\uff1a\u8bcd\u6cd5 Token \u5e8f\u5217", "ParserResult", "\u8bed\u6cd5\u5206\u6790\u63a5\u53e3"],
    ["LR1Parser", "ParserResult parse(List<Token> tokens)", "stateStack\u3001symbolStack\u3001cursor", "accepted\u3001trace\u3001errors", "LR(1) \u9a71\u52a8\u5206\u6790"],
    ["LR1Parser", "static ParsingTable buildParsingTableForGrammar(Grammar g)", "\u8bfe\u7a0b\u6587\u6cd5", "ParsingTable", "\u6784\u9020 ACTION/GOTO"],
    ["LR1Parser", "Set<LR1Item> closure(...)", "FIRST\u96c6\u3001\u9879\u76ee\u6838", "\u95ed\u5305\u9879\u76ee\u96c6", "LR(1) \u95ed\u5305"],
    ["LR1Parser", "Set<LR1Item> goTo(...)", "\u72b6\u6001\u3001\u6587\u6cd5\u7b26\u53f7", "GOTO \u9879\u76ee\u96c6", "\u72b6\u6001\u8f6c\u79fb"],
    ["ParsingTable", "ActionEntry getAction(int state, String terminal)", "\u72b6\u6001\u3001\u7ec8\u7ed3\u7b26", "ActionEntry", "\u67e5\u79fb\u8fdb/\u89c4\u7ea6/\u63a5\u53d7"],
    ["ParsingTable", "GotoEntry getGoto(int state, String nonTerminal)", "\u72b6\u6001\u3001\u975e\u7ec8\u7ed3\u7b26", "GotoEntry", "\u89c4\u7ea6\u540e GOTO"],
    ["ParseReporter", "List<String> formatTrace(List<ParseTraceStep>)", "trace \u5217\u8868", "List<String>", "\u5206\u6790\u6808\u8f93\u51fa"],
    ["Grammar", "static Grammar defaultLabGrammar()", "\u65e0", "Grammar", "\u5b9e\u9a8c\u6587\u6cd5"],
    ["ExperimentDemo", "void printReport(...)", "parserResult", "void", "\u6253\u5370\u5206\u6790\u8868\u4e0e trace"],
]


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
    for _ in range(3):
        doc.add_paragraph()
    t = doc.add_paragraph()
    t.alignment = WD_ALIGN_PARAGRAPH.CENTER
    e.set_run_font(t.add_run("\u7f16\u8bd1\u7cfb\u7edf\u8bbe\u8ba1\u5b9e\u8df5\u5b9e\u9a8c\u62a5\u544a"), "\u9ed1\u4f53", 22, True)
    doc.add_paragraph()
    t2 = doc.add_paragraph()
    t2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    e.set_run_font(t2.add_run("\u5b9e\u9a8c\u4e8c\uff1aLR(1) \u8bed\u6cd5\u5206\u6790\u7a0b\u5e8f\u5b9e\u9a8c"), "\u9ed1\u4f53", 18, True)
    doc.add_paragraph()
    for label in [
        "\u5b66    \u53f7\uff1a",
        "\u59d3    \u540d\uff1a",
        "\u5e74    \u7ea7\uff1a",
        "\u5b66    \u9662\uff1a",
        "\u4e13    \u4e1a\uff1a",
    ]:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        e.set_run_font(p.add_run(f"{label}  ________________"), size=14)
    e.add_table(
        doc,
        ["\u5c0f\u7ec4\u6210\u5458", "\u5b66\u53f7", "\u5206\u5de5"],
        [["", "", "LR(1) \u8bed\u6cd5\u5206\u6790\u3001\u5b9e\u9a8c\u62a5"], ["", "", ""]],
    )
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    e.set_run_font(p.add_run("\u5b9e\u9a8c\u65f6\u95f4\uff1a2025\u20142026 \u5b66\u5e74\u7b2c\u4e8c\u5b66\u671f"), size=12)
    doc.add_page_break()

    e.add_heading(doc, "\u4e00\u3001\u5b9e\u9a8c\u76ee\u7684\u4e0e\u4efb\u52a1", 1)
    e.add_para(
        doc,
        "\u6839\u636e\u8bfe\u7a0b\u6587\u6cd5\u7f16\u5236 LR(1) \u5206\u6790\u7a0b\u5e8f\uff0c"
        "\u5bf9\u8f93\u5165\u7b26\u53f7\u4e32\u81ea\u4e0b\u800c\u4e0a\u5206\u6790\uff1b"
        "\u8f93\u51fa LR(1) \u5206\u6790\u8868\u4e0e\u5206\u6790\u8fc7\u7a0b\u4e2d\u7684\u72b6\u6001\u6808\u3001"
        "\u7b26\u53f7\u6808\u3001\u5269\u4f59\u8f93\u5165\u4e0e\u52a8\u4f5c\uff08\u5bf9\u5e94\u6559\u6750\u56fe 4-36\u30014-38\uff09\u3002",
        indent=True,
    )
    e.add_para(
        doc,
        "\u6587\u6cd5\u4e2d bool \u5728\u5de5\u7a0b\u4e2d\u8bb0\u4e3a boolExpr\u3002"
        "\u5b9e\u73b0\u5305\uff1aedu.groupname.compiler.parser \u4e0e grammar\u3002",
        indent=True,
    )

    e.add_heading(doc, "\u4e8c\u3001\u529f\u80fd\u63cf\u8ff0", 1)
    e.add_bullets(
        doc,
        [
            "\u8f93\u51fa\u8bed\u6cd5\u5206\u6790\u8868\uff1aACTION(\u72b6\u6001, \u7ec8\u7aef\u7b26)=\u79fb\u8fdb/s/\u89c4\u7ea6\uff0cGOTO(\u72b6\u6001, \u975e\u7ec8\u7aef\u7b26)=\u72b6\u6001\uff1b\u683c\u5f0f\u53c2\u8003\u6559\u6750\u56fe 4-38\u3002",
            "\u8f93\u51fa\u5206\u6790\u8fc7\u7a0b\uff1a\u6bcf\u6b65\u201c\u72b6\u6001\u6808 | \u7b26\u53f7\u6808 | \u5269\u4f59\u8f93\u5165 | \u52a8\u4f5c\u201d\uff08ParseReporter.formatStep\uff09\u3002",
            "\u5206\u6790\u6210\u529f\u65f6 accepted=true\uff1b\u65e0\u52a8\u4f5c\u9879\u65f6\u8bb0\u5f55 SyntaxError\u5e76 accepted=false\u3002",
            "\u5206\u6790\u8868\u9759\u6001\u6784\u9020\uff1b\u5bf9 else \u7684\u79fb\u8fdb/\u89c4\u7ea6\u51b2\u7a81\u6309\u60ac\u7a7a else \u4f18\u5148\u79fb\u8fdb\u3002",
            "ExperimentDemo \u8f93\u51fa\u300c\u5b9e\u9a8c\u4e8c\uff1aLR(1) \u8bed\u6cd5\u5206\u6790\u300d\u5c0f\u8282\u3002",
        ],
    )

    e.add_heading(doc, "\u4e09\u3001\u7a0b\u5e8f\u7ed3\u6784\u63cf\u8ff0", 1)
    e.add_heading(doc, "3.1 \u51fd\u6570/\u65b9\u6cd5\u8bf4\u660e", 2)
    e.add_table(doc, ["\u7c7b", "\u51fd\u6570\u539f\u578b", "\u53c2\u6570\u542b\u4e49", "\u8fd4\u56de\u503c", "\u529f\u80fd"], API_ROWS, [2.0, 3.0, 3.2, 2.0, 2.8])
    e.add_heading(doc, "3.2 \u51fd\u6570\u8c03\u7528\u5173\u7cfb\u56fe", 2)
    e.add_picture(doc, IMG_CALL, "\u56fe1  LR(1) \u8bed\u6cd5\u5206\u6790\u6a21\u5757\u51fd\u6570\u8c03\u7528\u5173\u7cfb\u56fe")
    e.add_heading(doc, "3.3 \u7a0b\u5e8f\u603b\u4f53\u6267\u884c\u6d41\u7a0b\u56fe", 2)
    e.add_picture(doc, IMG_FLOW, "\u56fe2  LR1Parser.parse \u603b\u4f53\u6267\u884c\u6d41\u7a0b\u56fe")

    e.add_heading(doc, "\u56db\u3001LR(1) \u5206\u6790\u8868\u4e0e\u6808\u7ed3\u6784\u8bbe\u8ba1", 1)
    e.add_heading(doc, "4.1 ParsingTable", 2)
    e.add_bullets(
        doc,
        [
            "ACTION \u952e (state, terminal)\uff0c\u503c SHIFT(s)/REDUCE(r)/ACCEPT/ERROR\u3002",
            "GOTO \u952e (state, nonTerminal)\uff0c\u503c\u4e3a\u89c4\u7ea6\u540e\u72b6\u6001\u53f7\u3002",
            "exportRows() \u5bfc\u51fa\u4e3a ACTION(...)=... / GOTO(...)=... \u6587\u672c\u884c\u3002",
        ],
    )
    e.add_heading(doc, "4.2 \u5206\u6790\u6808\u4e0e ParseTraceStep", 2)
    e.add_table(
        doc,
        ["\u7ed3\u6784", "\u542b\u4e49", "\u793a\u4f8b"],
        [
            ["stateStack", "\u72b6\u6001\u53f7\u6808", "[0, 3, 4, 5]"],
            ["symbolStack", "\u6587\u6cd5\u7b26\u53f7\u6808", "[$, {, decls, stmts]"],
            ["\u5269\u4f59\u8f93\u5165", "\u672a\u8bfb\u7ec8\u7ed3\u7b26", "id = num ; } $"],
            ["\u52a8\u4f5c", "ACTION \u663e\u793a", "s22 / r4 / err"],
        ],
    )
    e.add_para(doc, "\u8f93\u51fa\u683c\u5f0f\u4e0e\u6559\u6750\u56fe 4-38 \u4e00\u81f4\u3002", indent=True)

    e.add_heading(doc, "\u4e94\u3001LR(1) \u7b97\u6cd5\u6982\u8981", 1)
    e.add_bullets(
        doc,
        [
            "FIRST\u96c6\uff1acomputeFirstSets() \u8fed\u4ee3\u6c42\u89e3\u3002",
            "\u9879\u76ee\u96c6\uff1aclosure\u3001goTo\uff1bbuildParsingTableForGrammar \u586b ACTION/GOTO\u3002",
            "\u5206\u6790\uff1agetAction \u6267\u884c shift/reduce/accept/error\u3002",
        ],
    )

    e.add_heading(doc, "\u516d\u3001\u7a0b\u5e8f\u5173\u952e\u6e90\u4ee3\u7801", 1)
    e.add_para(doc, "\u4ee5\u4e0b\u6458\u81ea\u5de5\u7a0b parser\u3001grammar \u5305\uff0c\u884c\u53f7\u4e0e\u4ed3\u5e93\u4e00\u81f4\u3002", indent=True)
    for title, rel, s, en in [
        ("6.1 LR1Parser.parse", "edu/groupname/compiler/parser/LR1Parser.java", 46, 146),
        ("6.2 buildParsingTableForGrammar", "edu/groupname/compiler/parser/LR1Parser.java", 196, 251),
        ("6.3 closure / goTo", "edu/groupname/compiler/parser/LR1Parser.java", 254, 307),
        ("6.4 ParsingTable", "edu/groupname/compiler/parser/lr1/ParsingTable.java", 14, 44),
        ("6.5 Grammar.defaultLabGrammar", "edu/groupname/compiler/grammar/Grammar.java", 8, 70),
        ("6.6 ParseReporter", "edu/groupname/compiler/parser/ParseReporter.java", 14, 27),
    ]:
        e.add_heading(doc, title, 2)
        e.add_code_block(doc, java_lines(rel, s, en))

    e.add_heading(doc, "\u4e03\u3001\u6d4b\u8bd5\u6570\u636e\u4e0e\u8fd0\u884c\u7ed3\u679c\uff08\u22653 \u7ec4\uff09", 1)
    e.add_para(
        doc,
        "\u6765\u81ea docs/test-output/*.txt\u300c\u5b9e\u9a8c\u4e8c\u300d\u7ae0\u8282"
        "\uff08scripts/export-test-outputs.ps1 \u5bfc\u51fa\uff09\uff0c\u4e0e\u63a7\u5236\u53f0\u4e00\u81f4\u3002",
        indent=True,
    )
    for base, title in EXP2_SAMPLES:
        data = parse_exp2_section(TEST_OUTPUT_DIR / f"{base}.txt")
        e.add_heading(doc, title, 2)
        e.add_para(doc, f"\u8f93\u5165\uff08samples/{base}.src\uff09\uff1a", bold=True)
        e.add_code_block(doc, read_sample_source(base))
        e.add_para(doc, "\u8bed\u6cd5\u5206\u6790\u8868\uff08\u8282\u9009 ACTION\uff09\uff1a", bold=True)
        e.add_code_block(doc, data["table_summary"] + "\n" + "\n".join(data["table_rows"]))
        e.add_para(doc, "\u5206\u6790\u8fc7\u7a0b\uff08\u524d\u6bb5 \u2026 \u672b\u6bb5\uff09\uff1a", bold=True)
        trace = "\n".join(data["trace_head"])
        if data["trace_tail"]:
            trace += "\n...\n" + "\n".join(data["trace_tail"])
        e.add_code_block(doc, trace)
        e.add_para(doc, f"\u5206\u6790\u7ed3\u679c\uff1a{data['accepted']}", indent=True)

    e.add_heading(doc, "\u6d4b\u8bd5\u7ec4 4\uff1a\u8bed\u6cd5\u9519\u8bef", 2)
    e.add_para(doc, f"\u8f93\u5165\uff1a{SYNTAX_ERROR_SOURCE}", bold=True)
    e.add_para(doc, "\u5206\u6790\u8fc7\u7a0b\uff08\u672b\u51e0\u6b65\uff09\uff1a", bold=True)
    e.add_code_block(doc, "\n".join(SYNTAX_ERROR_TRACE))
    e.add_para(doc, f"\u5206\u6790\u7ed3\u679c\uff1a\n{SYNTAX_ERROR_RESULT}", indent=True)
    e.add_para(doc, "\u9a8c\u8bc1\uff1aLR1ParserTest.rejectsSyntaxErrorInput\u3002", indent=True)

    e.add_heading(doc, "\u516b\u3001\u5b9e\u9a8c\u603b\u7ed3", 1)
    e.add_bullets(
        doc,
        [
            "\u5b8c\u6210\u4e86\u4efb\u52a1\u4e66\u8981\u6c42\u7684 LR(1) \u5206\u6790\u8868\u8f93\u51fa\u4e0e\u5206\u6790\u6808\u8ddf\u8e2a\u8f93\u51fa\u3002",
            "\u5206\u6790\u8868\u9759\u6001\u6784\u9020\uff0c\u8f93\u51fa\u683c\u5f0f\u4e0e\u6559\u6750\u56fe 4-36\u30014-38 \u5bf9\u5e94\u3002",
            "\u6d4b\u8bd5\u7ec4 1\u20133 \u6765\u81ea test-output \u5bfc\u51fa\uff1b\u7ec4 4 \u9a8c\u8bc1\u591a\u4f59 } \u62d2\u7edd\u5206\u6790\u3002",
            "\u4e3a\u5b9e\u9a8c\u4e09\u8bed\u6cd5\u5236\u5bfc\u7ffb\u8bd1\u4e0e\u4e2d\u95f4\u4ee3\u7801\u751f\u6210\u63d0\u4f9b\u57fa\u7840\u3002",
        ],
    )
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    e.set_run_font(
        p.add_run(
            "\u6559\u6750\uff1a\u7f16\u8bd1\u539f\u7406\uff08\u7b2c 2 \u7248\uff09\uff0c"
            "Alfred V. Aho \u7b49\u8457\uff0c\u8d75\u5efa\u534e\u7b49\u8bd1\uff0c\u673a\u68b0\u5de5\u4e1a\u51fa\u7248\u793e"
        ),
        size=10,
    )
    return doc


def save_as_doc(docx_path: Path, doc_path: Path) -> bool:
    try:
        import win32com.client

        word = win32com.client.Dispatch("Word.Application")
        word.Visible = False
        d = word.Documents.Open(str(docx_path.resolve()))
        d.SaveAs(str(doc_path.resolve()), FileFormat=0)
        d.Close(False)
        word.Quit()
        return True
    except Exception as exc:
        print(f"Word COM: {exc}", file=sys.stderr)
        return False


def main() -> int:
    _setup_cjk_font()
    DESKTOP.mkdir(parents=True, exist_ok=True)
    doc = build_document()
    doc.save(str(OUTPUT_DOCX))
    print(f"Saved: {OUTPUT_DOCX}")
    if save_as_doc(OUTPUT_DOCX, OUTPUT_DOC):
        print(f"Saved: {OUTPUT_DOC}")
    for p in (IMG_CALL, IMG_FLOW):
        try:
            p.unlink(missing_ok=True)
        except OSError:
            pass
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
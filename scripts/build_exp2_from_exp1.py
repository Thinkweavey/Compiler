# -*- coding: utf-8 -*-
import re
from pathlib import Path

SRC = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\generate_exp1_word.py")
DST = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\generate_exp2_word.py")
ROOT = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler")
text = SRC.read_text(encoding="utf-8")

text = text.replace(
    "Generate Experiment 1 report Word .doc (aligned with project lexer code).",
    "Generate Experiment 2 (LR(1) syntax analysis) Word report on desktop.",
)
text = text.replace('REPORT_DIR = DESKTOP / "\u65b0\u5efa\u6587\u4ef6\u5939"\nOUTPUT_DOC = REPORT_DIR / "\u5b9e\u9a8c\u62a5\u544a\u4e00_\u66f4\u65b0.doc"\nTEMP_DOCX = REPORT_DIR / "_\u5b9e\u9a8c\u62a5\u544a\u4e00_temp.docx"\nIMG_CALL = DESKTOP / "_exp1_call_graph.png"\nIMG_FLOW = DESKTOP / "_exp1_flow_chart.png"',
    'OUTPUT_DOCX = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.docx"\nOUTPUT_DOC = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.doc"\nIMG_CALL = DESKTOP / "_exp2_call_graph.png"\nIMG_FLOW = DESKTOP / "_exp2_flow_chart.png"')

# constants
old_samples = re.search(r"# .*export-test-outputs.*\nSAMPLE_CASES = \[.*?\]\n\n", text, re.S)
exp2_const = """EXP2_SAMPLES = [
    ("sample1_basic", "\u6d4b\u8bd5\u7ec4 1\uff1a\u57fa\u7840\u58f0\u660e\uff08sample1_basic\uff09"),
    ("sample2_if_else", "\u6d4b\u8bd5\u7ec4 2\uff1aif-else\uff08sample2_if_else\uff09"),
    ("sample3_loop", "\u6d4b\u8bd5\u7ec4 3\uff1awhile / do-while\uff08sample3_loop\uff09"),
]
SYNTAX_ERROR_SOURCE = "{ int a; } }"
SYNTAX_ERROR_TRACE_TAIL = [
    "\u72b6\u6001\u6808=[0, 3, 4, 6, 22, 38] | \u7b26\u53f7\u6808=[$, {, decls, type, id, ;] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=r4",
    "\u72b6\u6001\u6808=[0, 3, 4, 8] | \u7b26\u53f7\u6808=[$, {, decls, decl] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=r2",
    "\u72b6\u6001\u6808=[0, 3, 4] | \u7b26\u53f7\u6808=[$, {, decls] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=r11",
    "\u72b6\u6001\u6808=[0, 3, 4, 5] | \u7b26\u53f7\u6808=[$, {, decls, stmts] | \u5269\u4f59\u8f93\u5165=} } $ | \u52a8\u4f5c=s21",
    "\u72b6\u6001\u6808=[0, 3, 4, 5, 21] | \u7b26\u53f7\u6808=[$, {, decls, stmts, }] | \u5269\u4f59\u8f93\u5165=} $ | \u52a8\u4f5c=err",
]
SYNTAX_ERROR_RESULT = (
    "accepted = false\\n"
    "\u8bed\u6cd5\u9519\u8bef: \u8bed\u6cd5\u9519\u8bef\uff0c\u5f53\u524d\u7b26\u53f7: } @ SourcePosition[line=1, column=12]"
)

"""
if old_samples:
    text = text[:old_samples.start()] + exp2_const + text[old_samples.end():]

parse_exp2 = '''
def parse_exp2_section(txt_path: Path) -> dict:
    lines = read_output_text(txt_path).splitlines()
    in_exp2 = False
    in_table = False
    in_trace = False
    table_summary = ""
    table_rows: list[str] = []
    trace_rows: list[str] = []
    accepted = ""
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
            in_trace = True
            in_table = False
            continue
        if in_trace and line.startswith("\u72b6\u6001\u6808="):
            trace_rows.append(line.strip())
            continue
        if line.strip().startswith("accepted"):
            accepted = line.strip()
            in_trace = False
        if in_trace and line.startswith("\u8bed\u6cd5\u9519\u8bef"):
            accepted += "\\n" + line.strip()
    return {
        "table_summary": table_summary,
        "table_rows": table_rows,
        "trace_head": trace_rows[:6],
        "trace_tail": trace_rows[-3:] if len(trace_rows) > 6 else [],
        "accepted": accepted,
    }

'''

text = re.sub(r"def parse_exp1_from_output.*?def load_test_cases_from_exports", parse_exp2 + "\ndef _load_test_cases_removed", text, flags=re.S)
text = text.replace("def _load_test_cases_removed", "def load_test_cases_from_exports", 1)

# fix main
text = re.sub(
    r"def main\(\) -> int:.*?if __name__",
    '''def main() -> int:
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


if __name__''',
    text,
    flags=re.S,
)

DST.write_text(text, encoding="utf-8")
print("written", DST, "bytes", DST.stat().st_size)
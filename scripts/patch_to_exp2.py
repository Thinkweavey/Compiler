# ASCII-only patcher: reads generate_exp1_word.py, writes generate_exp2_word.py
from pathlib import Path

SRC = Path(__file__).parent / "generate_exp1_word.py"
DST = Path(__file__).parent / "generate_exp2_word.py"

# Unicode strings via escapes to avoid editor encoding issues
U = lambda s: s  # noqa: E731 - content is utf-8 in this file when saved correctly

def main() -> None:
    text = SRC.read_text(encoding="utf-8")
    text = text.replace(
        '"""Generate Experiment 1 report Word .doc (aligned with project lexer code)."""',
        '"""Generate Experiment 2 (LR(1) syntax analysis) Word report on desktop."""',
    )
    old_paths = """REPORT_DIR = DESKTOP / "\u65b0\u5efa\u6587\u4ef6\u5939"
OUTPUT_DOC = REPORT_DIR / "\u5b9e\u9a8c\u62a5\u544a\u4e00_\u66f4\u65b0.doc"
TEMP_DOCX = REPORT_DIR / "_\u5b9e\u9a8c\u62a5\u544a\u4e00_temp.docx"
IMG_CALL = DESKTOP / "_exp1_call_graph.png"
IMG_FLOW = DESKTOP / "_exp1_flow_chart.png\""""
    new_paths = """OUTPUT_DOCX = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.docx"
OUTPUT_DOC = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.doc"
IMG_CALL = DESKTOP / "_exp2_call_graph.png"
IMG_FLOW = DESKTOP / "_exp2_flow_chart.png\""""
    text = text.replace(old_paths, new_paths)
    DST.write_text(text, encoding="utf-8")
    data = DST.read_bytes()
    assert b"\x00" not in data
    print("patched", DST, "len", len(data))


if __name__ == "__main__":
    main()

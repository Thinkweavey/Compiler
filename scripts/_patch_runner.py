from pathlib import Path
SRC = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\generate_exp1_word.py")
DST = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\generate_exp2_word.py")
text = SRC.read_text(encoding="utf-8")
text = text.replace(
    "Generate Experiment 1 report Word .doc (aligned with project lexer code).",
    "Generate Experiment 2 (LR(1) syntax analysis) Word report on desktop.",
)
old = 'REPORT_DIR = DESKTOP / "\u65b0\u5efa\u6587\u4ef6\u5939"\nOUTPUT_DOC = REPORT_DIR / "\u5b9e\u9a8c\u62a5\u544a\u4e00_\u66f4\u65b0.doc"\nTEMP_DOCX = REPORT_DIR / "_\u5b9e\u9a8c\u62a5\u544a\u4e00_temp.docx"\nIMG_CALL = DESKTOP / "_exp1_call_graph.png"\nIMG_FLOW = DESKTOP / "_exp1_flow_chart.png"'
new = 'OUTPUT_DOCX = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.docx"\nOUTPUT_DOC = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.doc"\nIMG_CALL = DESKTOP / "_exp2_call_graph.png"\nIMG_FLOW = DESKTOP / "_exp2_flow_chart.png"'
text = text.replace(old, new)
# title and section markers for experiment 2 report shell
text = text.replace("\u5b9e\u9a8c\u4e00\uff1a\u8bcd\u6cd5\u5206\u6790", "\u5b9e\u9a8c\u4e8c\uff1aLR(1) \u8bed\u6cd5\u5206\u6790")
text = text.replace("\u8bcd\u6cd5\u5206\u6790\u7a0b\u5e8f\u5b9e\u9a8c", "LR(1) \u8bed\u6cd5\u5206\u6790\u7a0b\u5e8f\u5b9e\u9a8c")
text = text.replace("\u8bcd\u6cd5\u5206\u6790\u3001\u5b9e\u9a8c\u62a5\u544a", "LR(1) \u8bed\u6cd5\u5206\u6790\u3001\u5b9e\u9a8c\u62a5\u544a")
text = text.replace(
    "    REPORT_DIR.mkdir(parents=True, exist_ok=True)\n    doc = build_document()\n    doc.save(str(TEMP_DOCX))\n    print(f\"Saved: {TEMP_DOCX}\")\n\n    ok = save_as_doc(TEMP_DOCX, OUTPUT_DOC)",
    "    DESKTOP.mkdir(parents=True, exist_ok=True)\n    doc = build_document()\n    doc.save(str(OUTPUT_DOCX))\n    print(f\"Saved: {OUTPUT_DOCX}\")\n\n    ok = save_as_doc(OUTPUT_DOCX, OUTPUT_DOC)",
)
text = text.replace(
    '    fallback_docx = REPORT_DIR / "\u5b9e\u9a8c\u62a5\u544a\u4e00_\u66f4\u65b0.docx"',
    '    fallback_docx = DESKTOP / "\u5b9e\u9a8c\u62a5\u544a\u4e8c.docx"',
)
DST.write_text(text, encoding="utf-8")
print("step1 ok", len(text))

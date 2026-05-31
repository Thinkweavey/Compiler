def build_document() -> Document:
    generate_call_graph(IMG_CALL)
    generate_flow_chart(IMG_FLOW)
    doc = Document()
    sec = doc.sections[0]
    sec.top_margin = Cm(2.5)
    sec.bottom_margin = Cm(2.5)
    sec.left_margin = Cm(2.8)
    sec.right_margin = Cm(2.5)
    for _ in range(3):
        doc.add_paragraph()
    t = doc.add_paragraph()
    t.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run_font(t.add_run("\u7f16\u8bd1\u7cfb\u7edf\u8bbe\u8ba1\u5b9e\u8df5\u5b9e\u9a8c\u62a5\u544a"), "\u9ed1\u4f53", 22, True)
    doc.add_paragraph()
    t2 = doc.add_paragraph()
    t2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run_font(t2.add_run("\u5b9e\u9a8c\u4e8c\uff1aLR(1) \u8bed\u6cd5\u5206\u6790\u7a0b\u5e8f\u5b9e\u9a8c"), "\u9ed1\u4f53", 18, True)
    doc.add_page_break()
    add_heading(doc, "\u4e00\u3001\u5b9e\u9a8c\u76ee\u7684\u4e0e\u4efb\u52a1", 1)
    add_para(doc, "\u6839\u636e\u8bfe\u7a0b\u6587\u6cd5\u7f16\u5236 LR(1) \u5206\u6790\u7a0b\u5e8f\uff1b\u8f93\u51fa\u5206\u6790\u8868\u4e0e\u5206\u6790\u6808\u8ddf\u8e2a\u3002", indent=True)
    return doc
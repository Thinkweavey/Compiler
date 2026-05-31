from pathlib import Path
p = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\gen_exp3_report.py")
lines = p.read_text(encoding="utf-8").splitlines()
header = [
    'DESKTOP = Path(r"c:\\Users\\Cc\\Desktop")',
    'COVER_DOC = DESKTOP / "\\u5b9e\\u9a8c\\u62a5\\u544a\\u5c01\\u9762.doc"',
    'BODY_TEMP = DESKTOP / "_exp3_body_temp.docx"',
    'OUTPUT_DOCX = DESKTOP / "\\u5b9e\\u9a8c\\u62a5\\u544a\\u4e09.docx"',
    'OUTPUT_DOC = DESKTOP / "\\u5b9e\\u9a8c\\u62a5\\u544a\\u4e09.doc"',
    'IMG_CALL = DESKTOP / "_exp3_call_graph.png"',
    'IMG_FLOW = DESKTOP / "_exp3_flow_chart.png"',
]
out = []
skip = False
for line in lines:
    if line.startswith("COVER_DOC") or line.startswith("BODY_TEMP") or (line.startswith("DESKTOP =") and "Path" in line) or line.startswith("OUTPUT_") or line.startswith("IMG_"):
        if not skip:
            out.extend(header)
            skip = True
        continue
    if skip and line == "":
        skip = False
    if line.startswith("EXP3_SAMPLES"):
        skip = False
    if not skip or not (line.startswith("COVER") or line.startswith("BODY") or line.startswith("DESKTOP") or line.startswith("OUTPUT") or line.startswith("IMG_")):
        if line.startswith("EXP3_SAMPLES"):
            skip = False
        if not (line.startswith("COVER_DOC") or line.startswith("BODY_TEMP") or (line.startswith("DESKTOP =") and "Path" in line) or line.startswith("OUTPUT_") or line.startswith("IMG_")):
            out.append(line)
text = "\n".join(out)
merge_fn = '''

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
if "def merge_with_cover" not in text:
    text = text.replace("def save_as_doc", merge_fn + "\ndef save_as_doc")
p.write_text(text, encoding="utf-8")
compile(text, "x", "exec")
print("ok")
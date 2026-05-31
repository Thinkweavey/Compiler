import shutil
from pathlib import Path
import win32com.client
src = Path(r"c:\Users\Cc\Desktop") / "\u5b9e\u9a8c\u62a5\u544a\u5c01\u9762.doc"
dst = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\_cover.doc")
body = Path(r"c:\Users\Cc\Desktop\_exp3_body_temp.docx")
if not body.exists():
    body = Path(r"c:\Users\Cc\Desktop") / "\u5b9e\u9a8c\u62a5\u544a\u4e09.docx"
shutil.copy2(src, dst)
print("cover exists", src.exists(), "body", body.exists())
word = win32com.client.Dispatch("Word.Application")
word.Visible = False
try:
    c = word.Documents.Open(str(dst.resolve()))
    print("opened cover paras", c.Paragraphs.Count)
    c.Close(False)
    if body.exists():
        d = word.Documents.Open(str(dst.resolve()))
        e = d.Content
        e.Collapse(0)
        e.InsertBreak(7)
        e.InsertFile(str(body.resolve()))
        out = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\_merged_test.docx")
        d.SaveAs2(str(out.resolve()), FileFormat=16)
        d.Close(False)
        print("merged ok", out.stat().st_size)
except Exception as ex:
    print("ERR", ex)
finally:
    word.Quit()
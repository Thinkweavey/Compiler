from pathlib import Path
import win32com.client
cover = Path(r"c:\Users\Cc\Desktop") / "\u5b9e\u9a8c\u62a5\u544a\u5c01\u9762.doc"
print(cover, cover.exists(), cover.stat().st_size)
word = win32com.client.DispatchEx("Word.Application")
word.Visible = False
word.DisplayAlerts = 0
try:
    d = word.Documents.Open(str(cover), ConfirmConversions=True, ReadOnly=True, AddToRecentFiles=False)
    print("opened", d.Name, d.Paragraphs.Count)
    tmp = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\_cover_out.docx")
    d.SaveAs2(str(tmp), FileFormat=16)
    d.Close(False)
    print("saved docx", tmp.stat().st_size)
except Exception as e:
    print("open fail", e)
word.Quit()
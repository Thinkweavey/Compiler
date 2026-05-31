from pathlib import Path
p = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\gen_exp3_report.py")
text = p.read_text(encoding="utf-8")
text = text.replace('win32com.client.Dispatch("Word.Application")', 'win32com.client.DispatchEx("Word.Application")')
if "DisplayAlerts" not in text:
    text = text.replace("word.Visible = False\n        cover =", "word.Visible = False\n        word.DisplayAlerts = 0\n        cover =")
    text = text.replace("word.Visible = False\n        doc = word.Documents.Open(str(temp_cover", "word.Visible = False\n        word.DisplayAlerts = 0\n        doc = word.Documents.Open(str(temp_cover")
old_save = "        d = word.Documents.Open(str(docx_path.resolve()))"
if old_save in text and "DisplayAlerts" not in text[text.find("def save_as_doc"):text.find("def save_as_doc")+500]:
    text = text.replace(
        "        word = win32com.client.DispatchEx(\"Word.Application\")\n        word.Visible = False\n        d = word.Documents.Open",
        "        word = win32com.client.DispatchEx(\"Word.Application\")\n        word.Visible = False\n        word.DisplayAlerts = 0\n        d = word.Documents.Open",
        1,
    )
p.write_text(text, encoding="utf-8")
print("done")
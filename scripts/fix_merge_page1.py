from pathlib import Path
p = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\gen_exp3_report.py")
text = p.read_text(encoding="utf-8")
start = text.find("def merge_with_cover")
end = text.find("\ndef save_as_doc", start)
new_fn = r'''def merge_with_cover(cover_path: Path, body_path: Path, out_path: Path) -> bool:
    if not cover_path.exists():
        print(f"Cover missing: {cover_path}", file=sys.stderr)
        return False
    temp_cover = cover_path.parent / "_cover_temp.docx"
    try:
        import win32com.client
        word = win32com.client.DispatchEx("Word.Application")
        word.Visible = False
        word.DisplayAlerts = 0
        cover = word.Documents.Open(
            str(cover_path.resolve()),
            ConfirmConversions=True,
            ReadOnly=True,
            AddToRecentFiles=False,
        )
        # page 1 only (cover template may contain extra pages)
        p1_start = cover.GoTo(What=1, Which=1, Count=1)
        p2_start = cover.GoTo(What=1, Which=1, Count=2)
        page1 = cover.Range(Start=p1_start.Start, End=p2_start.Start)
        merged = word.Documents.Add()
        page1.Copy()
        merged.Content.Paste()
        cover.Close(False)
        rng = merged.Content
        rng.Collapse(0)
        rng.InsertBreak(7)
        rng.InsertFile(
            str(body_path.resolve()),
            ConfirmConversions=False,
            Link=False,
            Attachment=False,
        )
        merged.SaveAs2(str(out_path.resolve()), FileFormat=16)
        merged.Close(False)
        word.Quit()
        temp_cover.unlink(missing_ok=True)
        return True
    except Exception as exc:
        print(f"merge cover: {exc}", file=sys.stderr)
        try:
            word.Quit()
        except Exception:
            pass
        temp_cover.unlink(missing_ok=True)
        return False
'''
text = text[:start] + new_fn + text[end:]
p.write_text(text, encoding="utf-8")
compile(text, "x", "exec")
print("updated merge")
from pathlib import Path

def patch_merge():
    p = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\gen_exp3_report.py")
    text = p.read_text(encoding="utf-8")
    old = """def merge_with_cover(cover_path: Path, body_path: Path, out_path: Path) -> bool:
    if not cover_path.exists():
        print(f\"Cover missing: {cover_path}\", file=sys.stderr)
        return False
    try:
        import win32com.client
        word = win32com.client.Dispatch(\"Word.Application\")
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
        print(f\"merge cover: {exc}\", file=sys.stderr)
        return False"""
    new = """def merge_with_cover(cover_path: Path, body_path: Path, out_path: Path) -> bool:
    if not cover_path.exists():
        print(f\"Cover missing: {cover_path}\", file=sys.stderr)
        return False
    temp_cover = cover_path.parent / \"_cover_temp.docx\"
    try:
        import win32com.client
        word = win32com.client.Dispatch(\"Word.Application\")
        word.Visible = False
        cover = word.Documents.Open(str(cover_path.resolve()), ReadOnly=True)
        cover.SaveAs2(str(temp_cover.resolve()), FileFormat=16)
        cover.Close(False)
        doc = word.Documents.Open(str(temp_cover.resolve()))
        rng = doc.Content
        rng.Collapse(0)
        rng.InsertBreak(7)
        rng.InsertFile(
            str(body_path.resolve()),
            ConfirmConversions=False,
            Link=False,
            Attachment=False,
        )
        doc.SaveAs2(str(out_path.resolve()), FileFormat=16)
        doc.Close(False)
        word.Quit()
        temp_cover.unlink(missing_ok=True)
        return True
    except Exception as exc:
        print(f\"merge cover: {exc}\", file=sys.stderr)
        try:
            word.Quit()
        except Exception:
            pass
        temp_cover.unlink(missing_ok=True)
        return False"""
    if old not in text:
        print("old merge block not found")
        return
    p.write_text(text.replace(old, new), encoding="utf-8")
    print("patched merge")

patch_merge()
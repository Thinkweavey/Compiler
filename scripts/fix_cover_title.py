from pathlib import Path
p = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\gen_exp3_report.py")
text = p.read_text(encoding="utf-8")
needle = "        page1 = cover.Range(Start=p1_start.Start, End=p2_start.Start)"
insert = """        page1 = cover.Range(Start=p1_start.Start, End=p2_start.Start)
        rep = [
            ("\\u5b9e\\u9a8c\\u9879\\u76ee\\u4e00\\uff1a\\u8bcd\\u6cd5\\u5206\\u6790\\u5b9e\\u9a8c",
             "\\u5b9e\\u9a8c\\u4e09\\uff1a\\u8bed\\u6cd5\\u5236\\u5bfc\\u7ffb\\u8bd1\\u4e0e\\u4e2d\\u95f4\\u4ee3\\u7801\\u5b9e\\u9a8c"),
            ("\\u5b9e\\u9a8c\\u4e00\\uff1a\\u8bcd\\u6cd5\\u5206\\u6790",
             "\\u5b9e\\u9a8c\\u4e09\\uff1a\\u8bed\\u6cd5\\u5236\\u5bfc\\u7ffb\\u8bd1"),
        ]
        for old, new in rep:
            f = page1.Find
            f.ClearFormatting()
            f.Text = old.encode("utf-8").decode("unicode_escape")
            f.Replacement.Text = new.encode("utf-8").decode("unicode_escape")
            f.Execute(Replace=2)"""
if needle in text and "rep = [" not in text:
    text = text.replace(needle, insert)
    p.write_text(text, encoding="utf-8")
    print("added cover title replace")
else:
    print("skip", "rep" in text)
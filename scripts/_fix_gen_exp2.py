from pathlib import Path
p = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\gen_exp2_report.py")
lines = p.read_text(encoding="utf-8").splitlines()
out = []
for i, ln in enumerate(lines):
    if ln.strip().startswith("TYePg") or (ln.strip().startswith('"') and "r`h" in ln):
        continue
    if ln.strip() == 'TYePg' or 'iRYO' in ln and 'ParseReporter' in ln and ln.strip().startswith('"'):
        continue
    out.append(ln)
text = "\n".join(out)
text = "".join(c for c in text if c in "\t\n\r" or ord(c) >= 32)
p.write_text(text, encoding="utf-8")
compile(text, "x", "exec")
print("lines", len(lines), "->", len(out))
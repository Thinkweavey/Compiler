import shutil
from pathlib import Path

root = Path(__file__).parent
shutil.copyfile(root / "generate_exp1_word.py", root / "generate_exp2_word.py")
p = root / "generate_exp2_word.py"
lines = p.read_text(encoding="utf-8").splitlines()
out = []
skip_next = False
for i, line in enumerate(lines):
    if skip_next:
        skip_next = False
        continue
    if (
        line.rstrip() == '            accepted += "'
        and i + 1 < len(lines)
        and lines[i + 1].strip() == '" + line.strip()'
    ):
        out.append('            accepted += "\\n" + line.strip()')
        skip_next = True
        continue
    out.append(line)
p.write_text("\n".join(out) + "\n", encoding="utf-8")
compile(p.read_text(encoding="utf-8"), "x", "exec")
print("ok")

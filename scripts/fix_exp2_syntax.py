from pathlib import Path
import re
p = Path(r"d:\fzu\PrinciplesOfCompilation\Compiler\scripts\generate_exp2_word.py")
text = p.read_text(encoding="utf-8")
text = re.sub(r"accepted \+= \"\s*\n\s*\" \+ line\.strip\(\)", "accepted += \"\\n\" + line.strip()", text)
p.write_text(text, encoding="utf-8")
compile(text, str(p), "exec")
print("ok")

"""Turn a failed Gradle log into job annotations, so build errors show on the run's summary page."""
import re
import sys

path = sys.argv[1] if len(sys.argv) > 1 else "build.log"
try:
    lines = open(path, encoding="utf-8", errors="replace").read().splitlines()
except OSError:
    print("::error title=Build failed::No build log was written.")
    sys.exit(0)

keep = []
in_block = False
for line in lines:
    stripped = re.sub(r"file://\S*?/app/src/main/java/", "", line)
    if line.startswith("* What went wrong"):
        in_block = True
    elif line.startswith("* Try") or line.startswith("* Get more help"):
        in_block = False
    if in_block or line.startswith("e: ") or "error:" in line or line.startswith("ERROR"):
        keep.append(stripped)
if not keep:
    keep = lines[-80:]

# De-duplicate while keeping order, then send in chunks (a step can carry up to 10 error annotations).
seen = set()
unique = [l for l in keep if not (l in seen or seen.add(l))]


def esc(s):
    return s.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


chunk = 40
for n, start in enumerate(range(0, min(len(unique), chunk * 9), chunk), 1):
    body = "\n".join(unique[start:start + chunk])
    print(f"::error title=Build errors part {n}::{esc(body)}")

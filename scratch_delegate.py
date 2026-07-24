import re
import os
import json
import urllib.request
import time
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

API_KEY = os.environ.get("JULES_API_KEY")
REPO = "phalbohr/tic-tac-tore"
DW_FILE = "/Users/ppolukhin/Projects/tic-tac-tore/_bmad-output/implementation-artifacts/deferred-work.md"
CODE1_FILE = "/Users/ppolukhin/.gemini/skills/code-1-guide/SKILL.md"
CODE2_FILE = "/Users/ppolukhin/.gemini/skills/code-2-test/SKILL.md"

def get_file_content(path):
    try:
        with open(path, "r") as f:
            return f.read()
    except Exception:
        return ""

code1_rules = get_file_content(CODE1_FILE)
code2_rules = get_file_content(CODE2_FILE)

with open(DW_FILE, "r") as f:
    content = f.read()

blocks = content.split("### ")
open_tasks = []

for block in blocks[1:]:
    lines = block.strip().split("\n")
    if not lines: continue
    
    title = lines[0].strip()
    dw_id = title.split(":")[0].strip()
    
    if dw_id != "DW-8":
        continue
    
    reason = ""
    location = ""
    
    for line in lines:
        if line.strip().startswith("reason:"):
            reason = line[len("reason:"):].strip()
        elif line.strip().startswith("location:"):
            location = line[len("location:"):].strip()
            
    open_tasks.append({
        "id": dw_id,
        "title": title,
        "reason": reason,
        "location": location
    })

for task in open_tasks:
    print(f"Processing {task['id']}...")
    
    prompt = f"""Task: {task['title']}
Reason/Description: {task['reason']}
Location: {task['location']}

Context: You are delegated this task from the Deferred Work ledger. Please fix the issue described above.

CRITICAL INSTRUCTION FOR THIS FIX:
DO NOT add test endpoints (like /api/auth/test-user) into production controllers. DO NOT remove environment checks from production code. 
Instead, if you need test endpoints to assert state after 401 account deletion, create a dedicated separate controller class (e.g., E2ETestSupportController) and annotate it at the class level with `@Profile("e2e")` or `@Profile("test")` so it is completely excluded from production builds.

Acceptance criteria:
- The issue described in the Reason is resolved without exposing backdoors in production.
- Test endpoints are strictly protected via Spring @Profile.
- Code compiles and passes existing tests.

Code conventions: see CLAUDE.md and AGENTS.md in the repo root.
Do not write code comments — no inline comments, no block comments, no Javadoc/KDoc.

Production code conventions:
{code1_rules[:2000]}

Test code conventions:
{code2_rules[:2000]}
"""

    body = {
        "title": f"Retry {task['title']}",
        "prompt": prompt,
        "sourceContext": {
            "source": f"sources/github/{REPO}",
            "githubRepoContext": {"startingBranch": "develop"}
        },
        "automationMode": "AUTO_CREATE_PR",
        "requirePlanApproval": False
    }

    try:
        req = urllib.request.Request(
            "https://jules.googleapis.com/v1alpha/sessions",
            data=json.dumps(body).encode(),
            headers={
                "x-goog-api-key": API_KEY,
                "Content-Type": "application/json"
            }
        )
        resp = urllib.request.urlopen(req, context=ctx)
        result = json.loads(resp.read())
        print(f"✅ Created Jules session for {task['id']}: {result.get('name')} | State: {result.get('state')}")
    except Exception as e:
        print(f"❌ Failed to create session for {task['id']}: {e}")
        try:
            print(e.read().decode())
        except:
            pass

print("Done.")

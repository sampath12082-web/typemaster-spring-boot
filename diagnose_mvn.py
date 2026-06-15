import subprocess
from pathlib import Path
cwd = Path(__file__).parent
result = subprocess.run([cwd / 'mvnw.cmd', '-e', 'test-compile'], cwd=cwd, capture_output=True, text=True)
print('RETURN_CODE:', result.returncode)
print('STDOUT:\n', result.stdout)
print('STDERR:\n', result.stderr)

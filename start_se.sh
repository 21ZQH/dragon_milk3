#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_WEBAPP="$SCRIPT_DIR/webapp"

if [[ ! -d "$SOURCE_WEBAPP/WEB-INF" ]]; then
  echo "Source webapp directory not found: $SOURCE_WEBAPP" >&2
  exit 1
fi

resolve_tomcat_home() {
  local candidate
  for candidate in \
    "${TOMCAT_HOME:-}" \
    "${CATALINA_HOME:-}" \
    "/opt/homebrew/opt/tomcat/libexec" \
    "/usr/local/opt/tomcat/libexec"; do
    if [[ -n "$candidate" && -x "$candidate/bin/startup.sh" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done
  return 1
}

TOMCAT_HOME="$(resolve_tomcat_home || true)"
if [[ -z "$TOMCAT_HOME" ]]; then
  echo "Could not locate Tomcat." >&2
  echo "Set TOMCAT_HOME or CATALINA_HOME to your Tomcat 11 installation directory." >&2
  exit 1
fi

TARGET_DIR="$TOMCAT_HOME/webapps/SE"
BACKUP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/se-file-backup.XXXXXX")"
trap 'rm -rf "$BACKUP_DIR"' EXIT

echo "[1/5] Using Tomcat at: $TOMCAT_HOME"
echo "[2/5] Backing up runtime data..."
if [[ -d "$TARGET_DIR/WEB-INF/file" ]]; then
  cp -R "$TARGET_DIR/WEB-INF/file/." "$BACKUP_DIR/"
fi

echo "[3/5] Deploying project to $TARGET_DIR..."
rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"
cp -R "$SOURCE_WEBAPP/." "$TARGET_DIR/"

echo "[4/5] Restoring runtime data..."
mkdir -p "$TARGET_DIR/WEB-INF/file"
if compgen -G "$BACKUP_DIR/*" > /dev/null; then
  cp -R "$BACKUP_DIR/." "$TARGET_DIR/WEB-INF/file/"
fi
touch \
  "$TARGET_DIR/WEB-INF/file/users.txt" \
  "$TARGET_DIR/WEB-INF/file/courses.txt" \
  "$TARGET_DIR/WEB-INF/file/deadline.txt" \
  "$TARGET_DIR/WEB-INF/file/mo-deadline.txt"
if [[ -f "$SOURCE_WEBAPP/WEB-INF/file/candidates.txt" ]]; then
  cp "$SOURCE_WEBAPP/WEB-INF/file/candidates.txt" "$TARGET_DIR/WEB-INF/file/candidates.txt"
fi

echo "[5/5] Compiling and starting Tomcat..."
export TOMCAT_HOME
export CATALINA_HOME="$TOMCAT_HOME"
export CATALINA_BASE="$TOMCAT_HOME"
(cd "$TARGET_DIR/WEB-INF" && ../command2.sh)
"$TOMCAT_HOME/bin/startup.sh"

echo "Deployment finished."
echo "Open: http://localhost:8081/SE/start.html"

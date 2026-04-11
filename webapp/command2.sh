#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WEB_INF_DIR="$SCRIPT_DIR/WEB-INF"
if [[ ! -d "$WEB_INF_DIR/src" ]]; then
  WEB_INF_DIR="$SCRIPT_DIR"
fi

SRC_DIR="$WEB_INF_DIR/src"
CLASSES_DIR="$WEB_INF_DIR/classes"

if [[ ! -d "$SRC_DIR" ]]; then
  echo "Source directory not found: $SRC_DIR" >&2
  exit 1
fi

find_api_jar() {
  local version candidate

  for version in 6.1.0 6.0.0 5.0.0; do
    candidate="$HOME/.m2/repository/jakarta/servlet/jakarta.servlet-api/$version/jakarta.servlet-api-$version.jar"
    if [[ -f "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done

  for candidate in \
    "${TOMCAT_HOME:-}" \
    "${CATALINA_HOME:-}" \
    "${CATALINA_BASE:-}"; do
    if [[ -n "$candidate" && -f "$candidate/lib/servlet-api.jar" ]]; then
      printf '%s\n' "$candidate/lib/servlet-api.jar"
      return 0
    fi
    if [[ -n "$candidate" && -f "$candidate/lib/jakarta.servlet-api.jar" ]]; then
      printf '%s\n' "$candidate/lib/jakarta.servlet-api.jar"
      return 0
    fi
  done

  return 1
}

API_JAR="$(find_api_jar || true)"
if [[ -z "$API_JAR" ]]; then
  echo "Could not find a servlet API JAR." >&2
  echo "Set TOMCAT_HOME or CATALINA_HOME, or install jakarta.servlet-api in Maven local repository." >&2
  exit 1
fi

mkdir -p "$CLASSES_DIR"
CP="$API_JAR:$CLASSES_DIR:."

cd "$SRC_DIR"
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" model/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" store/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" listener/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" controller/*.java

echo "Compilation completed."

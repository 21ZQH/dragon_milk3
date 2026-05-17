#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WEB_INF_DIR="$SCRIPT_DIR/WEB-INF"
if [[ ! -d "$WEB_INF_DIR/src" ]]; then
  WEB_INF_DIR="$SCRIPT_DIR"
fi

SRC_DIR="$WEB_INF_DIR/src"
CLASSES_DIR="$WEB_INF_DIR/classes"
LIB_DIR="$WEB_INF_DIR/lib"

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

find_pdfbox_jars() {
  local pdfbox_version="3.0.3"
  local commons_logging_version="1.3.3"
  local pdfbox_jar="$HOME/.m2/repository/org/apache/pdfbox/pdfbox/$pdfbox_version/pdfbox-$pdfbox_version.jar"
  local fontbox_jar="$HOME/.m2/repository/org/apache/pdfbox/fontbox/$pdfbox_version/fontbox-$pdfbox_version.jar"
  local pdfbox_io_jar="$HOME/.m2/repository/org/apache/pdfbox/pdfbox-io/$pdfbox_version/pdfbox-io-$pdfbox_version.jar"
  local commons_logging_jar="$HOME/.m2/repository/commons-logging/commons-logging/$commons_logging_version/commons-logging-$commons_logging_version.jar"

  if [[ -f "$pdfbox_jar" && -f "$fontbox_jar" && -f "$pdfbox_io_jar" && -f "$commons_logging_jar" ]]; then
    printf '%s:%s:%s:%s\n' "$pdfbox_jar" "$fontbox_jar" "$pdfbox_io_jar" "$commons_logging_jar"
    return 0
  fi

  return 1
}

PDFBOX_CP="$(find_pdfbox_jars || true)"
if [[ -z "$PDFBOX_CP" ]]; then
  echo "Could not find Apache PDFBox JARs." >&2
  echo "Run \"mvn -DskipTests compile\" from the project root first." >&2
  exit 1
fi

mkdir -p "$LIB_DIR"
IFS=':' read -r -a pdfbox_jars <<< "$PDFBOX_CP"
for jar in "${pdfbox_jars[@]}"; do
  cp -f "$jar" "$LIB_DIR/"
done

CP="$API_JAR:$PDFBOX_CP:$CLASSES_DIR:."

cd "$SRC_DIR"
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" model/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" store/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" service/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" service/ai/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" service/ai/impl/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" service/impl/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" listener/*.java
javac -encoding UTF-8 -classpath "$CP" -d "$CLASSES_DIR" controller/*.java

echo "Compilation completed."

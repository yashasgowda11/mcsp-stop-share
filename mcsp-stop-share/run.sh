#!/bin/bash

# === CONFIGURATION ===
SRC_DIR="src"
OUT_DIR="out"
JAVA_MAIN="core.Main"

# === DEFAULT VALUES ===
DATASET="central_usa.txt"
NUM_CRITERIA=3
MAX_MEMORY_PARTITIONS=50
PARTITION_SIZE=200
REGEN="false"

# === PARSE INPUT ARGUMENTS ===
while [[ $# -gt 0 ]]; do
  case "$1" in
    --regen) REGEN="true"; shift ;;
    *.txt) DATASET="$1"; shift ;;
    *) 
      if [[ -z "$NUM_CRITERIA_SET" ]]; then NUM_CRITERIA="$1"; NUM_CRITERIA_SET=true
      elif [[ -z "$MAX_MEMORY_SET" ]]; then MAX_MEMORY_PARTITIONS="$1"; MAX_MEMORY_SET=true
      elif [[ -z "$PARTITION_SIZE_SET" ]]; then PARTITION_SIZE="$1"; PARTITION_SIZE_SET=true
      else echo "Unknown argument: $1"; exit 1; fi
      shift ;;
  esac
done

# === CREATE OUTPUT FOLDERS ===
mkdir -p results
mkdir -p "$OUT_DIR"

# === COMPILE JAVA PROJECT ===
echo "Compiling Java source files..."
rm -rf out/
javac -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java")

if [ $? -ne 0 ]; then
  echo "Compilation failed. Please check for errors."
  exit 1
fi

# === RUN JAVA PROGRAM ===
echo ""
echo "=== Running Main.java ==="
echo "Dataset               : $DATASET"
echo "Number of Criteria    : $NUM_CRITERIA"
echo "Max Memory Partitions : $MAX_MEMORY_PARTITIONS"
echo "Partition Size        : $PARTITION_SIZE"
echo "Force Regenerate      : $REGEN"
echo ""

java -Xmx16G -cp "$OUT_DIR" "$JAVA_MAIN" "$DATASET" "$NUM_CRITERIA" "$MAX_MEMORY_PARTITIONS" "$PARTITION_SIZE" "$REGEN"

# === PLOT RESULTS ===
RESULT_CSV="results/${DATASET%.txt}.csv"
if [ -f "$RESULT_CSV" ]; then
  echo "Plotting results from $RESULT_CSV..."
  python plot.py "$RESULT_CSV"
else
  echo "Results not found: $RESULT_CSV"
fi
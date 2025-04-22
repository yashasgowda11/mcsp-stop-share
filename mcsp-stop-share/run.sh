#!/bin/bash

# === CONFIGURATION ===
SRC_DIR="src"
OUT_DIR="out"
DATASET="central_usa.txt"   # Change this for other datasets
JAVA_MAIN="core.Main"

# === CREATE OUTPUT FOLDERS IF MISSING ===
mkdir -p results
mkdir -p "$OUT_DIR"

# === COMPILE JAVA PROJECT ===
echo "Compiling Java source files..."
javac -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java")
if [ $? -ne 0 ]; then
  echo "Compilation failed. Please check for errors."
  exit 1
fi

# === RUN JAVA PROGRAM ===
echo "Running Main.java on dataset: $DATASET"
java -Xmx16G -cp "$OUT_DIR" "$JAVA_MAIN" "$DATASET"

# === OPTIONAL: REGENERATE DATASET ===
# Uncomment the following line to regenerate the dataset
# echo "Regenerating dataset..."
# java -Xmx4G -cp "$OUT_DIR" "$JAVA_MAIN" "$DATASET" --regen

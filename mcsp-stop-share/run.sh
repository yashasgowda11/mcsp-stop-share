#!/bin/bash

# === CONFIGURATION ===
SRC_DIR="src"
OUT_DIR="out"
DATASET="central_usa.txt"       # Change to orkut.txt, twitter.txt, etc.
SOURCE_NODE=0
TARGET_NODE=100
CRITERIA=3

# === CREATE OUTPUT FOLDERS IF MISSING ===
mkdir -p results
mkdir -p $OUT_DIR

# === COMPILE JAVA PROJECT ===
echo "Compiling Java source files..."
javac -d $OUT_DIR $(find $SRC_DIR -name "*.java")
if [ $? -ne 0 ]; then
  echo "Compilation failed. Please check for errors."
  exit 1
fi

# === RUN PROGRAM ===
echo "Running Main.java on dataset: $DATASET"
java -Xmx4G -cp $OUT_DIR core.Main "$DATASET" "$SOURCE_NODE" "$TARGET_NODE" "$CRITERIA"

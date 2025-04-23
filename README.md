# Multi-Criteria Shortest Path (MCSP) Algorithms 

This project implements the STOP & SHARE framework from the TKDE 2024 paper "I/O-Efficient Multi-Criteria Shortest Paths Query Processing on Large Graphs". It supports four key strategies:

OHP – One-Hop Strategy

MHP – Multi-Hop Strategy

BMHP – Bidirectional Multi-Hop Strategy

BMHPS – Shortcut-enhanced BMHP using overlay graphs

It is designed to work with large-scale real-world datasets such as road networks, social graphs, and web graphs.


## Supported Algorithms

✅ OHP – One-Hop Strategy

Uses N priority queues

Performs one expansion step per criterion

Tracks partition access and cache sharing

✅ MHP – Multi-Hop Strategy

Expands a queue until next partition is unavailable

Dynamically tracks memory/cache reuse

✅ BMHP – Bidirectional Multi-Hop

Uses forward and reverse queues

Stops when fwd_cost + rev_cost > best_cost

✅ BMHPS – Shortcut Overlay Optimization

Builds shortcuts within each partition between boundary nodes

Constructs an overlay graph G~


## 🚀 How to Run

🛠 Step 1: Compile

    javac -d out src/core/*.java src/io/*.java src/model/*.java src/Main.java

▶️ Step 2: Run the Project

    java -Xmx16G -cp out core.Main synthetic_sf.txt 3 50 200 --regen

Parameters:

synthetic_sf.txt: Dataset file (place in data/ folder)
3: Number of edge weight criteria
50: Memory Partitions(Alpha)
200: Boundaries (Nue)
--regen: To regenrate partitions and overlays

🖱 Or simply use the run script:

    ./run.sh

You can simplify the compilation and execution process using the provided run.sh script. This script:

 Compiles all Java source files
 Executes the MCSP query with your specified parameters
 Optionally regenerates partitions
 Generates plots (if results are available)

## 🔧 Usage
    ./run.sh [dataset.txt] [num_criteria] [max_memory_partitions] [partition_size] [--regen]

dataset.txt	 - Graph file (e.g., central_usa.txt, placed in data/)
num_criteria - Number of edge weight criteria (default: 3)
max_memory_partitions	- Max number of partitions to keep in memory (default: 50)
partition_size	- Partition size for disk-based partitioning (default: 200)
--regen	- Optional flag to force regeneration of partitions and Overlay

## ✅ Example

    ./run.sh central_usa.txt 3 50 200 --regen

This compiles the code, runs the MCSP query from core.Main, and plots the results using plot.py if output is available in results/.

## 📁 Output Directory Structure

Compiled Classes: out/
Results: results/<dataset>.csv
Plots: results/<dataset>.png (if generated)

Make sure your dataset is present in the data/ folder and the plot.py script is set up correctly with matplotlib and pandas.

## 📊 Output and Results

Logs: results/log_<dataset>.txt

Metrics (if logging is enabled in code):

query_time_ms

disk_reads

cache_hits

CSV files: results/*.csv

Plots (if plot_results.py is used): results/*.png


## 📈 Plotting Evaluation (Optional)

To plot query time and disk access:

    python plot.py results/"dataset".csv

Make sure you have matplotlib and pandas installed:

    pip install matplotlib pandas


## 🧰 Dependencies & Development Environment

📌 Language Requirements

   Component	           Language	       Version	             Description
________________________________________________________________________________________________
Core Algorithms	       Java	           8 or higher	     OOP and file-based graph processing
Plotting Scripts	     Python	         3.6+	             Optional: for visualization

🔧 Java Setup
To build and run this project, ensure Java is installed:

✅ Install OpenJDK (macOS/Linux):

    brew install openjdk      # macOS
    sudo apt install openjdk-11-jdk  # Ubuntu/Debian

✅ Check Version

    java -version
    javac -version

Must show Java 8 or later.

## 📂 Datasets

This project has been tested on large-scale graph datasets from both social networks and road networks. Below are the supported datasets and their sources:

UK-2007 (Synthetic_sf)	Web graph of the .uk domain	(https://chato.cl/webspam/datasets/uk2007/links/) 
Wikipedia	Wikipedia article hyperlink graph	SNAP  (https://snap.stanford.edu/data/wikipedia-article-networks.html)
Orkut	Social network from Orkut platform	SNAP  (https://snap.stanford.edu/data/com-Orkut.html)
Twitter	Ego networks from Twitter	SNAP  (https://snap.stanford.edu/data/ego-Twitter.html)
California	Road network of California	SNAP  (https://snap.stanford.edu/data/roadNet-CA.html)
Texas	Road network of Texas	SNAP  (https://snap.stanford.edu/data/roadNet-TX.html)

## 📦 Python (for evaluation plots)

Install Python 3 with pip, then:

    pip install matplotlib pandas

You may optionally use a virtual environment:

    python -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt  # if requirements.txt is created


## 🗂 Project Coding Info

🏗 Code Style
Java is structured using modular folders:

  core/ – algorithms & graph logic
  io/ – file access and memory mapping
  model/ – helper objects (e.g., State for PQ)

Prefer CamelCase for Java classes and snake_case for scripts

Use System.currentTimeMillis() for timing; write logs to results/ in .csv format

⚙️ Compilation & Execution
Compile using:

    javac -d out src/core/*.java src/io/*.java src/model/*.java src/Main.java

Run with:

    java -cp out core.Main central_usa.txt 0 100 3

Or use:

    ./run.sh

## 🧪 Testing & Evaluation

You can evaluate correctness or performance using:

Sample queries (source → target pairs)

Timing data

Partition reads (simulated I/O)

Overlay graph shortcut logs

## Reference

If you found any of our implimentations useful, please cite the appropriate references, listed below:

```
@article{zhou2024efficient,
  title={I/O-Efficient Multi-Criteria Shortest Paths Query Processing on Large Graphs},
  author={Zhou, Xinjie and Huang, Kai and Li, Lei and Zhang, Mengxuan and Zhou, Xiaofang},
  journal={IEEE Transactions on Knowledge and Data Engineering},
  year={2024},
  publisher={IEEE}
}
```
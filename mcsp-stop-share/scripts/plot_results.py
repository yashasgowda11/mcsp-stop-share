import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# === CONFIG ===
DATA_DIR = "../results"  # Adjust if needed
ALGORITHMS = ["OHP", "MHP", "BMHP", "BMHPS"]
METRICS = ["query_time_ms", "disk_reads", "cache_hits", "memory_usage_mb"]

# Dataset display names
DATASETS = {
    "central_usa": "Central USA",
    "orkut": "Orkut",
    "wikipedia": "Wikipedia",
    "twitter": "Twitter"
}

# === Load Data ===
def load_data():
    all_data = []
    for algo in ALGORITHMS:
        file = os.path.join(DATA_DIR, f"{algo.lower()}_results.csv")
        if not os.path.exists(file) or os.path.getsize(file) == 0:
            print(f"Warning: File missing or empty: {file}")
            continue
        df = pd.read_csv(file)
        df["Algorithm"] = algo
        all_data.append(df)
    if not all_data:
        raise ValueError("No data found to plot.")
    return pd.concat(all_data, ignore_index=True)

# === Plot Function (Bar Plot) ===
def plot_metric(metric, title):
    df = load_data()
    if metric not in df.columns:
        print(f"Metric '{metric}' not found in data.")
        return

    df["Dataset"] = df["dataset"].map(DATASETS)

    plt.figure(figsize=(10, 6))
    sns.barplot(data=df, x="Dataset", y=metric, hue="Algorithm", palette="Set2", ci="sd")
    plt.title(title)
    plt.ylabel(metric.replace("_", " ").title())
    plt.xlabel("Dataset")
    plt.legend(title="Algorithm")
    plt.tight_layout()
    plt.savefig(os.path.join(DATA_DIR, f"plot_{metric}_bar.png"))
    print(f"Bar plot saved to: plot_{metric}_bar.png")

# === Plot Function (Line Plot) ===
def plot_metric_line(metric, title):
    df = load_data()
    if metric not in df.columns:
        print(f"Metric '{metric}' not found in data.")
        return

    df["Dataset"] = df["dataset"].map(DATASETS)
    
    plt.figure(figsize=(10, 6))
    sns.lineplot(data=df, x="Dataset", y=metric, hue="Algorithm", marker="o", ci="sd")
    plt.title(title)
    plt.ylabel(metric.replace("_", " ").title())
    plt.xlabel("Dataset")
    plt.legend(title="Algorithm")
    plt.tight_layout()
    plt.savefig(os.path.join(DATA_DIR, f"plot_{metric}_line.png"))
    print(f"Line plot saved to: plot_{metric}_line.png")

# === Run All Plots ===
if __name__ == "__main__":
    for metric in METRICS:
        plot_metric(metric, f"Algorithm Comparison – {metric.replace('_', ' ').title()} (Bar)")
        plot_metric_line(metric, f"Algorithm Comparison – {metric.replace('_', ' ').title()} (Line)")

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

sns.set(style="whitegrid")

def plot_all_metrics(df, dataset_name, save_dir="plots"):
    os.makedirs(save_dir, exist_ok=True)

    metrics = {
        'time_ms': 'Query Time (ms)',
        'disk_reads': 'Disk Reads (Count)',
        'cache_hits': 'Cache Hits (Count)',
        'cost_0': 'Cost - Criterion 0',
        'cost_1': 'Cost - Criterion 1',
        'cost_2': 'Cost - Criterion 2'
    }

    # 1. Boxplot & Barplot per metric
    for metric, label in metrics.items():
        plt.figure(figsize=(10, 6))
        sns.boxplot(data=df, x='strategy', y=metric, palette='pastel')
        plt.title(f"{label} Distribution Across Algorithms ({dataset_name})", fontsize=14)
        plt.xlabel("Algorithm", fontsize=12)
        plt.ylabel(label, fontsize=12)
        plt.tight_layout()
        plt.savefig(f"{save_dir}/{dataset_name}_{metric}_boxplot.png")
        plt.close()

        plt.figure(figsize=(10, 6))
        mean_values = df.groupby('strategy')[metric].mean().reset_index()
        sns.barplot(data=mean_values, x='strategy', y=metric, palette='muted')
        plt.title(f"Average {label} Per Algorithm ({dataset_name})", fontsize=14)
        plt.xlabel("Algorithm", fontsize=12)
        plt.ylabel(f"Avg {label}", fontsize=12)
        plt.tight_layout()
        plt.savefig(f"{save_dir}/{dataset_name}_{metric}_barplot.png")
        plt.close()

    # 2. Grouped avg barplot across all metrics
    avg_df = df.groupby('strategy').agg({
        'time_ms': 'mean',
        'disk_reads': 'mean',
        'cache_hits': 'mean',
        'cost_0': 'mean',
        'cost_1': 'mean',
        'cost_2': 'mean'
    }).reset_index().melt(id_vars='strategy', var_name='metric', value_name='value')

    metric_titles = {k: v for k, v in metrics.items()}

    avg_df['metric'] = avg_df['metric'].map(metric_titles)

    plt.figure(figsize=(12, 6))
    sns.barplot(data=avg_df, x='metric', y='value', hue='strategy', palette='deep')
    plt.title(f"Average Metrics Comparison Across Algorithms ({dataset_name})", fontsize=14)
    plt.xlabel("Metric", fontsize=12)
    plt.ylabel("Average Value", fontsize=12)
    plt.xticks(rotation=15)
    plt.legend(title='Algorithm')
    plt.tight_layout()
    plt.savefig(f"{save_dir}/{dataset_name}_grouped_avg_barplot.png")
    plt.close()

def main():
    import argparse
    parser = argparse.ArgumentParser(description="Plot algorithm comparisons from result CSVs")
    parser.add_argument("csv_files", nargs='+', help="Path to one or more results.csv files")
    args = parser.parse_args()

    for file in args.csv_files:
        df = pd.read_csv(file)
        dataset_name = df['dataset'].iloc[0]
        print(f"Plotting for dataset: {dataset_name}")
        plot_all_metrics(df, dataset_name)

if __name__ == "__main__":
    main()

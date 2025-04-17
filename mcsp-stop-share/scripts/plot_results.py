import pandas as pd
import matplotlib.pyplot as plt

def plot_csv(file_path, title):
    df = pd.read_csv(file_path)
    
    plt.figure(figsize=(10, 5))
    plt.bar(df['dataset'], df['query_time_ms'], label='Query Time (ms)')
    plt.title(title)
    plt.xlabel('Dataset')
    plt.ylabel('Query Time (ms)')
    plt.grid(True)
    plt.legend()
    plt.savefig(file_path.replace(".csv", "_query_time.png"))

    plt.figure(figsize=(10, 5))
    plt.bar(df['dataset'], df['disk_reads'], color='orange', label='Disk Reads')
    plt.title(title + " - Disk Reads")
    plt.xlabel('Dataset')
    plt.ylabel('Disk Reads')
    plt.grid(True)
    plt.legend()
    plt.savefig(file_path.replace(".csv", "_disk_reads.png"))

    print(f"Plots saved for: {file_path}")

# Call for all CSVs
plot_csv("../results/ohp_results.csv", "OHP Algorithm")
plot_csv("../results/mhp_results.csv", "MHP Algorithm")
plot_csv("../results/bmhp_results.csv", "BMHP Algorithm")
plot_csv("../results/bmhps_results.csv", "BMHPS Algorithm")

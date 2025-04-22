# generate_overlay_queries.py
import random
import argparse
import os

def extract_overlay_nodes(overlay_path):
    node_ids = set()
    with open(overlay_path, 'r') as f:
        for line in f:
            if line.strip():
                parts = line.strip().split()
                if len(parts) >= 2:
                    u, v = int(parts[0]), int(parts[1])
                    node_ids.add(u)
                    node_ids.add(v)
    return sorted(list(node_ids))

def generate_queries(node_ids, num_queries):
    queries = set()
    while len(queries) < num_queries:
        src = random.choice(node_ids)
        tgt = random.choice(node_ids)
        if src != tgt:
            queries.add((src, tgt))
    return list(queries)

def save_queries(dataset_name, queries):
    output_dir = "queries"
    os.makedirs(output_dir, exist_ok=True)

    output_path = f"{output_dir}/{dataset_name}_queries.txt"
    with open(output_path, 'w') as f:
        for src, tgt in queries:
            f.write(f"{src} {tgt}\n")

    print(f" Saved {len(queries)} overlay-based queries to {output_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset", required=True, help="Dataset filename (e.g. central_usa.txt)")
    parser.add_argument("--count", type=int, default=20, help="Number of queries to generate")
    args = parser.parse_args()

    dataset_name = args.dataset.replace(".txt", "")
    overlay_path = f"partitions/{dataset_name}/overlay.txt"

    if not os.path.exists(overlay_path):
        print(f" Overlay file not found at: {overlay_path}")
        exit(1)

    node_ids = extract_overlay_nodes(overlay_path)
    if len(node_ids) < 2:
        print(" Not enough nodes in overlay to generate queries.")
        exit(1)

    queries = generate_queries(node_ids, args.count)
    save_queries(dataset_name, queries)

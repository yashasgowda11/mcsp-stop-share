# generate_random_queries.py
import random
import argparse
import os


def extract_node_ids(data_path):
    node_ids = set()
    with open(data_path, 'r') as f:
        for line in f:
            if line.strip() and not line.startswith('#'):
                parts = line.strip().split()
                if len(parts) >= 2:
                    node_ids.add(int(parts[0]))
                    node_ids.add(int(parts[1]))
    return sorted(list(node_ids))


def generate_queries(node_ids, num_queries):
    queries = []
    while len(queries) < num_queries:
        src = random.choice(node_ids)
        tgt = random.choice(node_ids)
        if src != tgt:
            queries.append((src, tgt))
    return queries


def save_queries(dataset_name, queries):
    output_dir = "queries"
    os.makedirs(output_dir, exist_ok=True)  # ✅ ensure directory exists

    output_path = f"{output_dir}/{dataset_name}_queries.txt"
    with open(output_path, 'w') as f:
        for src, tgt in queries:
            f.write(f"{src} {tgt}\n")

    print(f" Saved {len(queries)} queries to {output_path}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset", required=True, help="Dataset filename in data/ (e.g. central_usa.txt)")
    parser.add_argument("--count", type=int, default=10, help="Number of queries to generate")
    args = parser.parse_args()

    data_file = f"/Users/yashasgowda/Developer/mcsp-stop-share/mcsp-stop-share/data/{args.dataset}"
    dataset_name = args.dataset.replace(".txt", "")

    node_ids = extract_node_ids(data_file)
    queries = generate_queries(node_ids, args.count)
    save_queries(dataset_name, queries)

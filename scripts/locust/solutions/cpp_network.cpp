#include <iostream>
#include <vector>
#include <queue>
#include <algorithm>

using namespace std;

const int INF = 1e9;

int main() {
    // Fast I/O: Essential for CP platforms to avoid Time Limit Exceeded
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);

    int n, m, k;
    if (!(cin >> n >> m >> k)) return 0;

    // Adjacency list to represent the graph: adj[u] = list of {v, weight}
    vector<vector<pair<int, int>>> adj(n + 1);
    for (int i = 0; i < m; ++i) {
        int u, v, w;
        cin >> u >> v >> w;
        adj[u].push_back({v, w});
    }

    // Distance array initialized to infinity
    vector<int> dist(n + 1, INF);
    dist[k] = 0;

    // Min-heap priority queue to always process the closest node first
    // Stores pairs of {current_distance, node}
    priority_queue<pair<int, int>, vector<pair<int, int>>, greater<pair<int, int>>> pq;
    pq.push({0, k});

    while (!pq.empty()) {
        int d = pq.top().first;
        int u = pq.top().second;
        pq.pop();

        // If we found a shorter path to u previously, skip processing
        if (d > dist[u]) continue;

        // Check all neighboring nodes
        for (auto& edge : adj[u]) {
            int v = edge.first;
            int weight = edge.second;

            // Relaxation step: If we found a faster way to node v, update and push
            if (dist[u] + weight < dist[v]) {
                dist[v] = dist[u] + weight;
                pq.push({dist[v], v});
            }
        }
    }

    // Find the maximum time it took to reach any node
    int max_time = 0;
    for (int i = 1; i <= n; ++i) {
        if (dist[i] == INF) {
            // A node is completely unreachable from the starting node k
            cout << -1 << "\n";
            return 0;
        }
        max_time = max(max_time, dist[i]);
    }

    // Output the final minimum time required to reach all nodes
    cout << max_time << "\n";

    return 0;
}

import java.io.*;
import java.util.*;

public class faah {
    public static void main(String[] args) throws IOException {
        // Fast I/O: Essential for Java to pass strict time limits
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        // Read the first line (n, m, k)
        String line = br.readLine();
        if (line == null) return; // Edge case for empty files
        StringTokenizer st = new StringTokenizer(line);
        
        int n = Integer.parseInt(st.nextToken());
        int m = Integer.parseInt(st.nextToken());
        int k = Integer.parseInt(st.nextToken());

        // Adjacency list: adj.get(u) contains int[]{v, weight}
        List<List<int[]>> adj = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            adj.add(new ArrayList<>());
        }

        // Read all edges
        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            int u = Integer.parseInt(st.nextToken());
            int v = Integer.parseInt(st.nextToken());
            int w = Integer.parseInt(st.nextToken());
            adj.get(u).add(new int[]{v, w});
        }

        // Distance array initialized to "infinity"
        int[] dist = new int[n + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[k] = 0;

        // Min-heap Priority Queue: stores int[]{current_distance, node}
        // Sorted by the distance (index 0)
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, k});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int d = current[0];
            int u = current[1];

            // If we found a shorter path previously, skip
            if (d > dist[u]) continue;

            // Relaxation step
            for (int[] edge : adj.get(u)) {
                int v = edge[0];
                int weight = edge[1];

                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    pq.offer(new int[]{dist[v], v});
                }
            }
        }

        // Find the maximum time to reach any node
        int maxTime = 0;
        for (int i = 1; i <= n; i++) {
            if (dist[i] == Integer.MAX_VALUE) {
                // A node is completely unreachable
                System.out.println(-1);
                return;
            }
            maxTime = Math.max(maxTime, dist[i]);
        }

        // Print the final answer
        System.out.println(maxTime);
    }
}

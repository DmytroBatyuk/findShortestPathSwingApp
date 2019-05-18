// Java implementation of Dijkstra's Algorithm
// using Priority Queue

import java.util.*;

public class DPQCpy {
    private int dist[];
    private Set<Integer> settled;
    private PriorityQueue<NodeCpy> pq;
    private int V; // Number of vertices
    List<List<NodeCpy>> adj;

    public DPQCpy(int V) {
        this.V = V;
        dist = new int[V];
        settled = new HashSet<Integer>();
        pq = new PriorityQueue<NodeCpy>(V, new NodeCpy());
    }

    // Function for Dijkstra's Algorithm
    public void dijkstra(List<List<NodeCpy>> adj, int src) {
        this.adj = adj;

        for (int i = 0; i < V; i++)
            dist[i] = Integer.MAX_VALUE;

        // Add source node to the priority queue
        pq.add(new NodeCpy(src, 0));

        // Distance to the source is 0
        dist[src] = 0;
        while (settled.size() != V) {

            // remove the minimum distance node
            // from the priority queue
            int u = pq.remove().node;

            // adding the node whose distance is
            // finalized
            settled.add(u);

            e_Neighbours(u);
        }
    }

    // Function to process all the neighbours
    // of the passed node
    private void e_Neighbours(int u) {
        int edgeDistance = -1;
        int newDistance = -1;

        // All the neighbors of v
        for (int i = 0; i < adj.get(u).size(); i++) {
            NodeCpy v = adj.get(u).get(i);

            // If current node hasn't already been processed
            if (!settled.contains(v.node)) {
                edgeDistance = v.cost;
                newDistance = dist[u] + edgeDistance;

                // If new distance is cheaper in cost
                if (newDistance < dist[v.node])
                    dist[v.node] = newDistance;

                // Add the current node to the queue
                pq.add(new NodeCpy(v.node, dist[v.node]));
                System.out.println("e_Neighbours: " + u + " -> " + i + " : v=" + v.node + "=" + dist[v.node] + ", calculated");
            } else {
                System.out.println("e_Neighbours: " + u + " -> " + i + " : v=" + v.node + "=" + dist[v.node]);
            }
        }
    }

    // Driver code
    public static void main(String arg[]) {
        int V = 5;
        int source = 0;

        // Adjacency list representation of the
        // connected edges
        List<List<NodeCpy>> adj = new ArrayList<List<NodeCpy>>();

        // Initialize list for every node
        for (int i = 0; i < V; i++) {
            List<NodeCpy> item = new ArrayList<NodeCpy>();
            adj.add(item);
        }

        // Inputs for the DPQ graph
        adj.get(0).add(new NodeCpy(1, 1));
        adj.get(0).add(new NodeCpy(2, 1));
        adj.get(0).add(new NodeCpy(3, 1));

        adj.get(2).add(new NodeCpy(1, 1));
        adj.get(2).add(new NodeCpy(3, 1));
        adj.get(2).add(new NodeCpy(4, 1));

        // Calculate the single source shortest path
        DPQCpy dpq = new DPQCpy(V);
        dpq.dijkstra(adj, source);

        // Print the shortest path to all the nodes
        // from the source node
        System.out.println("The shorted path from node :");
        for (int i = 0; i < dpq.dist.length; i++)
            System.out.println(source + " to " + i + " is "
                    + dpq.dist[i]);
    }
}

// Class to represent a node in the graph
class NodeCpy implements Comparator<NodeCpy> {
    public int node;
    public int cost;

    public NodeCpy() {
    }

    public NodeCpy(int node, int cost) {
        this.node = node;
        this.cost = cost;
    }

    @Override
    public int compare(NodeCpy node1, NodeCpy node2) {
        if (node1.cost < node2.cost)
            return -1;
        if (node1.cost > node2.cost)
            return 1;
        return 0;
    }
}

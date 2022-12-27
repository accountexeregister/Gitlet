import java.util.ArrayDeque;
import java.util.Queue;

public class GitletBreadthFirstPaths {
    private boolean[] marked;
    private int[] edgeTo;
    private int[] distTo;
    private GitletGraph G;

    public GitletBreadthFirstPaths(GitletGraph G, Commit commitS) {
        int s = G.getCommitKeyInGraph(commitS);
        bfs(G, s);
        this.G = G;
    }

    public int distanceTo(Commit destination) {
        int destinationInt = G.getCommitKeyInGraph(destination);
        return distTo[destinationInt];
    }

    private void bfs(GitletGraph G, int s) {
        marked = new boolean[G.vertices()];
        edgeTo = new int[G.vertices()];
        distTo = new int[G.vertices()];
        distTo[s] = 0;
        Queue<Integer> fringe = new ArrayDeque<Integer>();
        fringe.add(s);
        marked[s] = true;
        while (!fringe.isEmpty()) {
            int v = fringe.remove();
            for (int w : G.adj(v)) {
                if (!marked[w]) {
                    fringe.add(w);
                    marked[w] = true;
                    edgeTo[w] = v;
                    distTo[w] = distTo[v] + 1;
                }
            }
        }

    }
}

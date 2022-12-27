import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitletGraph {
    private Map<Integer, List<Integer>> graphMap;
    private Map<Commit, Integer> commitKeyInGraph;
    private int n;

    public GitletGraph() {
        graphMap = new HashMap<>();
        commitKeyInGraph = new HashMap<>();
        n = 0;
    }

    public void addEdge(Commit v, Commit w) {
        if (!commitKeyInGraph.containsKey(v)) {
            commitKeyInGraph.put(v, n++);
        }

        if (!commitKeyInGraph.containsKey(w)) {
            commitKeyInGraph.put(w, n++);
        }

        addEdge(commitKeyInGraph.get(v), commitKeyInGraph.get(w));
    }

    public void addEdge(int v, int w) {
        if (!graphMap.containsKey(v)) {
            graphMap.put(v, new ArrayList<Integer>());
        }

        if (!graphMap.containsKey(w)) {
            graphMap.put(w, new ArrayList<Integer>());
        }

        graphMap.get(v).add(w);
        graphMap.get(w).add(v);
    }

    public List<Integer> adj(int v) {
        return graphMap.get(v);
    }
}

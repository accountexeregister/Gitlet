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

    public boolean containsKey(Commit c) {
        return commitKeyInGraph.containsKey(c);
    }

    public int getCommitKeyInGraph(Commit c) {
        return commitKeyInGraph.get(c);
    }

    public int vertices() {
        return n;
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

    public static GitletGraph createGraph(Commit currentCommit, Commit givenCommit) {
        GitletGraph graph = new GitletGraph();
        addEdges(currentCommit, graph);
        if (!graph.containsKey(givenCommit)) {
            addEdges(givenCommit, graph);
        }
        return graph;
    }

    private static void addEdges(Commit commit, GitletGraph graph) {

        if (commit.getNumOfParents() == 0) {
            return;
        }

        for (String parentCommitId : commit.getParents()) {
            Commit parentCommit = Repository.getCommit(parentCommitId, Repository.OBJECTS);
            if (!graph.containsKey(parentCommit)) {
                graph.addEdge(commit, parentCommit);
                addEdges(parentCommit, graph);
            } else {
                graph.addEdge(commit, parentCommit);
            }
        }
    }
}

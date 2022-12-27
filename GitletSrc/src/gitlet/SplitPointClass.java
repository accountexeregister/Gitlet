package gitlet;

public class SplitPointClass {
    private Commit currentCommit;
    private int currentBranchShortestPath;
    private int givenBranchShortestPath;

    public Commit getCurrentCommit() {
        return currentCommit;
    }

    public int getCurrentBranchShortestPath() {
        return currentBranchShortestPath;
    }

    public int getGivenBranchShortestPath() {
        return givenBranchShortestPath;
    }

    public void setCurrentCommit(Commit currentCommit) {
        this.currentCommit = currentCommit;
    }

    public void setCurrentBranchShortestPath(int currentBranchPath) {
        this.currentBranchShortestPath = currentBranchPath;
    }

    public void setGivenBranchShortestPath(int givenBranchPath) {
        this.givenBranchShortestPath = givenBranchPath;
    }
}

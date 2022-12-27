import Utilities.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable {

    private String message;
    private Date date;
    private List<String> parents = new ArrayList<>();
    // Represents number of parents this commit has
    private int p = 0;
    private List<String> nextCommits = new ArrayList<>();
    // Represents the number of next commits this commit has
    private int n = 0;
    // Maps file name to its SHA1
    private Map<String, String> fileToSHA1 = new HashMap<>();

    public void setNext(Commit next) {
        nextCommits.add(next.toSHA1());
        n++;
    }

    public int getNumOfParents() {
        return p;
    }

    public void addFilesFromStage(Commit parentCommit, Stage stage) {
        for (String commitFileName : parentCommit.getFileNames()) {
            String stageAdditionFileSHA1 = stage.getStagedForAdditionFileSHA1(commitFileName);
            Boolean stageRemovalFileSHA1 = stage.getStagedForRemovalFileSHA1(commitFileName);
            if (stageAdditionFileSHA1 != null) {
                fileToSHA1.put(commitFileName, stageAdditionFileSHA1);
                stage.unstage(commitFileName);
            } else if (stageRemovalFileSHA1 != null && stageRemovalFileSHA1) {
                fileToSHA1.remove(commitFileName);
            } else {
                fileToSHA1.put(commitFileName, parentCommit.fileToSHA1.get(commitFileName));
            }
        }

        for (String fileName : stage.getStageFileNames()) {
            fileToSHA1.put(fileName, stage.getStagedForAdditionFileSHA1(fileName));
        }
        saveCommit();
    }

    public void saveCommit() {
        Repository.writeCommit(this, this.toSHA1(), Repository.OBJECTS);
    }

    public boolean isTracked(String fileName) {
        return fileToSHA1.get(fileName) != null;
    }

    public void setParent(Commit parent) {
        parents.add(parent.toSHA1());
        p++;


    }

    public Commit() {
    }

    public void addCommitDetail(String message) {
        this.message = message;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        date = cal.getTime();
    }

    public boolean isStageable(Stage stage, String fileName, String fileToAddSHA1) {
        return this.fileToSHA1.get(fileName) == null || this.fileToSHA1.get(fileName) != null && stage.getStagedForRemovalFileSHA1(fileName) != null || !(fileToSHA1.get(fileName).equals(fileToAddSHA1)) && stage.getStagedForAdditionFileSHA1(fileName) == null || stage.getStagedForAdditionFileSHA1(fileName) != null && !(stage.getStagedForAdditionFileSHA1(fileName).equals(fileToAddSHA1));
    }

    public boolean fileExists(String fileName) {
        return getFileSHA1(fileName) != null;
    }

    public String getFileSHA1(String fileName) {
        return fileToSHA1.get(fileName);
    }

    // Factory method to create initial commit by calling private constructor Commit()
    public static Commit createInitCommit() {
        Commit commit = new Commit();
        commit.message = "initial commit";
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        cal.setTimeInMillis(0);
        commit.date = cal.getTime();
        return commit;
    }

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        return sdf.format(date);
    }

    public String getMessage() {
        return message;
    }

    public String getFirstParent() {
        if (p == 0) {
            return null;
        }
        return parents.get(0);
    }

    public String toSHA1() {
        String parentsCommitIds = "";
        for (String parentCommitId : parents) {
            parentsCommitIds += parentCommitId;
        }
        return Utils.sha1(this.date.toString() + this.message + this.fileToSHA1 + parentsCommitIds);
    }

    public Set<String> getFileNames() {
        return fileToSHA1.keySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (this.getClass() != o.getClass()) {
            return false;
        }

        Commit oCommit = (Commit) o;
        return this.toSHA1().equals(oCommit.toSHA1());
    }

    public List<String> getNextCommits() {
        return nextCommits;
    }

    public List<String> getParents() {
        return parents;
    }

    @Override
    public int hashCode() {
        return this.toSHA1().hashCode();
    }

    public static Commit getSplitPoint(Commit currentCommit, Commit givenCommit) {
        Commit initialCommit = Utils.readObject(Repository.INITIAL_COMMIT, Commit.class);
        GitletGraph graph = GitletGraph.createGraph(currentCommit, givenCommit);
        GitletBreadthFirstPaths currentCommitBFS = new GitletBreadthFirstPaths(graph, currentCommit);
        GitletBreadthFirstPaths givenCommitBFS = new GitletBreadthFirstPaths(graph, givenCommit);
        SplitPointClass splitPointStruct = new SplitPointClass();
        return getSplitPointBFS(graph, splitPointStruct, initialCommit, currentCommitBFS, givenCommitBFS, 0, 0).getCurrentCommit();
    }

    private static SplitPointClass getSplitPointBFS(GitletGraph G, SplitPointClass splitPointStruct, Commit currentCommit,
                                                    GitletBreadthFirstPaths currentBranchBFS, GitletBreadthFirstPaths givenBranchBFS,
                                                    int currentBranchShortestPath, int givenBranchShortestPath) {

        if (!G.containsKey(currentCommit)) {
            return splitPointStruct;
        }
        int currentBranchPath = currentBranchBFS.distanceTo(currentCommit);
        int givenBranchPath = givenBranchBFS.distanceTo(currentCommit);

        if (currentCommit.getFirstParent() != null) {
            if (currentBranchPath > currentBranchShortestPath) {
                return splitPointStruct;
            }

            if (givenBranchPath > givenBranchShortestPath) {
                return splitPointStruct;
            }
        }

        splitPointStruct.setCurrentBranchShortestPath(currentBranchPath);
        splitPointStruct.setGivenBranchShortestPath(givenBranchPath);

        splitPointStruct.setCurrentCommit(currentCommit);

        for (String nextCommitId : currentCommit.getNextCommits()) {
            Commit nextCommit = Repository.getCommit(nextCommitId, Repository.OBJECTS);
            splitPointStruct = getSplitPointBFS(G, splitPointStruct, nextCommit,
                    currentBranchBFS, givenBranchBFS, splitPointStruct.getCurrentBranchShortestPath(), splitPointStruct.getGivenBranchShortestPath());
        }

        return splitPointStruct;
    }
}

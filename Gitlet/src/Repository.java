import Utilities.Utils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static Utilities.Utils.join;


public class Repository {
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGEDIR = join(GITLET_DIR, "stage");
    public static final File STAGE = Utils.join(STAGEDIR, "stage.txt");
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File REFS_HEADS = join(REFS, "heads");
    // File that stores which branch it is pointing at
    public static final File HEAD = join(GITLET_DIR, "HEAD.txt");
    // Directory that stores objects such as commits
    public static final File OBJECTS = Utils.join(GITLET_DIR, "objects");
    public static final File COMMITS = Utils.join(GITLET_DIR, "commits");
    public static final File INITIAL_COMMIT = Utils.join(GITLET_DIR, "initialcommit.txt");
    public static final int SHA1_LENGTH = 40;

    public static void initGitlet() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        STAGEDIR.mkdir();
        // Commit initCommit = Commit.createInitCommit();
        REFS.mkdir();
        REFS_HEADS.mkdir();
        OBJECTS.mkdir();
        COMMITS.mkdir();
        try {
            INITIAL_COMMIT.createNewFile();
            HEAD.createNewFile();
            STAGE.createNewFile();
            Stage initialStage = new Stage();
            initialStage.saveStage();
        } catch (IOException e) {
            System.exit(0);
        }
        File mainBranch = new File(REFS_HEADS, "master.txt");
        Utils.writeContents(HEAD, "master");
        try {
            mainBranch.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
        commit("initial commit");
    }

    private static String getBranchName(String fileName) {
        return fileName.substring(0, fileName.length() - 4);
    }

    // Creates branch with name given by branchName
    public static void branch(String branchName) {
        String branchFileName = branchName + ".txt";
        File branchFile = Utils.join(REFS_HEADS, branchFileName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            branchFile.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
        String headCommitSHA1 = getHeadCommitSHA1();
        Utils.writeContents(branchFile, headCommitSHA1);
    }

    public static void rmBranch(String branchName) {
        String branchFileName = branchName + ".txt";
        File branchFile = Utils.join(REFS_HEADS, branchFileName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (isHead(branchFile)) {
            System.out.println("Cannot remove the current branch.");
        }
        branchFile.delete();

    }


    public static void status() {
        FileComparator fileComparator = new FileComparator();
        System.out.println("=== Branches ===");
        List<File> refHeadsFiles = new ArrayList<>();
        Collections.addAll(refHeadsFiles, REFS_HEADS.listFiles());
        Collections.sort(refHeadsFiles, fileComparator);
        for (File branchFile : refHeadsFiles) {
            if (isHead(branchFile)) {
                System.out.print("*");
            }
            System.out.println(getBranchName(branchFile.getName()));
        }
        System.out.println();
        Commit headCommit = getHeadCommit();
        Stage stage = getStage();
        System.out.println("=== Staged Files ===");
        List<String> stageFileNames = new ArrayList<>(stage.getStageFileNames());
        Collections.sort(stageFileNames);
        for (String fileName : stageFileNames) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> stageForRemovalFileNames = new ArrayList<>(stage.getStageForRemovalFileNames());
        Collections.sort(stageForRemovalFileNames);
        for (String fileName : stageForRemovalFileNames) {
            System.out.println(fileName);
        }
        System.out.println();
        Set<String> cwdAndStageFileSet = new HashSet<>(headCommit.getFileNames());
        String[] cwdFileList = CWD.list();
        Collections.addAll(cwdAndStageFileSet, cwdFileList);
        cwdAndStageFileSet.addAll(stage.getStageFileNames());
        List<String> cwdAndStageFileList = new ArrayList<>(cwdAndStageFileSet);
        Collections.sort(cwdAndStageFileList);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : cwdAndStageFileList) {
            if (fileName.equals(".gitlet")) {
                continue;
            }
            if (Utils.join(CWD, fileName).exists()) {
                if (isNotStagedAfterModified(headCommit, stage, fileName)) {
                    System.out.println(fileName + " (modified)");
                }
            } else if (isNotStagedAfterRemoved(headCommit, stage, fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String fileName : cwdAndStageFileList) {
            if (fileName.equals(".gitlet")) {
                continue;
            }
            if (Utils.join(CWD, fileName).exists()) {
                if (headCommit.isTracked(fileName) && (stage.isStagedForRemoval(fileName) != null)) {
                    System.out.println(fileName);
                } else if (!(headCommit.isTracked(fileName)) && !(stage.isStagedForAddition(fileName))) {
                    System.out.println(fileName);
                }
            }
        }
        System.out.println();
    }

    private static boolean isNotStagedAfterModified(Commit currentCommit, Stage stage, String fileName) {
        String cwdFileSHA1 = Utils.sha1(Utils.readContentsAsString(Utils.join(CWD, fileName)));
        if (currentCommit.isTracked(fileName)) {
            if (!(currentCommit.getFileSHA1(fileName).equals(cwdFileSHA1)) && !stage.isStagedForAddition(fileName)) {
                return true;
            }
        }
        if (stage.isStagedForAddition(fileName)) {
            return !(stage.getStagedForAdditionFileSHA1(fileName).equals(cwdFileSHA1));
        }
        return false;

    }

    private static boolean isNotStagedAfterRemoved(Commit currentCommit, Stage stage, String fileName) {
        if (stage.isStagedForRemoval(fileName) != null && stage.isStagedForRemoval(fileName)) {
            return false;
        }
        String stagedCommitFileSHA1 = stage.getStagedForAdditionFileSHA1(fileName);
        return (stagedCommitFileSHA1 != null && !(Utils.join(CWD, fileName).exists())) || (currentCommit.isTracked(fileName) && !(Utils.join(CWD, fileName).exists()));
    }

    private static boolean isHead(File branchFile) {
        return (Utils.readContentsAsString(HEAD) + ".txt").equals(branchFile.getName());
    }

    public static void find(String message) {
        if (!find(message, COMMITS)) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    // Finds the commit with given message, printing its id if found and returning true
    // If commit with given message is not found, returns false
    private static boolean find(String message, File currentFile) {
        if (currentFile.isFile()) {
            Commit commit = Utils.readObject(currentFile, Commit.class);
            if (commit.getMessage().equals(message)) {
                System.out.println(commit.toSHA1());
                return true;
            }
            return false;
        }
        boolean atLeastOneCommitWithMessage = false;
        for (File subFile : currentFile.listFiles()) {
            if (find(message, subFile)) {
                atLeastOneCommitWithMessage = true;
            }
        }
        return atLeastOneCommitWithMessage;
    }

    public static void add(String fileName) {
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Stage stage = getStage();
        Commit headCommit = getHeadCommit();
        stage.stageFile(headCommit, fileName);
    }

    public static Stage getStage() {
        return Utils.readObject(STAGE, Stage.class);
    }

    public static void commit(String message, Commit givenCommitForMerge) {
        Commit headCommit = getHeadCommit();
        File headBranchFile = getHeadBranchFile();
        Stage stage = getStage();
        if (headCommit != null) {
            if (!stage.isStageExists(headCommit)) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }
            if (message.length() < 1) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            Commit nextCommit = new Commit();

            if (givenCommitForMerge == null) {
                nextCommit.setParent(headCommit);
                nextCommit.addCommitDetail(message);
                nextCommit.addFilesFromStage(headCommit, stage);
                headCommit.setNext(nextCommit);
            } else {
                nextCommit.setParent(headCommit);
                nextCommit.setParent(givenCommitForMerge);
                nextCommit.addCommitDetail(message);
                nextCommit.addFilesFromStage(headCommit, stage);
                headCommit.setNext(nextCommit);
                givenCommitForMerge.setNext(nextCommit);
            }

            writeCommit(headCommit, headCommit.toSHA1(), OBJECTS);
            writeCommit(headCommit, headCommit.toSHA1(), COMMITS);

            if (givenCommitForMerge != null) {
                writeCommit(givenCommitForMerge, givenCommitForMerge.toSHA1(), OBJECTS);
                writeCommit(givenCommitForMerge, givenCommitForMerge.toSHA1(), COMMITS);
            }

            if (headCommit.getFirstParent() == null) {
                Utils.writeObject(INITIAL_COMMIT, headCommit);
            }
            headCommit = nextCommit;
            createBlobs(headCommit);
            stage.resetStage();
        } else {
            Commit initCommit = Commit.createInitCommit();
            Utils.writeObject(INITIAL_COMMIT, initCommit);
            headCommit = initCommit;
        }
        writeCommit(headCommit, headCommit.toSHA1(), OBJECTS);
        writeCommit(headCommit, headCommit.toSHA1(), COMMITS);
        // Advance branch that is pointed by head
        Utils.writeContents(headBranchFile, headCommit.toSHA1());
    }

    public static void commit(String message) {
        commit(message, null);
    }

    public static void reset(String commitId) {
        Commit commitToResetTo = null;
        try {
            commitToResetTo = getCommit(commitId, OBJECTS);
        } catch (NullPointerException e) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit headCommit = getHeadCommit();
        for (String fileName : CWD.list()) {
            if (fileName.equals(".gitlet")) {
                continue;
            }
            if (!headCommit.isTracked((fileName)) && !commitToResetTo.fileExists(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
            if (headCommit.isTracked(fileName) && !commitToResetTo.fileExists(fileName)) {
                Repository.rm(fileName);
            }
        }
        for (String fileName : commitToResetTo.getFileNames()) {
            Repository.checkout(commitId, fileName);
        }
        Stage stage = getStage();
        stage.resetStage();
        Utils.writeContents(getHeadBranchFile(), commitId);

    }

    public static void globalLog() {
        logFile(COMMITS);
    }

    public static void logFile(File file) {
        if (file.isFile()) {
            Commit commit = Utils.readObject(file, Commit.class);
            printCommitData(commit);
            return;
        }
        for (File subFile : file.listFiles()) {
            logFile(subFile);
        }
    }

    public static void log() {
        Commit headCommit = getHeadCommit();
        Commit currentCommit = headCommit;
        while (currentCommit != null) {
            printCommitData(currentCommit);
            currentCommit = getCommit(currentCommit.getFirstParent(), OBJECTS);
        }
    }

    public static void printCommitData(Commit currentCommit) {
        System.out.println("===");
        System.out.println("commit " + currentCommit.toSHA1());
        System.out.println("Date: " + currentCommit.getDate());
        System.out.println(currentCommit.getMessage());
        System.out.println();
    }

    // Returns commit object based on SHA1 commit starting from directory
    public static Commit getCommit(String commitSHA1, File startingDirectory) {
        if (commitSHA1 == null) {
            return null;
        }
        if (commitSHA1.length() < SHA1_LENGTH) {
            return null;
        }
        String firstTwoCharOfCommitID = commitSHA1.substring(0, 2);
        String restOfCommitID = commitSHA1.substring(2);
        File firstTwoCharComIdDir = Utils.join(startingDirectory, firstTwoCharOfCommitID);
        File restOfComIdFile = Utils.join(firstTwoCharComIdDir, restOfCommitID + ".txt");
        return Utils.readObject(restOfComIdFile, Commit.class);
    }

    private static Commit getHeadCommit() {
        File headBranchFile = getHeadBranchFile();
        return getCommit(Utils.readContentsAsString(headBranchFile), OBJECTS);
    }

    private static File getHeadBranchFile() {
        String headBranchName = Utils.readContentsAsString(HEAD);
        return Utils.join(REFS_HEADS, headBranchName + ".txt");
    }

    private static String getHeadCommitSHA1() {
        String headBranchName = Utils.readContentsAsString(HEAD);
        return Utils.readContentsAsString(Utils.join(REFS_HEADS, headBranchName + ".txt"));
    }

    public static void writeCommit(Commit commit, String sha1, File startingDirectory) {
        File commitFile = createAndGetDirectoryAndFile(sha1, startingDirectory);
        Utils.writeObject(commitFile, commit);
    }

    // Creates file using sha1 starting from startingDirectory, and returns the file created if does not exists, else
    // returns only the file if it already exists
    private static File createAndGetDirectoryAndFile(String sha1, File startingDirectory) {
        String firstTwoCharOfCommitID = sha1.substring(0, 2);
        String restOfCommitID = sha1.substring(2);
        File firstTwoCharComIdDir = Utils.join(startingDirectory, firstTwoCharOfCommitID);
        if (!(firstTwoCharComIdDir.exists())) {
            firstTwoCharComIdDir.mkdir();
        }
        File restOfComIdFile = Utils.join(firstTwoCharComIdDir, restOfCommitID + ".txt");
        try {
            restOfComIdFile.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
        return restOfComIdFile;
    }

    // Create blobs for the commit about to be created
    private static void createBlobs(Commit commit) {
        for (String fileName : commit.getFileNames()) {
            createBlob(commit, fileName);
        }
    }

    // Create blob in .gitlet objects folder for given file
    private static void createBlob(Commit commit, String fileName) {
        String fileSHA1 = commit.getFileSHA1(fileName);
        if (fileSHA1 == null) {
            return;
        }
        File blobFile = createAndGetDirectoryAndFile(fileSHA1, OBJECTS);
        Utils.writeContents(blobFile, Utils.readContentsAsString(Utils.join(CWD, fileName)));
    }

    private static String getBlobContents(Commit commit, String fileName) {
        return Utils.readContentsAsString(getBlobFile(commit, fileName));
    }

    private static File getBlobFile(Commit commit, String fileName) {
        String fileSHA1 = commit.getFileSHA1(fileName);
        return createAndGetDirectoryAndFile(fileSHA1, OBJECTS);
    }

    public static void deleteFiles() {
        deleteFile(CWD);
    }

    private static void deleteFile(File file) {
        File[] fileList = file.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (File subFile : fileList) {
                deleteFile(subFile);
            }
        }
        if (!file.exists()) {
            return;
        }
        if (!file.equals(GITLET_DIR) && !file.equals(CWD)) {
            file.delete();
        }
    }

    public static void rm(String fileName) {
        Commit headCommit = getHeadCommit();
        Stage stage = getStage();
        if (stage.isStagedForAddition(fileName)) {
            stage.unstage(fileName);
        }
        if (headCommit.isTracked(fileName)) {
            stage.stageForRemoval(fileName);
            deleteFile(Utils.join(CWD, fileName));
        }
    }

    private static String getBranchCommitID(String branchFileName) {
        return Utils.readContentsAsString(Utils.join(REFS_HEADS, branchFileName));
    }

    public static void checkout(String commitId, String fileName) {
        try {
            Commit checkedOutCommit = getCommit(commitId, OBJECTS);
            if (!checkedOutCommit.fileExists(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
            File currentFileInRepo = Utils.join(CWD, fileName);
            Utils.writeContents(currentFileInRepo, getBlobContents(checkedOutCommit, fileName));
        } catch (NullPointerException e) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Stage stage = getStage();
        stage.resetStage();
    }

    public static void checkoutBranch(String branchName) {
        String branchFileName = branchName + ".txt";
        File branchFile = Utils.join(REFS_HEADS, branchFileName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (isHead(branchFile)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String branchCommitId = getBranchCommitID(branchFileName);
        Commit branchCommit = getCommit(branchCommitId, OBJECTS);
        List<File> filesOverriden = new ArrayList<>();
        List<File> filesToDelete = new ArrayList<>();
        Commit headCommit = getHeadCommit();
        for (File file : Utils.join(CWD).listFiles()) {
            if (file.getName().equals(".gitlet")) {
                continue;
            }
            if (!headCommit.isTracked(file.getName()) && !branchCommit.fileExists(file.getName())) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            } else if (!branchCommit.fileExists(file.getName())) {
                filesToDelete.add(file);
            } else {
                filesOverriden.add(file);
            }
        }
        for (String fileName : branchCommit.getFileNames()) {
            if (fileName == null) {
                continue;
            }
            File file = Utils.join(CWD, fileName);
            if (file.exists()) {
                continue;
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.exit(0);
            }
            filesOverriden.add(file);
        }
        for (File fileOverriden : filesOverriden) {
            Utils.writeContents(fileOverriden, getBlobContents(branchCommit, fileOverriden.getName()));
        }
        for (File fileToDelete : filesToDelete) {
            fileToDelete.delete();
        }
        Stage stage = getStage();
        stage.resetStage();
        Utils.writeContents(HEAD, branchName);
    }

    public static void checkoutHead(String fileName) {
        checkout(getHeadCommitSHA1(), fileName);
    }

    @Test
    public void testSplitPoint() {
        Commit initCommit = Commit.createInitCommit();
        Commit commit1 = new Commit();
        commit1.setParent(initCommit);
        commit1.addCommitDetail("1");
        initCommit.setNext(commit1);
        Commit commit2 = new Commit();
        commit2.setParent(commit1);
        commit2.addCommitDetail("2");
        commit1.setNext(commit2);


        Commit commit4 = new Commit();
        commit4.setParent(initCommit);
        commit4.addCommitDetail("4");
        initCommit.setNext(commit4);
        Commit commit5 = new Commit();
        commit5.setParent(commit4);
        commit5.setParent(commit1);
        commit5.addCommitDetail("5");
        commit1.setNext(commit5);
        commit4.setNext(commit5);
        Commit commit6 = new Commit();
        commit6.setParent(commit5);
        commit6.addCommitDetail("6");
        commit5.setNext(commit6);

        Commit commit3 = new Commit();
        commit3.setParent(commit2);
        commit3.setParent(commit5);
        commit3.addCommitDetail("3");
        commit2.setNext(commit3);
        commit5.setNext(commit3);

        Commit commit7 = new Commit();
        commit7.setParent(commit3);
        commit7.addCommitDetail("7");
        commit3.setNext(commit7);

        Commit commit8 = new Commit();
        commit8.setParent(commit7);
        commit8.addCommitDetail("8");
        commit7.setNext(commit8);

        Commit commit9 = new Commit();
        commit9.setParent(commit7);
        commit9.addCommitDetail("9");
        commit7.setNext(commit9);

        Commit commit10 = new Commit();
        commit10.setParent(commit6);
        commit10.setParent(commit3);
        commit10.addCommitDetail("10");
        commit6.setNext(commit10);
        commit3.setNext(commit10);


        initGitlet();
        writeCommit(initCommit, initCommit.toSHA1(), OBJECTS);
        writeCommit(commit1, commit1.toSHA1(), OBJECTS);
        writeCommit(commit2, commit2.toSHA1(), OBJECTS);
        writeCommit(commit3, commit3.toSHA1(), OBJECTS);
        writeCommit(commit4, commit4.toSHA1(), OBJECTS);
        writeCommit(commit5, commit5.toSHA1(), OBJECTS);
        writeCommit(commit6, commit6.toSHA1(), OBJECTS);
        writeCommit(commit7, commit7.toSHA1(), OBJECTS);
        writeCommit(commit8, commit8.toSHA1(), OBJECTS);
        writeCommit(commit9, commit9.toSHA1(), OBJECTS);
        writeCommit(commit10, commit10.toSHA1(), OBJECTS);


        Utils.writeObject(INITIAL_COMMIT, initCommit);
        System.out.println(getSplitPoint(commit4, commit2).getMessage());
    }

    public static void merge(String givenBranchName) {
        String givenBranchCommitId = getBranchCommitID(givenBranchName + ".txt");
        Commit givenBranchCommit = getCommit(givenBranchCommitId, OBJECTS);
        Commit headCommit = getHeadCommit();
        Commit splitPoint = getSplitPoint(headCommit, givenBranchCommit);
        if (givenBranchCommit.equals(splitPoint)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        if (headCommit.equals(splitPoint)) {
            checkoutBranch(givenBranchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        for (String fileName : splitPoint.getFileNames()) {
            if (isModifiedFromSplitPoint(givenBranchCommit, splitPoint, fileName) && !isModifiedFromSplitPoint(headCommit, splitPoint, fileName)) {
                checkout(givenBranchCommitId, fileName);
                add(fileName);
            }
        }

        String currentBranch = getBranchName(getHeadBranchFile().getName());
        String commitMessage = "Merged " + givenBranchName + " into " + currentBranch + ".";
        commit(commitMessage, givenBranchCommit);


    }

    private static boolean isModifiedFromSplitPoint(Commit commit, Commit splitPoint, String splitPointFileName) {
        String commitFileSHA1 = commit.getFileSHA1(splitPointFileName);
        String splitPointFileSHA1 = splitPoint.getFileSHA1(splitPointFileName);
        return !commitFileSHA1.equals(splitPointFileSHA1);
    }

    public static void getSplitPointMessage(String branchName) {
        String branchCommitId = getBranchCommitID(branchName + ".txt");
        Commit branchCommit = getCommit(branchCommitId, OBJECTS);
        Commit headCommit = getHeadCommit();
        System.out.println(getSplitPoint(headCommit, branchCommit).getMessage());
    }

    public static Commit getSplitPoint(Commit currentCommit, Commit givenCommit) {
        Commit initialCommit = Utils.readObject(INITIAL_COMMIT, Commit.class);
        GitletGraph graph = createGraph(currentCommit, givenCommit);
        GitletBreadthFirstPaths currentCommitBFS = new GitletBreadthFirstPaths(graph, currentCommit);
        GitletBreadthFirstPaths givenCommitBFS = new GitletBreadthFirstPaths(graph, givenCommit);
        SplitPointClass splitPointStruct = new SplitPointClass();
        return getSplitPointBFS(graph, splitPointStruct, currentCommit, givenCommit, initialCommit, currentCommitBFS, givenCommitBFS, 0, 0).getCurrentCommit();
    }


    private static SplitPointClass getSplitPointBFS(GitletGraph G, SplitPointClass splitPointStruct, Commit currentBranchCommit, Commit givenBranchCommit, Commit currentCommit,
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
            Commit nextCommit = getCommit(nextCommitId, OBJECTS);
            splitPointStruct = getSplitPointBFS(G, splitPointStruct, currentBranchCommit, givenBranchCommit, nextCommit,
                    currentBranchBFS, givenBranchBFS, splitPointStruct.getCurrentBranchShortestPath(), splitPointStruct.getGivenBranchShortestPath());
        }

        return splitPointStruct;
    }

    private static GitletGraph createGraph(Commit currentCommit, Commit givenCommit) {
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
            Commit parentCommit = getCommit(parentCommitId, OBJECTS);
            if (!graph.containsKey(parentCommit)) {
                graph.addEdge(commit, parentCommit);
                addEdges(parentCommit, graph);
            } else {
                graph.addEdge(commit, parentCommit);
            }
        }
    }
}

import Utilities.Utils;

import java.io.File;
import java.io.IOException;

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
    public static final File STAGE = join(GITLET_DIR, "stage");
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File REFS_HEADS = join(REFS, "heads");
    // File that stores which branch it is pointing at
    public static final File HEAD = join(GITLET_DIR, "HEAD.txt");
    // Directory that stores objects such as commits
    public static final File OBJECTS = Utils.join(GITLET_DIR, "objects");
    public static final File COMMITS = Utils.join(GITLET_DIR, "commits");
    public static final int SHA1_LENGTH = 40;

    public static void initGitlet() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        STAGE.mkdir();
        // Commit initCommit = Commit.createInitCommit();
        REFS.mkdir();
        REFS_HEADS.mkdir();
        OBJECTS.mkdir();
        COMMITS.mkdir();
        try {
            HEAD.createNewFile();
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

    public static void status() {
        System.out.println("=== Branches ===");
        for (File branchFile : REFS_HEADS.listFiles()) {
            if (isHead(branchFile)) {
                System.out.print("*");
            }
            System.out.println(getBranchName(branchFile.getName()));
        }
        System.out.println();
        Commit headCommit = getHeadCommit();
        Commit stagingCommit = headCommit.getNextStagedCommit();
        System.out.println("=== Staged Files ===");
        for (String fileName : stagingCommit.getFileNames()) {
            if (fileName == null) {
                continue;
            }
            if (!(stagingCommit.getFileSHA1(fileName).equals(headCommit.getFileSHA1(fileName)))) {
                System.out.println(fileName);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");

    }

    private static boolean isHead(File branchFile) {
        return Utils.readContentsAsString(getHeadBranchFile()).equals(Utils.readContentsAsString(branchFile));
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
        Commit headCommit = getHeadCommit();
        headCommit.stageFile(fileName);
    }


    public static void commit(String message) {
        Commit headCommit = getHeadCommit();
        File headBranchFile = getHeadBranchFile();
        if (headCommit != null) {
            if (!headCommit.isStageExists()) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }
            if (message.length() < 1) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            Commit headNextStagedCommit = headCommit.getNextStagedCommit();
            headNextStagedCommit.addCommitDetail(message);
            writeCommit(headNextStagedCommit, headNextStagedCommit.toSHA1(), OBJECTS);
            writeCommit(headNextStagedCommit, headNextStagedCommit.toSHA1(), COMMITS);
            createBlobs(headNextStagedCommit);
            // Remove stage commit sha1 and make a new one
            deleteFile(Utils.join(STAGE, headNextStagedCommit.toStatusSHA1()));
            headCommit.resetStage();
            headCommit = headNextStagedCommit;
        } else {
            Commit initCommit = Commit.createInitCommit();
            headCommit = initCommit;
        }
        createAndGetDirectoryAndFile(headCommit.toSHA1(), COMMITS);
        Commit nextStagedCommit = new Commit();
        nextStagedCommit.setParent(headCommit);
        nextStagedCommit.setStage(headCommit);
        headCommit.setNext(nextStagedCommit);
        writeCommit(nextStagedCommit, nextStagedCommit.toStatusSHA1(), STAGE);
        writeCommit(headCommit, headCommit.toSHA1(), OBJECTS);
        writeCommit(headCommit, headCommit.toSHA1(), COMMITS);
        // Advance branch that is pointed by head
        Utils.writeContents(headBranchFile, headCommit.toSHA1());
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
            currentCommit = getCommit(currentCommit.getParent(), OBJECTS);
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
        if (headCommit.isStagedForAddition(fileName)) {
            headCommit.unstage(fileName);
        }
        if (headCommit.isTracked(fileName)) {
            headCommit.stageForRemoval(fileName);
            deleteFile(Utils.join(CWD, fileName));
        }
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
    }

    public static void checkoutHead(String fileName) {
        checkout(getHeadCommitSHA1(), fileName);
    }
}

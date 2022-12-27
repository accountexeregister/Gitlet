import Utilities.Utils;

import java.io.File;
import java.io.IOException;

import static Utilities.Utils.join;


public class Repository {
    /**
     @@ -20,10 +23,38 @@ public class Repository {
      * variable is used. We've provided two examples for you.
     */

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

    public static void initGitlet() {
        GITLET_DIR.mkdir();
        STAGE.mkdir();
        Commit initCommit = Commit.createInitCommit();
        REFS.mkdir();
        REFS_HEADS.mkdir();
        OBJECTS.mkdir();
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
        File mainBranch = new File(REFS_HEADS, "master.txt");
        try {
            mainBranch.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
        createCommitDirectory(initCommit, OBJECTS);
        Utils.writeContents(mainBranch, initCommit.toSHA1());
        Utils.writeContents(HEAD, Utils.readContentsAsString(mainBranch));
        commit();
    }

    public static void add(String fileName) {
        Commit headCommit = getHeadCommit();
        headCommit.stageFile(fileName);
    }

    public static void commit() {
        Commit headCommit = getHeadCommit();
        Commit nextStagedCommit = new Commit();
        createCommitDirectory(nextStagedCommit, STAGE);
        headCommit.setNext(nextStagedCommit);
        nextStagedCommit.setParent(headCommit);
        nextStagedCommit.setStage(headCommit);
    }

    public static void log() {
        Commit headCommit = getHeadCommit();
        Commit currentCommit = headCommit;
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentCommit.toSHA1());
            System.out.println("Date: " + currentCommit.getDate());
            System.out.println(currentCommit.getMessage());
            System.out.println();
            currentCommit = getCommit(currentCommit.getParent(), OBJECTS);
        }
    }

    // Returns commit object based on SHA1 commit starting from directory
    public static Commit getCommit(String commitSHA1, File startingDirectory) {
        if (commitSHA1 == null) {
            return null;
        }
        String firstTwoCharOfCommitID = commitSHA1.substring(0, 2);
        String restOfCommitID = commitSHA1.substring(2);
        File firstTwoCharComIdDir = Utils.join(startingDirectory, firstTwoCharOfCommitID);
        File restOfComIdFile = Utils.join(firstTwoCharComIdDir, restOfCommitID + ".txt");
        return Utils.readObject(restOfComIdFile, Commit.class);
    }

    private static Commit getHeadCommit() {
        return getCommit(Utils.readContentsAsString(HEAD), OBJECTS);
    }

    private static void createCommitDirectory(Commit commit, File startingDirectory) {
        String commitSHA1 = commit.toSHA1();
        String firstTwoCharOfCommitID = commitSHA1.substring(0, 2);
        String restOfCommitID = commitSHA1.substring(2);
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
        Utils.writeObject(restOfComIdFile, commit);
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
        if (!file.equals(GITLET_DIR) && !file.equals(CWD)) {
            file.delete();
        }
    }

    /*
    private static String commitToSHA1(Commit commit) {
        return Utils.sha1(commit.toString());
    }
     */
}

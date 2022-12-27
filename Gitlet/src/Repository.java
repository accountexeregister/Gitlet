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
    public static final int SHA1_LENGTH = 40;

    public static void initGitlet() {
        GITLET_DIR.mkdir();
        STAGE.mkdir();
        // Commit initCommit = Commit.createInitCommit();
        REFS.mkdir();
        REFS_HEADS.mkdir();
        OBJECTS.mkdir();
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

    public static void add(String fileName) {
        Commit headCommit = getHeadCommit();
        headCommit.stageFile(fileName);
    }

    public static void commit(String message) {
        Commit headCommit = getHeadCommit();
        File headBranchFile = getHeadBranchFile();
        if (headCommit != null) {
            Commit headNextStagedCommit = headCommit.getNextStagedCommit();
            headNextStagedCommit.addCommitDetail(message);
            writeCommit(headNextStagedCommit, headNextStagedCommit.toSHA1(), OBJECTS);
            createBlobs(headNextStagedCommit);
            headCommit = headNextStagedCommit;
        } else {
            Commit initCommit = Commit.createInitCommit();
            headCommit = initCommit;
        }
        Commit nextStagedCommit = new Commit();
        nextStagedCommit.setParent(headCommit);
        nextStagedCommit.setStage(headCommit);
        headCommit.setNext(nextStagedCommit);
        writeCommit(nextStagedCommit, nextStagedCommit.toStatusSHA1(), STAGE);
        writeCommit(headCommit, headCommit.toSHA1(), OBJECTS);
        // Advance branch that is pointed by head
        Utils.writeContents(headBranchFile, headCommit.toSHA1());
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
        for (String fileName : commit.getFileList()) {
            createBlob(commit, fileName);
        }
    }

    // Create blob in .gitlet objects folder for given file
    private static void createBlob(Commit commit, String fileName) {
        String fileSHA1 = commit.getFileSHA1(fileName);
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

    public static void checkout(String commitId, String fileName) {
        Commit checkedOutCommit = getCommit(commitId, OBJECTS);
        File currentFileInRepo = Utils.join(CWD, fileName);
        Utils.writeContents(currentFileInRepo, getBlobContents(checkedOutCommit, fileName));
    }

    public static void checkoutHead(String fileName) {
        checkout(getHeadCommitSHA1(), fileName);
    }
}

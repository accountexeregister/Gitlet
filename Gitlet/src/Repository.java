import Utilities.Utils;

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
        Commit stageCommit = headCommit.getNextStagedCommit();
        System.out.println("=== Staged Files ===");
        List<String> stageFileNames = new ArrayList<>(stageCommit.getStageFileNames());
        Collections.sort(stageFileNames);
        for (String fileName : stageFileNames) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> stageForRemovalFileNames = new ArrayList<>(stageCommit.getStageForRemovalFileNames());
        Collections.sort(stageForRemovalFileNames);
        for (String fileName : stageForRemovalFileNames) {
            System.out.println(fileName);
        }
        System.out.println();
        Set<String> cwdAndStageFileSet = new HashSet<>(stageCommit.getFileNames());
        String[] cwdFileList = CWD.list();
        Collections.addAll(cwdAndStageFileSet, cwdFileList);
        List<String> cwdAndStageFileList = new ArrayList<>(cwdAndStageFileSet);
        Collections.sort(cwdAndStageFileList);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : cwdAndStageFileList) {
            if (fileName.equals(".gitlet")) {
                continue;
            }
            if (Utils.join(CWD, fileName).exists()) {
                if (stageCommit.isTracked(fileName) && isNotStagedAfterModified(headCommit, fileName)) {
                    System.out.println(fileName + " (modified)");
                }
            } else if (isNotStagedAfterRemoved(headCommit, fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String fileName : cwdAndStageFileList) {
            if (fileName.equals(".gitlet")) {
                continue;
            }
            if (Utils.join(CWD, fileName).exists() && !(stageCommit.isTracked(fileName))) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    private static boolean isNotStagedAfterModified(Commit currentCommit, String fileName) {
        String cwdFileSHA1 = Utils.sha1(Utils.readContentsAsString(Utils.join(CWD, fileName)));
        String stagedCommitFileSHA1 = currentCommit.getNextStagedCommit().getFileSHA1(fileName);
        return !(cwdFileSHA1.equals(stagedCommitFileSHA1));
    }

    private static boolean isNotStagedAfterRemoved(Commit currentCommit, String fileName) {
        if (currentCommit.isStagedForRemoval(fileName)) {
            return false;
        }
        String stagedCommitFileSHA1 = currentCommit.getNextStagedCommit().getFileSHA1(fileName);
        return (stagedCommitFileSHA1 != null && !(Utils.join(CWD, fileName).exists())) || (currentCommit.getNextStagedCommit().isTracked(fileName) && !(Utils.join(CWD, fileName).exists()));
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

    public static void commit(String message) {
        Commit headCommit = getHeadCommit();
        File headBranchFile = getHeadBranchFile();
        Stage stage = getStage();
        if (headCommit != null) {
            if (!headCommit.isStageExists()) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }
            if (message.length() < 1) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            Commit nextCommit = new Commit();
            nextCommit.setParent(headCommit);
            headCommit.setNext(nextCommit);
            nextCommit.addFilesFromStage(headCommit, stage);
            headCommit = nextCommit;
            headCommit.addCommitDetail(message);
            createBlobs(headCommit);
            stage.resetStage();
        } else {
            Commit initCommit = Commit.createInitCommit();
            headCommit = initCommit;
        }
        writeCommit(headCommit, headCommit.toSHA1(), OBJECTS);
        writeCommit(headCommit, headCommit.toSHA1(), COMMITS);
        // Advance branch that is pointed by head
        Utils.writeContents(headBranchFile, headCommit.toSHA1());
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
        headCommit.resetStage();
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
        headCommit.resetStage();
        Utils.writeContents(HEAD, branchName);
    }

    public static void checkoutHead(String fileName) {
        checkout(getHeadCommitSHA1(), fileName);
    }
}

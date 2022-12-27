import Utilities.Utils;
import org.junit.Test;

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
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File REFS_HEADS = join(REFS, "heads");
    // File that stores which branch it is pointing at
    public static final File HEAD = join(GITLET_DIR, "HEAD.txt");
    // Directory that stores objects such as commits
    public static final File OBJECTS = Utils.join(GITLET_DIR, "objects");

    public static void initGitlet() {
        GITLET_DIR.mkdir();
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
        createCommitDirectory(initCommit);
        Utils.writeContents(mainBranch, commitToSHA1(initCommit));
        Utils.writeContents(HEAD, Utils.readContentsAsString(mainBranch));
    }

    private static void createCommitDirectory(Commit commit) {
        String commitSHA1 = commitToSHA1(commit);
        String firstTwoCharOfCommitID = commitSHA1.substring(0, 2);
        String restOfCommitID = commitSHA1.substring(2);
        File firstTwoCharComIdDir = Utils.join(OBJECTS, firstTwoCharOfCommitID);
        if (!(firstTwoCharComIdDir.exists())) {
            firstTwoCharComIdDir.mkdir();
        }
        File restOfComIdFile = Utils.join(firstTwoCharComIdDir, restOfCommitID + ".txt");
        try {
            restOfComIdFile.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
    }

    private void changeCurrentBranch() {

    }

    private static String commitToSHA1(Commit commit) {
        return Utils.sha1(commit.toString());
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

    @Test
    public void convertCommitToSHA1() {
        Commit commit = new Commit("test");
        String commitSHA1 = Utils.sha1(commit.toString());
        System.out.println(commitSHA1);
    }
}

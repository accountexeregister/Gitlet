import java.io.File;
import java.io.IOException;

import static gitlet.Utils.join;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
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

    /* TODO: fill in the rest of this class. */
    public static void initGitlet() {
        GITLET_DIR.mkdir();
        Commit initCommit = Commit.createInitCommit();
        REFS.mkdir();
        REFS_HEADS.mkdir();
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
        File mainBranch = new File(REFS_HEADS, "main.txt");
        try {
            mainBranch.createNewFile();
        } catch (IOException e) {
            System.exit(0);
        }
        // Utils.writeContents(mainBranch, Utils.sha1(initCommit));
        // File initCommitDirect = new File(Utils.sha1(initCommit));
        // initCommitDirect.mkdir();
    }
}

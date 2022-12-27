import Utilities.Utils;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable {

    private String message;
    private Date date;
    private String parent;
    private String nextStagedCommit;
    // Maps file name to its SHA1
    private Map<String, String> fileToSHA1 = new HashMap<>();
    // List of files stored by the commit
    private List<String> fileList = new ArrayList<>();

    // Creates the initial commit by creating initial commit message, setting date to epoch time and setting its parent to null
    public Commit(String message) {
        this.message = message;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        date = cal.getTime();
        parent = null;
    }

    public void setNext(Commit nextCommitStaged) {
        this.nextStagedCommit = nextCommitStaged.toStatusSHA1();
    }

    public List<String> getFileList() {
        return fileList;
    }

    public String getFileSHA1(String fileName) {
        return fileToSHA1.get(fileName);
    }

    public String getNextStagedCommitString() {
        return nextStagedCommit;
    }

    public void setParent(Commit parent) {
        this.parent = parent.toSHA1();
    }

    // Factory method to create initial commit by calling private constructor Commit()
    public static Commit createInitCommit() {
        Commit commit = new Commit("initial commit");
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

    public String getParent() {
        return this.parent;
    }

    public String toSHA1() {
        return Utils.sha1(this.date.toString() + this.message + this.fileList + this.fileToSHA1 + this.parent);
    }

    public String toStatusSHA1() {
        return parent;
    }

    public Commit getNextStagedCommit() {
        return Repository.getCommit(nextStagedCommit, Repository.STAGE);
    }

    public boolean isStageable(String fileName, String fileToAddSHA1) {
        return fileToSHA1.get(fileName) == null || !(fileToSHA1.get(fileName).equals(fileToAddSHA1));
    }

    public void stageFile(String fileName) {
        Commit nextStagedCommitObj = getNextStagedCommit();
        File fileToAdd = Utils.join(Repository.CWD, fileName);
        String fileToAddSHA1 = Utils.sha1(Utils.readContentsAsString(fileToAdd));
        if (isStageable(fileName, fileToAddSHA1)) {
            if (fileToSHA1.get(fileName) == null) {
                nextStagedCommitObj.fileList.add(fileName);
            }
            nextStagedCommitObj.fileToSHA1.put(fileName, fileToAddSHA1);
        } else {
            String originalSha1OfFile = fileToSHA1.get(fileName);
            if (originalSha1OfFile == null || !(nextStagedCommitObj.fileToSHA1.get(fileName).equals(originalSha1OfFile))) {
                nextStagedCommitObj.fileToSHA1.put(fileName, originalSha1OfFile);
            }
        }
    }

    // Sets the stage for the commit which is in staging mode (Must only be called for commit about to be commited next
    public void setStage(Commit parent) {
        for (String fileName : parent.fileList) {
            fileList.add(fileName);
            fileToSHA1.put(fileName, parent.fileToSHA1.get(fileName));
        }
    }


}

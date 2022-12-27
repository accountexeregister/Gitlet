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
    private Map<String, Boolean> stageFileToSha1 = new HashMap<>();
    private Map<String, Boolean> stageRemoveFileToSha1 = new HashMap<>();
    /*
        // List of files stored by the commit
        private List<String> fileList = new ArrayList<>();
    */
    // Checks if a file has been staged on this commit
    private boolean stageExists = false;

    public void setNext(Commit nextCommitStaged) {
        this.nextStagedCommit = nextCommitStaged.toStatusSHA1();
    }

    public String getNextStagedCommitString() {
        return nextStagedCommit;
    }

    public void addFilesFromStage(Commit parentCommit, Stage stage) {
        for (String commitFileName : parentCommit.getFileNames()) {
            String stageAdditionFileSHA1 = stage.getStagedForAdditionFileSHA1(commitFileName);
            boolean stageRemovalFileSHA1 = stage.getStagedForRemovalFileSHA1(commitFileName);
            if (stageAdditionFileSHA1 != null) {
                fileToSHA1.put(commitFileName, stageAdditionFileSHA1);
                stage.unstage(commitFileName);
            } else if (stageRemovalFileSHA1) {
                fileToSHA1.remove(commitFileName);
            } else {
                fileToSHA1.put(commitFileName, parentCommit.fileToSHA1.get(commitFileName));
            }
        }

        for (String fileName : stage.getStageFileNames()) {
            fileToSHA1.put(fileName, stage.getStagedForAdditionFileSHA1(fileName));
        }
    }

    public boolean isStageExists() {
        return stageExists;
    }

    public boolean isStagedForAddition(String fileName) {
        if (fileToSHA1.get(fileName) == null && getNextStagedCommit().fileToSHA1.get(fileName) != null) {
            return true;
        }
        return !(fileToSHA1.get(fileName).equals(getNextStagedCommit().fileToSHA1.get(fileName)));
    }

    public boolean isStagedForRemoval(String fileName) {
        return stageRemoveFileToSha1.get(fileName) != null;
    }

    public void unstage(String fileName) {
        Commit nextStagedCommit = getNextStagedCommit();
        nextStagedCommit.fileToSHA1.put(fileName, fileToSHA1.get(fileName));
        nextStagedCommit.stageFileToSha1.remove(fileName);
        Repository.writeCommit(nextStagedCommit, nextStagedCommit.toStatusSHA1(), Repository.STAGE);
    }

    public boolean isTracked(String fileName) {
        return fileToSHA1.get(fileName) != null;
    }

    public void stageForRemoval(String fileName) {
        Commit nextStagedCommit = getNextStagedCommit();
        nextStagedCommit.fileToSHA1.put(fileName, null);
        // nextStagedCommit.stageFileToSha1.remove(fileName);
        nextStagedCommit.stageRemoveFileToSha1.put(fileName, true);
        stageExists = true;
        Repository.writeCommit(nextStagedCommit, nextStagedCommit.toStatusSHA1(), Repository.STAGE);
        Repository.writeCommit(this, toSHA1(), Repository.OBJECTS);
    }

    public void setParent(Commit parent) {
        this.parent = parent.toSHA1();
    }

    public Commit() {
        parent = null;
    }

    public void addCommitDetail(String message) {
        this.message = message;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        date = cal.getTime();
    }

    public boolean isAtEndOfBranch() {
        return getNextStagedCommit().getNextStagedCommit() == null;
    }

    // Returns true if its a staging commit and not actual commit
    public boolean isNotACommit() {
        return message == null && date == null;
    }

    public Commit resetStage() {
        Commit nextStageCommit = new Commit();
        nextStageCommit.setParent(this);
        this.setNext(nextStageCommit);
        nextStageCommit.setStage(this);
        Repository.writeCommit(nextStageCommit, nextStageCommit.toStatusSHA1(), Repository.STAGE);
        Repository.writeCommit(this, this.toSHA1(), Repository.OBJECTS);
        return nextStageCommit;
    }

    public Commit getNextStagedCommit() {
        return Repository.getCommit(nextStagedCommit, Repository.STAGE);
    }

    public boolean isStageable(String fileName, String fileToAddSHA1) {
        Commit stagedCommit = getNextStagedCommit();
        return stagedCommit.fileToSHA1.get(fileName) == null || !(stagedCommit.fileToSHA1.get(fileName).equals(fileToAddSHA1));
    }

    public void stageFile(String fileName) {
        Commit nextStagedCommitObj = getNextStagedCommit();
        File fileToAdd = Utils.join(Repository.CWD, fileName);
        String fileToAddSHA1 = Utils.sha1(Utils.readContentsAsString(fileToAdd));
        if (isStageable(fileName, fileToAddSHA1)) {
            nextStagedCommitObj.fileToSHA1.put(fileName, fileToAddSHA1);
            nextStagedCommitObj.stageFileToSha1.put(fileName, true);
            nextStagedCommitObj.stageRemoveFileToSha1.remove(fileName);
            stageExists = true;
        } else {
            nextStagedCommitObj.fileToSHA1.put(fileName, null);
            nextStagedCommitObj.stageFileToSha1.remove(fileName);
            stageExists = false;
        }
        Repository.writeCommit(nextStagedCommitObj, nextStagedCommitObj.toStatusSHA1(), Repository.STAGE);
        Repository.writeCommit(this, this.toSHA1(), Repository.OBJECTS);
    }

    // Sets the stage for the commit which is in staging mode (Must only be called for commit about to be commited next
    public void setStage(Commit parent) {
        for (String fileName : parent.fileToSHA1.keySet()) {
            fileToSHA1.put(fileName, parent.fileToSHA1.get(fileName));
        }
    }

    /*
    public List<String> getFileList() {
        return fileList;
    }
    */

    public File getFile(String fileName) {
        return new File(getFileSHA1(fileName));
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

    public String getParent() {
        return this.parent;
    }

    public String toSHA1() {
        return Utils.sha1(this.date.toString() + this.message + this.fileToSHA1 + this.parent);
    }

    public String toStatusSHA1() {
        return parent;
    }

    public Set<String> getFileNames() {
        return fileToSHA1.keySet();
    }

    public Set<String> getStageFileNames() {
        return stageFileToSha1.keySet();
    }

    public Set<String> getStageForRemovalFileNames() {
        return stageRemoveFileToSha1.keySet();
    }
}

import Utilities.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Stage implements Serializable {
    private Map<String, String> stageFileToSha1 = new HashMap<>();
    private Map<String, Boolean> stageRemoveFileToSha1 = new HashMap<>();

    public void stageFile(Commit headCommit, String fileName) {

        File fileToAdd = Utils.join(Repository.CWD, fileName);
        String fileToAddSHA1 = Utils.sha1(Utils.readContentsAsString(fileToAdd));
        if (headCommit.isStageable(this, fileName, fileToAddSHA1)) {
            stageFileToSha1.put(fileName, fileToAddSHA1);
            stageRemoveFileToSha1.remove(fileName);
        } else {
            stageFileToSha1.remove(fileName);
        }
        saveStage();
    }

    public void resetStage() {
        stageFileToSha1 = new HashMap<String, String>();
        stageRemoveFileToSha1 = new HashMap<String, Boolean>();
        saveStage();
    }

    public boolean isStagedForAddition(String fileName) {
        return stageFileToSha1.get(fileName) != null;
    }

    public boolean isStagedForRemoval(String fileName) {
        return stageRemoveFileToSha1.get(fileName);
    }

    public void unstage(String fileName) {
        stageFileToSha1.remove(fileName);
        saveStage();
    }

    public void stageForRemoval(String fileName) {
        stageRemoveFileToSha1.put(fileName, true);
        saveStage();
    }

    public boolean isStageable(Commit headCommit, String fileName, String fileToAddSHA1) {
        return stageFileToSha1.get(fileName) == null || !(stageFileToSha1.get(fileName).equals(fileToAddSHA1));
    }

    public Set<String> getStageFileNames() {
        return stageFileToSha1.keySet();
    }

    public String getStagedForAdditionFileSHA1(String fileName) {
        return stageFileToSha1.get(fileName);
    }

    public boolean getStagedForRemovalFileSHA1(String fileName) {
        return stageRemoveFileToSha1.get(fileName);
    }

    public Set<String> getStageForRemovalFileNames() {
        return stageRemoveFileToSha1.keySet();
    }

    public boolean isStageExists(Commit headCommit) {
        return !stageFileToSha1.isEmpty() || !stageRemoveFileToSha1.isEmpty();
    }

    public void saveStage() {
        Utils.writeObject(Repository.STAGE, this);
    }

}

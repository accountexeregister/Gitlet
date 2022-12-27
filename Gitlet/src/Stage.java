import Utilities.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Stage {
    private Map<String, String> stageFileToSha1 = new HashMap<>();
    private Map<String, String> stageRemoveFileToSha1 = new HashMap<>();
    private Map<String, String> tracks = new HashMap<>();

    public void stageFile(Commit headCommit, String fileName) {

        File fileToAdd = Utils.join(Repository.CWD, fileName);
        String fileToAddSHA1 = Utils.sha1(Utils.readContentsAsString(fileToAdd));
        if (headCommit.isStageable(fileName, fileToAddSHA1)) {
            stageFileToSha1.put(fileName, fileToAddSHA1);
            stageRemoveFileToSha1.remove(fileName);
        } else {
            stageFileToSha1.remove(fileName);
        }
    }

    public boolean isStagedForAddition(String fileName) {
        return stageFileToSha1.get(fileName) != null;
    }

    public boolean isStagedForRemoval(String fileName) {
        return stageRemoveFileToSha1.get(fileName) != null;
    }

    public void unstage(String fileName) {
        stageFileToSha1.remove(fileName);
    }

    public void stageForRemoval(String fileName) {
        stageRemoveFileToSha1.put(fileName, null);
    }

    public boolean isStageable(Commit headCommit, String fileName, String fileToAddSHA1) {
        return stageFileToSha1.get(fileName) == null || !(stageFileToSha1.get(fileName).equals(fileToAddSHA1));
    }

    public Set<String> getStageFileNames() {
        return stageFileToSha1.keySet();
    }

    public Set<String> getStageForRemovalFileNames() {
        return stageRemoveFileToSha1.keySet();
    }

}

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {

    public int compare(File o1, File o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

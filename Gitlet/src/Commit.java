import Utilities.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Commit implements Serializable {

    private String message;
    private Date date;
    private String parent;

    public Commit(String message) {
        this.message = message;
        // date = Calendar.getInstance();
    }

    // Creates the initial commit by creating initial commit message, setting date to epoch time and setting its parent to null
    private Commit() {
        this.message = "initial commit";
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        cal.setTimeInMillis(0);
        date = cal.getTime();
        parent = null;
    }

    // Factory method to create initial commit by calling private constructor Commit()
    public static Commit createInitCommit() {
        return new Commit();
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
        return Utils.sha1(this.toString());
    }
}

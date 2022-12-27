import java.util.Calendar;

public class Commit {

    private String message;
    private Calendar date;
    private String parent;
    
    public Commit(String message) {
        this.message = message;
        date = Calendar.getInstance();
    }

    // Creates the initial commit by creating initial commit message, setting date to epoch time and setting its parent to null
    private Commit() {
        this.message = "initial commit";
        date = Calendar.getInstance();
        date.setTimeInMillis(0);
        parent = null;
    }

    // Factory method to create initial commit by calling private constructor Commit()
    public static Commit createInitCommit() {
        return new Commit();
    }
}

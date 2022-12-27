public class Main {
    public static void main(String[] args) {
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.initGitlet();
                break;
            case "add":
                break;
            case "log":
                Repository.log();
                break;
            case "delete":
                Repository.deleteFiles();
                break;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.initGitlet();
                break;
            case "add":
                if (args.length < 2) {
                    return;
                }
                String fileName = args[1];
                Repository.add(fileName);
                break;
            case "commit":
                if (args.length < 2) {
                    return;
                }
                String message = args[1];
                Repository.commit(message);
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

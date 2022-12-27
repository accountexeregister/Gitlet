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
            case "checkout":
                if (args.length == 3) {
                    if (args[1].equals("--")) {
                        String fileNameCheckout = args[2];
                        Repository.checkoutHead(fileNameCheckout);
                    }
                } else if (args.length == 4) {
                    String commitId = args[1];
                    if (args[2].equals("--")) {
                        String fileNameCheckout = args[3];
                        Repository.checkout(commitId, fileNameCheckout);
                    }
                }
        }
    }
}

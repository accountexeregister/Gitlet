public class Main {
    private static void printErrorIfNoGitletInit() {
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.initGitlet();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String fileName = args[1];
                Repository.add(fileName);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String message = args[1];
                Repository.commit(message);
                break;
            case "log":
                printErrorIfNoGitletInit();
                Repository.log();
                break;
            case "delete":
                printErrorIfNoGitletInit();
                Repository.deleteFiles();
                break;
            case "checkout":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length == 2) {
                    printErrorIfNoGitletInit();
                    String branchToCheckout = args[1];
                    Repository.checkoutBranch(branchToCheckout);
                } else if (args.length == 3) {
                    if (args[1].equals("--")) {
                        printErrorIfNoGitletInit();
                        String fileNameCheckout = args[2];
                        Repository.checkoutHead(fileNameCheckout);
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                } else if (args.length == 4) {
                    String commitId = args[1];
                    if (args[2].equals("--")) {
                        printErrorIfNoGitletInit();
                        String fileNameCheckout = args[3];
                        Repository.checkout(commitId, fileNameCheckout);
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "rm":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String fileNameToRm = args[1];
                Repository.rm(fileNameToRm);
                break;
            case "find":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String messageToFind = args[1];
                Repository.find(messageToFind);
                break;
            case "status":
                printErrorIfNoGitletInit();
                Repository.status();
                break;
            case "branch":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String branchName = args[1];
                Repository.branch(branchName);
                break;
            case "rm-branch":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String branchNameDel = args[1];
                Repository.rmBranch(branchNameDel);
                break;
            case "global-log":
                printErrorIfNoGitletInit();
                Repository.globalLog();
                break;
            case "reset":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String commitIdToReset = args[1];
                Repository.reset(commitIdToReset);
                break;
            case "splitpoint":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String branchFileName = args[1];
                Repository.getSplitPointMessage(branchFileName);
                break;
            case "merge":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                printErrorIfNoGitletInit();
                String givenBranchName = args[1];
                Repository.merge(givenBranchName);
                break;
            case "readfile":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String fileToRead = args[1];
                Repository.readFile(fileToRead);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}

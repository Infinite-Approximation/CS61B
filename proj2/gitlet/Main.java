package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Jack
 */
public class Main {
    private static void checkOpearands(String[] args, int numOfOperands) {
        if (args.length - 1 != numOfOperands) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    private static void checkForCheckout(String[] args) {
        int numOfOpearands = args.length - 1;
        boolean isPass = true;
        if (numOfOpearands == 1) {
            isPass = true;
        } else if (numOfOpearands == 2) {
            if (!args[1].equals("--")) {
                isPass = false;
            }
        } else if (numOfOpearands == 3) {
            if (!args[2].equals("--")) {
                isPass = false;
            }
        } else {
            isPass = false;
        }
        if (!isPass) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        String fileName;
        String branchName;
        String commitId;
        String remoteName;
        String remoteDir;
        try {
            switch(firstArg) {
                case "init":
                    checkOpearands(args, 0);
                    Repository.initCommand();
                    break;
                case "add":
                    checkOpearands(args, 1);
                    fileName = args[1];
                    Repository.addCommand(fileName, null);
                    break;
                case "rm":
                    checkOpearands(args, 1);
                    fileName = args[1];
                    Repository.rmCommand(fileName);
                    break;
                case "status":
                    checkOpearands(args, 0);
                    Repository.statusCommand();
                    break;
                case "commit":
                    checkOpearands(args, 1);
                    if (args.length == 1 || args[1].equals("")) {
                        System.out.println("Please enter a commit message.");
                        System.exit(0);
                    }
                    String message = args[1];
                    Repository.commitCommand(message, null, null);
                    break;
                case "log":
                    checkOpearands(args, 0);
                    Repository.logCommand();
                    break;
                case "global-log":
                    checkOpearands(args, 0);
                    Repository.globalLogCommand();
                    break;
                case "find":
                    checkOpearands(args, 1);
                    String searchMessage = args[1];
                    Repository.findCommand(searchMessage);
                case "branch":
                    checkOpearands(args, 1);
                    branchName = args[1];
                    Repository.branchCommand(branchName);
                    break;
                case "checkout":
                    checkForCheckout(args);
                    int length = args.length;
                    if (length == 2) {
                        branchName = args[1];
                        Repository.checkoutCommand(null, branchName);
                    } else if (length == 3) {
                        fileName = args[2];
                        Repository.checkoutCommandArg3(fileName);
                    } else if (length == 4) {
                        commitId = args[1];
                        fileName = args[3];
                        Repository.checkoutCommandArg4(commitId, fileName);
                    }
                    break;
                case "rm-branch":
                    checkOpearands(args, 1);
                    branchName = args[1];
                    Repository.rmBranchCommand(branchName);
                    break;
                case "reset":
                    checkOpearands(args, 1);
                    commitId = args[1];
                    Repository.resetCommand(commitId);
                    break;
                case "merge":
                    checkOpearands(args, 1);
                    branchName = args[1];
                    Repository.mergeCommand(null, branchName);
                    break;
                case "add-remote":
                    checkOpearands(args, 2);
                    remoteName = args[1];
                    remoteDir = args[2];
                    Repository.addRemoteCommand(remoteName, remoteDir);
                    break;
                case "rm-remote":
                    checkOpearands(args, 1);
                    remoteName = args[1];
                    Repository.rmRemoteCommand(remoteName);
                    break;
                case "push":
                    checkOpearands(args, 2);
                    remoteName = args[1];
                    branchName = args[2];
                    Repository.pushCommand(remoteName, branchName);
                    break;
                case "fetch":
                    checkOpearands(args, 2);
                    remoteName = args[1];
                    branchName = args[2];
                    Repository.fetchCommand(remoteName, branchName);
                    break;
                case "pull":
                    checkOpearands(args, 2);
                    remoteName = args[1];
                    branchName = args[2];
                    Repository.pullCommand(remoteName, branchName);
                    break;
                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Jack
 */
public class Main {

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
        try {
            switch(firstArg) {
                case "init":
                    Repository.initCommand();
                    break;
                case "add":
                    fileName = args[1];
                    Repository.addCommand(fileName);
                    break;
                case "rm":
                    fileName = args[1];
                    Repository.rmCommand(fileName);
                    break;
                case "status":
                    Repository.statusCommand();
                    break;
                case "commit":
                    if (args.length == 1) {
                        System.out.println("Please enter a commit message.");
                        System.exit(0);
                    }
                    String message = args[1];
                    Repository.commitCommand(message);
                    break;
                case "log":
                    Repository.logCommand();
                    break;
                case "global-log":
                    Repository.globalLogCommand();
                    break;
                case "find":
                    String searchMessage = args[1];
                    Repository.findCommand(searchMessage);
                case "branch":
                    branchName = args[1];
                    Repository.branchCommand(branchName);
                    break;
                case "checkout":
                    int length = args.length;
                    if (length == 2) {
                        branchName = args[1];
                        Repository.checkoutCommand(branchName);
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
                    branchName = args[1];
                    Repository.rmBranchCommand(branchName);
                    break;
                case "reset":
                    commitId = args[1];
                    Repository.resetCommand(commitId);
                    break;
                case "merge":
                    branchName = args[1];
                    Repository.mergeCommand(branchName);
                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

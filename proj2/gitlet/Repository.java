package gitlet;

import edu.princeton.cs.algs4.ST;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;
/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Jack
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCHES_DIR = join(GITLET_DIR, "refs", "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File STAGE_DIR = join(GITLET_DIR, "index"); // 用rm_代表removal
    public static void initCommand() throws IOException {
        // 如果存在.gitlet那么就退出
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        // 初始化
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        BRANCHES_DIR.mkdirs();
        STAGE_DIR.mkdir();
        // 创建初始提交
        Commit initCommit = new Commit();
        /** 需要在initCommit.getId()这个文件下写入initCommit对象 */
        writeObjectWithPrefix(COMMITS_DIR, initCommit.getId(), initCommit);
        // 创建一个master分支，让它指向initCommit
        File masterBranch = join(BRANCHES_DIR, "master");
        masterBranch.createNewFile();
        writeContents(masterBranch, initCommit.getId());
        // 初始化HEAD分支
        HEAD_FILE.createNewFile();
        writeContents(HEAD_FILE, join(BRANCHES_DIR, "master").getPath());
    }

    private static void checkIfInit() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void logCommand() {
        checkIfInit();
        String headCommitId = getHeadCommitId();
        Commit curCommit = getCommitById(headCommitId);
        while (curCommit != null) {
            printCommit(curCommit);
            curCommit = getCommitById(curCommit.getParent1Id());
        }
    }

    /** 读取HEAD指向的commit的ID，如果指向的是一个分支，那么返回分支指向的commit的ID */
    private static String getHeadCommitId() {
        String branchNameOrID = readContentsAsString(HEAD_FILE);
        if (branchNameOrID.startsWith(BRANCHES_DIR.getPath())) {
            return readBranchCommit(branchNameOrID);
        } else {
            return branchNameOrID;
        }
    }

    private static Commit getHeadCommit() {
        return getCommitById(getHeadCommitId());
    }

    /** 返回分支指向的Commit的ID */
    private static String readBranchCommit(String branchName) {
        File branchFile = new File(branchName);
        String commitID = readContentsAsString(branchFile);
        return commitID;
    }

    /** add指令：添加到暂存区 */
    public static void addCommand(String fileName, String branchName) throws IOException {
        checkIfInit();
        File fileToBeAdded = null;
        if (branchName == null) { // 这说明需要将当前目录下的文件添加到staging area
            fileToBeAdded = new File(fileName);
            if (!fileToBeAdded.exists()) { // 若文件不存在，则报错
                System.out.println("File does not exist.");
                System.exit(0);
            }
        } else { // 这说明需要将指定分支的文件写入到staging area
            Commit branchCommit = getBranchCommitByName(branchName);
            fileToBeAdded = branchCommit.getFileByName(fileName);
        }
        // 如果添加了a.txt，而暂存区又rm_a.txt，那么需要删除rm_a.txt
        File removedFile = join(STAGE_DIR, "rm_" + fileName);
        if (removedFile.exists()) {
            removedFile.delete();
        }
        String fileToBeAddedSHA1 = getFileSHA1(fileToBeAdded); // 计算添加文件的SHA-1值。
        // 需要取出当前commit的该文件的SHA-1值
        Commit curCommit = getHeadCommit();
        String curCommitFileSHA1 = curCommit.getFileSHA1ByFileName(fileName);
        if (fileToBeAddedSHA1.equals(curCommitFileSHA1)) {
            File oldFile = join(STAGE_DIR, fileName);
            oldFile.delete();
        } else {
            File newFile = join(STAGE_DIR, fileName);
            newFile.createNewFile();
            writeContents(newFile, readContents(fileToBeAdded));
        }
    }

    public static void commitCommand(String message, String parent1Id, String parent2Id) throws IOException {
        checkIfInit();
        // 首先判断一下暂存区有没有文件
        File[] files = STAGE_DIR.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        /** 设置新commit的信息 */
        Commit curCommit = getHeadCommit(); // curCommit
        String headCommitId = curCommit.getId(); // curCommit的ID
        Commit newCommit = new Commit(); // 新的Commit
        newCommit.setMessage(message);
        newCommit.setTimestamp(new Date());
        if (parent1Id == null) {
            newCommit.setParent1Id(headCommitId);
            newCommit.setBlobs(new TreeMap<>(curCommit.getBlobs()));
        } else {
            newCommit.setParent1Id(parent1Id);
            newCommit.setParent2Id(parent2Id);
            Commit commit = getCommitById(parent1Id);
            newCommit.setBlobs(new TreeMap<>(commit.getBlobs()));
        }
        /** 建立新commit的Map */
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.startsWith("rm_")) { // 处理暂存删除的文件
                fileName = fileName.substring(3);
                newCommit.removeFile(fileName);
            } else {
                String sha1 = getFileSHA1(file);
                newCommit.addFile(fileName, sha1);
                writeObjectWithPrefix(BLOBS_DIR, sha1, readContents(file)); // 把暂存区里的文件保存到objects目录下
            }
        }
        // 将新commit写入到磁盘内。
        String newCommitSHA1 = newCommit.getId();
        writeObjectWithPrefix(COMMITS_DIR, newCommitSHA1, newCommit);
        updateHead(newCommitSHA1);
        // 清除staging area
        clearStagingArea();
    }

    // 更新HEAD指针和当前分支指针。(1)当HEAD指向一个分支，那么移动这个分支就行。(2)当HEAD指向一个commit，那么需要修改HEAD指向新的commit
    private static void updateHead(String sha1) {
        String branchNameOrID = readContentsAsString(HEAD_FILE);
        if (branchNameOrID.startsWith(BRANCHES_DIR.getPath())) { // 这说明HEAD指向了一个分支
            File file = new File(branchNameOrID);
            writeContents(file, sha1);
        } else { // HEAD指向一个commit
            writeContents(HEAD_FILE, sha1);
        }
    }

    private static void clearStagingArea() {
        for (File file : STAGE_DIR.listFiles()) {
            file.delete();
        }
    }

    public static void globalLogCommand() {
        for (File dir : COMMITS_DIR.listFiles()) {
            for (File file : dir.listFiles()) {
                Commit commit = readObject(file, Commit.class);
                printCommit(commit);
            }
        }
    }

    public static void printCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getId());
        /** 处理存在两个parent的情况 */
        if (commit.getParent2Id() != null) {
            System.out.printf("Merge: %s %s%n",
                    commit.getParent1Id().substring(0, 7),
                    commit.getParent2Id().substring(0, 7));
        }
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void statusCommand() {
        checkIfInit();
        System.out.println("=== Branches ===");
        String curBranch = getCurBranch(); // HEAD指向的可能不是一个分支
        List<String> braches = getBranches();
        for (String branch : braches) {
            if (branch.equals(curBranch)) {
                System.out.printf("*");
            }
            System.out.println(branch);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = getStagedFiles();
        for (String file : stagedFiles) {
            if (!file.startsWith("rm_")) {
                System.out.println(file);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removedFileNames = getRemovedFileNames();
        for (String fileName : removedFileNames) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> modificationsFileNames = getModificationsFileNames();
        for (String fileName : modificationsFileNames) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        List<String> untrackedFileNames = getUntrackedFileNames();
        for (String fileName : untrackedFileNames) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    private static List<String> getModificationsFileNames() {
        List<String> modificationsFileNames = new ArrayList<>();
        List<String> stagedFiles = getStagedFiles();
        Set<String> trackedFileNames = getTrackedFileNames();
        Commit curCommit = getHeadCommit();
        for (File file: CWD.listFiles()) {
            String fileName = file.getName();
            // 1. Tracked in the current commit, changed in the working directory, but not staged;
            String CWDFileSHA1 = getFileSHA1(file);
            String curCommitFileSHA1 = curCommit.getFileSHA1ByFileName(fileName);
            if (trackedFileNames.contains(fileName) && !CWDFileSHA1.equals(curCommitFileSHA1)
                    && !stagedFiles.contains(fileName)) {
                modificationsFileNames.add(fileName + " (modified)");
            }
            // 2. Staged for addition, but with different contents than in the working directory;
            String stagedFileSHA1 = getFileSHA1ByNameInStagingArea(fileName);
            if (stagedFiles.contains(fileName) && !stagedFileSHA1.equals(CWDFileSHA1)) {
                modificationsFileNames.add(fileName + " (modified)");
            }
        }
        // 3. Staged for addition, but deleted in the working directory;
        for (File file: STAGE_DIR.listFiles()) {
            if (!file.getName().startsWith("rm_")) {
                String fileName = file.getName();
                File stagedFileName = new File(fileName);
                if (!stagedFileName.exists()) {
                    modificationsFileNames.add(fileName + " (deleted)");
                }
            }
        }
        // 4. Not staged for removal, but tracked in the current commit and deleted from the working directory.
        List<String> stagedForRemovalFileNames = getStagedForRemovalFileNames();
        for (String fileName: trackedFileNames) {
            File curFile = new File(fileName);
            if (!curFile.exists() && !stagedForRemovalFileNames.contains(fileName)) {
                modificationsFileNames.add(fileName + " (deleted)");
            }
        }
        return modificationsFileNames;
    }

    private static String getFileSHA1(File file) {
        if (file == null) {
            return null;
        }
        if (file.isFile()) {
            return sha1(readContents(file));
        }
        return null;
    }

    private static List<String> getStagedForRemovalFileNames() {
        List<String> stagedForRemovalFileNames = new ArrayList<>();
        for (File file: STAGE_DIR.listFiles()) {
            String fileName = file.getName();
            if (fileName.startsWith("rm_")) {
                stagedForRemovalFileNames.add(fileName.substring(3));
            }
        }
        return stagedForRemovalFileNames;
    }
    private static String getFileSHA1ByNameInStagingArea(String fileName) {
        for (File file : STAGE_DIR.listFiles()) {
            if (file.getName().equals(fileName)) {
                return sha1(readContents(file));
            }
        }
        return null;
    }
    private static List<String> getUntrackedFileNames() {
        List<String> untrackedFileNames = new ArrayList<>();
        List<String> stagedFiles = getStagedFiles();
        Set<String> trackedFileNames = getTrackedFileNames();
        for (File file: CWD.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && !stagedFiles.contains(fileName) && !trackedFileNames.contains(fileName)) {
                untrackedFileNames.add(fileName);
            }
        }
        Collections.sort(untrackedFileNames);
        return untrackedFileNames;
    }
    private static List<String> getRemovedFileNames() {
        List<String> removedFileNames = new ArrayList<>();
        for (File file : STAGE_DIR.listFiles()) {
            if (file.getName().startsWith("rm_")) {
                removedFileNames.add(file.getName().substring(3));
            }
        }
        Collections.sort(removedFileNames);
        return removedFileNames;
    }

    private static List<String> getStagedFiles() {
        List<String> stagedFiles = new ArrayList<>();
        for (File file : STAGE_DIR.listFiles()) {
            stagedFiles.add(file.getName());
        }
        Collections.sort(stagedFiles);
        return stagedFiles;
    }

    private static List<String> getBranches() {
        List<String> branches = new ArrayList<>();
        for (File file : BRANCHES_DIR.listFiles()) {
            branches.add(file.getName());
        }
        Collections.sort(branches);
        return branches;
    }

    private static String getCurBranch() {
        String branchOrCommit = readContentsAsString(HEAD_FILE);
        if (branchOrCommit.startsWith(BRANCHES_DIR.getPath())) {
            return new File(branchOrCommit).getName();
        } else {
            return null; // 返回null说明HEAD指向的是一个commit，而不是分支。
        }
    }

    public static void findCommand(String message) {
        checkIfInit();
        boolean found = false;
        for (File dir : COMMITS_DIR.listFiles()) {
            for (File file : dir.listFiles()) {
                Commit commit = readObject(file, Commit.class);
                if (commit.getMessage().equals(message)) {
                    System.out.println(commit.getId());
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void branchCommand(String branchName) throws IOException {
        checkIfInit();
        File branchPath = join(BRANCHES_DIR, branchName);
        if (branchPath.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File branch = join(BRANCHES_DIR, branchName);
        branch.createNewFile();
        Commit commit = getHeadCommit();
        writeContents(branch, commit.getId());
    }

    public static void checkoutCommand(String branchName) throws IOException {
        checkIfInit();
        // 处理一下特殊情况
        // 1. 检查分支是否存在
        File branchPath = join(BRANCHES_DIR, branchName);
        if (!branchPath.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        // 2. 检查当前分支
        String headBranchName = getCurCommitName();
        if (headBranchName.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        // 3. 检查当前目录，处理不符合条件的文件。
        checkOverwriteAndDelete(branchName);
        // 下面是正常情况
        // 1. 将指定分支的文件放在当前目录下
        String sha1 = readContentsAsString(branchPath);
        copyCommitFileToCWD(sha1);
        // 2. 将头指针指向指定分支
        setupHEAD(branchName);
        // 3. 清除staging area
        clearStagingArea();
    }

    private static void copyCommitFileToCWD(String sha1) throws IOException {
        Commit branchCommit = getCommitById(sha1);
        Map<String, String> blobs = branchCommit.getBlobs();
        for (Map.Entry<String, String> entry : blobs.entrySet()) {
            String blobName = entry.getKey();
            String blobSha1 = entry.getValue();
            File branchFile = getFileBySHA1(blobSha1); // 指定分支的文件
            File file = join(CWD, blobName); // 当前目录的同名文件
            if (!file.exists()) { // 如果当前目录下的同名文件不不存在，则创建一个
                file.createNewFile();
            }
            writeContents(file, readContents(branchFile)); // 往当前目录下的同名文件写入内容
        }
    }

    /**
     * 如果一个文件没有被当前commit追踪，但是会被指定的commit覆盖，那么就不行！
     * 如果一个文件被当前commit追踪，但是没有在指定的commit中追踪，就需要删除！
     * */
    private static void checkOverwriteAndDelete(String branchNameOrId) {
        Set<String> trackedFileNames = getTrackedFileNames();
        Set<String> commitFileNames = getTrackedFileNameByBranchNameOrID(branchNameOrId);
        for (File file: CWD.listFiles()) {
            // 如果一个文件没有被当前commit追踪，但是会被指定的commit覆盖，那么就不行！
            if (!trackedFileNames.contains(file.getName()) && commitFileNames.contains(file.getName())) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
            // 如果一个文件被当前commit追踪，但是没有在指定的commit中追踪，就需要删除！
            if (trackedFileNames.contains(file.getName()) && !commitFileNames.contains(file.getName())) {
                file.delete();
            }
        }
    }

    public static File getFileBySHA1(String sha1) {
        for (File dir: BLOBS_DIR.listFiles()) {
            String prefix = dir.getName();
            for (File file : dir.listFiles()) {
                String suffix = file.getName();
                String curSha1 = prefix + suffix;
                if (curSha1.equals(sha1)) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param branchNameOrId 可以是一个branch的名字，也可以是一个CommitId
     * @return 返回commit包含的文件名
     */
    private static Set<String> getTrackedFileNameByBranchNameOrID(String branchNameOrId) {
        File branch = join(BRANCHES_DIR, branchNameOrId); // branch这个file里面写的时commit的sha1值。
        Commit commit;
        if (branch.exists()) { // 存在说明branchNameOrId是分支的名字
            String sha1 = readContentsAsString(branch);
            commit = getCommitById(sha1);
        } else {
            commit = getCommitById(branchNameOrId);
        }
        Set<String> fileNames = commit.getFileNames();
        return fileNames;
    }

    private static void setupHEAD(String branchName) {
        String path = join(BRANCHES_DIR, branchName).getPath();
        writeContents(HEAD_FILE, path);
    }

    private static String getCurCommitName() {
        String headBranchPath = readContentsAsString(HEAD_FILE);
        String headBranchName = new File(headBranchPath).getName();
        return headBranchName;
    }

    public static void rmCommand(String fileName) throws IOException {
        checkIfInit();
        Commit commit = getHeadCommit();
        Set<String> trackedFileNames = getTrackedFileNames();
        Set<String> stagedFileNames = getStagedFileNames();
        if (trackedFileNames.contains(fileName) || stagedFileNames.contains(fileName)) {
            // 如果被暂存了，那么就从暂存区中删除
            File file = join(STAGE_DIR, fileName);
            if (file.exists()) {
                file.delete();
            }
            // 如果被目前的commit追踪了，那么首先暂存删除，然后从工作目录删除这个文件如果用户没有删除。
            if (trackedFileNames.contains(fileName)) {
                // 暂存删除
                File deleteFile = join(STAGE_DIR, "rm_" + fileName);
                deleteFile.createNewFile();
                // 删除文件
                File CWDFile = new File(fileName);
                if (CWDFile.exists()) {
                    CWDFile.delete();
                }

            }
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static Set<String> getTrackedFileNames() {
        Commit headCommit = getHeadCommit();
        Map<String, String> blobs = headCommit.getBlobs();
        Set<String> fileNames = blobs.keySet();
        return fileNames;
    }

    /** 这个返回的包含 a.txt 和 rm_a.txt */
    private static Set<String> getStagedFileNames() {
        Set<String> stagedFileNames = new HashSet<>();
        for (File file : STAGE_DIR.listFiles()) {
            stagedFileNames.add(file.getName());
        }
        return stagedFileNames;
    }

    public static void checkoutCommandArg3(String fileName) throws IOException {
        checkIfInit();
        Commit commit = getHeadCommit();
        checkFileInCommit(commit, fileName);
        File fileInCommit = commit.getFileByName(fileName);
        File fileInCWD = new File(fileName);
        if (!fileInCWD.exists()) {
            fileInCWD.createNewFile();
        }
        writeContents(fileInCWD, readContents(fileInCommit));
    }

    /**
     * 首先检查是否存在这个commit，再检查这个commit是否存在这个file，之后就在这个commit中取出同名的file，覆盖当前目录下的file
     */
    public static void checkoutCommandArg4(String commitId, String fileName) throws IOException {
        checkIfInit();
        String fullCommitId = checkCommitExistsByIdAndReturnFullId(commitId);
        Commit commit = getCommitById(fullCommitId);
        checkFileInCommit(commit, fileName);
        File fileInCommit = commit.getFileByName(fileName);
        File fileInCWD = new File(fileName);
        if (!fileInCWD.exists()) {
            fileInCWD.createNewFile();
        }
        writeContents(fileInCWD, readContents(fileInCommit));
    }

    /** 给定一个commit的sha1值，检查该commit是否存在，若存在则返回该commit完整的sha1值 */
    private static String checkCommitExistsByIdAndReturnFullId(String commitId) {
        String fullCommitId = "";
        List<String> commitIds = getCommitIds();
        boolean checkCommitExists = false;
        for (String id: commitIds) {
            if (id.contains(commitId)) {
                fullCommitId = id;
                checkCommitExists = true;
                break;
            }
        }
        if (!checkCommitExists) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        return fullCommitId;
    }

    private static void checkFileInCommit(Commit commit, String fileName) {
        Set<String> fileNames = commit.getFileNames();
        if (!fileNames.contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    private static List<String> getCommitIds() {
        List<String> commitIds = new ArrayList<>();
        for (File dir: COMMITS_DIR.listFiles()) {
            String prefix = dir.getName();
            for (File file : dir.listFiles()) {
                String suffix = file.getName();
                String curCommitId = prefix + suffix;
                commitIds.add(curCommitId);
            }
        }
        return commitIds;
    }

    /** 根据Id寻找对应的Commit */
    private static Commit getCommitById(String commitId) {
        for (File dir: COMMITS_DIR.listFiles()) {
            String prefix = dir.getName();
            for (File file : dir.listFiles()) {
                String suffix = file.getName();
                String curCommitId = prefix + suffix;
                if (curCommitId.equals(commitId)) {
                    Commit commit = readObject(file, Commit.class);
                    return commit;
                }
            }
        }
        return null;
    }

    public static void rmBranchCommand(String branchName) {
        checkIfInit();
        File branch = join(BRANCHES_DIR, branchName);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String curBranch = getCurBranch();
        if (branchName.equals(curBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branch.delete();
    }

    public static void resetCommand(String commitId) throws IOException {
        checkIfInit();
        // 1. If no commit with the given id exists, print No commit with that id exists.
        String fullCommitId = checkCommitExistsByIdAndReturnFullId(commitId);
        // 2. Check whether working file is untracked in the current branch and would be overwritten by the reset,
        // 3. Removes tracked files that are not present in that commit
        checkOverwriteAndDelete(fullCommitId);
        // 4. 将目标commit的内容复制到当前目录
        copyCommitFileToCWD(fullCommitId);
        // 5. 移动头部
        updateHead(fullCommitId);
        // 6. 清除staging area
        clearStagingArea();
    }

    /**
     * 一个未跟踪文件如果在givenCommit修改了，在curCommit删除了，说明冲突了，这个文件会被覆盖
     */
    private static void preCheckRule3(String untrackedFileName, Commit givenCommit, Commit latestCommonAncestor) {
        boolean flag1 = latestCommonAncestor.hasFile(untrackedFileName);
        boolean flag2 = givenCommit.hasFile(untrackedFileName);
        boolean flag3 = flag1 && flag2 && latestCommonAncestor.getFileSHA1ByFileName(untrackedFileName)
                        .equals(givenCommit.getFileSHA1ByFileName(untrackedFileName));
        if (flag3) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    /**
     * 一个未跟踪文件如果在givenCommit，但是不在split point commit的时候，那么就会被覆盖，对应rule5
     */
    private static void preCheckRule5(String untrackedFileName, Commit givenCommit, Commit latestCommonAncestor) {
        if (givenCommit.hasFile(untrackedFileName) && !latestCommonAncestor.hasFile(untrackedFileName)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    public static void mergeCommand(String branchName) throws IOException {
        checkIfInit();
        // Failure cases
        // 1.  If there are staged additions or removals present, print the error message You have uncommitted changes.
        checkStagingAreaIsEmpty();
        // 2. If a branch with the given name does not exist, print the error message A branch with that name
        // does not exist.
        checkBranchExist(branchName);
        // 3. If attempting to merge a branch with itself, print the error message Cannot merge a branch with itself.
        Commit curCommit = getHeadCommit();
        Commit givenCommit = getBranchCommitByName(branchName);
        if (curCommit.getId().equals(givenCommit.getId())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        // 4. If an untracked file in the current commit would be overwritten or deleted by the merge,
        // print There is an untracked file in the way; delete it, or add and commit it first.
        Commit latestCommonAncestor = getLatestCommonAncestor(curCommit, givenCommit);
        List<String> untrackedFileNames = getUntrackedFileNames();
        for (String untrackedFileName : untrackedFileNames) {
            preCheckRule3(untrackedFileName, givenCommit, latestCommonAncestor);
            preCheckRule5(untrackedFileName, givenCommit, latestCommonAncestor);
        }
        // 首先处理两个特殊情况
        // 1. If the split point is the same commit as the given branch, then we do nothing;
        // the merge is complete, and the operation ends with the message Given branch is an ancestor
        // of the current branch.
        if (latestCommonAncestor.getId().equals(givenCommit.getId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        // 2. If the split point is the current branch, then the effect is to check out the given branch,
        // and the operation ends after printing the message Current branch fast-forwarded.
        if (latestCommonAncestor.getId().equals(curCommit.getId())) {
            checkoutCommand(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        // 然后写八条规则
        Set<String> unionFileNames = getUnionFileNames(curCommit, givenCommit, latestCommonAncestor);
        boolean hasConflict = false, conflictFlag = false;
        for (String fileName : unionFileNames) {
            File curFile = curCommit.getFileByName(fileName);
            String curFileSha1 = getFileSHA1(curFile);
            File givenFile = givenCommit.getFileByName(fileName);
            String givenFileSha1 = getFileSHA1(givenFile);
            File latestCommonAncestorFile = latestCommonAncestor.getFileByName(fileName);
            String latestCommonAncestorFileSha1 = getFileSHA1(latestCommonAncestorFile);
            // 1. modified in givenCommit but not curCommit -> givenCommit
            rule1(fileName, curFileSha1, givenFileSha1, latestCommonAncestorFileSha1, branchName,
                            curFile, givenFile);
            // 2. modified in curCommit but not givenCommit -> curCommit
            rule2(fileName, curFileSha1, givenFileSha1, latestCommonAncestorFileSha1, branchName,
                    curFile, givenFile);
            // 3. modified in givenCommit and in curCommit
            hasConflict = rule3(fileName, curFileSha1, givenFileSha1, latestCommonAncestorFileSha1, branchName,
                    curFile, givenFile);
            if (hasConflict) {
                conflictFlag = true;
            }
            // 4. Not in split commit nor givenCommit but in curCommit
            rule4(fileName, curFileSha1, givenFileSha1, latestCommonAncestorFileSha1, branchName,
                    curFile, givenFile);
            // 5， Not in split commit nor curCommit but in givenCommit
            rule5(fileName, curFileSha1, givenFileSha1, latestCommonAncestorFileSha1, branchName,
                    curFile, givenFile);
            // 6. unmodified in curCommit but not present in givenCommit
            rule6(fileName, curFileSha1, givenFileSha1, latestCommonAncestorFileSha1, branchName,
                    curFile, givenFile);
            // 7. unmodified in givenCommit but not present in curCommit
            rule7(fileName, curFileSha1, givenFileSha1, latestCommonAncestorFileSha1, branchName,
                    curFile, givenFile);
        }
        if (conflictFlag) {
            System.out.println("Encountered a merge conflict.");
        }
        String curBranchName = getCurBranch();
        String message = "Merged " + branchName + " into " + curBranchName + ".";
        commitCommand(message, curCommit.getId(), givenCommit.getId());
    }

    // 1. modified in givenCommit but not curCommit -> givenCommit
    private static void rule1(String fileName, String curFileSha1, String givenFileSha1,
                              String latestCommonAncestorFileSha1, String branchName,
                            File curFile, File givenFile) throws IOException {
        if (curFileSha1 != null && latestCommonAncestorFileSha1 != null
                && curFileSha1.equals(latestCommonAncestorFileSha1) && givenFileSha1 != null
                && !givenFileSha1.equals(latestCommonAncestorFileSha1)) {
            File file = new File(fileName);
            // 先修改工作区
            if (!file.exists()) {
                file.createNewFile();
            }
            writeContents(file, readContents(givenFile));
            // 再修改暂存区
            addCommand(fileName, branchName);
        }
    }

    // 2. modified in curCommit but not givenCommit -> curCommit
    private static void rule2(String fileName, String curFileSha1, String givenFileSha1,
                              String latestCommonAncestorFileSha1, String branchName,
                              File curFile, File givenFile) throws IOException {
        if (curFileSha1 != null && latestCommonAncestorFileSha1 != null && givenFileSha1 != null
            && !curFileSha1.equals(latestCommonAncestorFileSha1)
            && givenFileSha1.equals(latestCommonAncestorFileSha1)) {
            // 不需要做任何事情
        }
    }

    // 3. modified in givenCommit and in curCommit
    private static boolean rule3(String fileName, String curFileSha1, String givenFileSha1,
                              String latestCommonAncestorFileSha1, String branchName,
                              File curFile, File givenFile) throws IOException {
        // 如果以相同的方式修改，如同时被删除或者同时被修改，那么就不做任何事情
        boolean hasConflict = false;
        // 但如果一方被删除，另一方被修改，那么就是冲突！！！
        if (curFileSha1 == null && givenFileSha1 != null && latestCommonAncestorFileSha1 != null
            && !latestCommonAncestorFileSha1.equals(givenFileSha1)) {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            writeConflict(file, "", readContentsAsString(givenFile));
            hasConflict = true;
        }
        if (curFileSha1 != null && givenFileSha1 == null && latestCommonAncestorFileSha1 != null
                && !latestCommonAncestorFileSha1.equals(curFileSha1)) {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            writeConflict(file, readContentsAsString(curFile), "");
            hasConflict = true;
        }
        // 如果两者以不同的方式修改
        if (curFileSha1 != null && givenFileSha1 != null && latestCommonAncestorFileSha1 != null
                && !latestCommonAncestorFileSha1.equals(curFileSha1)
                && !latestCommonAncestorFileSha1.equals(givenFileSha1)
                && !givenFileSha1.equals(curFileSha1)) {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            writeConflict(file, readContentsAsString(curFile), readContentsAsString(givenFile));
            hasConflict = true;
        }
        return hasConflict;
    }

    // 4. Not in split commit nor givenCommit but in curCommit
    private static void rule4(String fileName, String curFileSha1, String givenFileSha1,
                              String latestCommonAncestorFileSha1, String branchName,
                              File curFile, File givenFile) throws IOException {
        if (latestCommonAncestorFileSha1 == null && curFileSha1 != null && givenFileSha1 == null) {
            // 什么也不做
        }
    }

    // 5. Not in split commit nor curCommit but in givenCommit
    private static void rule5(String fileName, String curFileSha1, String givenFileSha1,
                              String latestCommonAncestorFileSha1, String branchName,
                              File curFile, File givenFile) throws IOException {
        if (latestCommonAncestorFileSha1 == null && curFileSha1 == null && givenFileSha1 != null) {
            File file = new File(fileName);
            // 先修改工作区
            if (!file.exists()) {
                file.createNewFile();
            }
            writeContents(file, readContents(givenFile));
            // 再修改暂存区
            addCommand(fileName, branchName);
        }
    }

    // 6. unmodified in curCommit but not present in givenBranch
    private static void rule6(String fileName, String curFileSha1, String givenFileSha1,
                              String latestCommonAncestorFileSha1, String branchName,
                              File curFile, File givenFile) throws IOException {
        if (latestCommonAncestorFileSha1 != null && curFileSha1 != null
                && latestCommonAncestorFileSha1.equals(curFileSha1)
                && givenFileSha1 == null) {
            rmCommand(fileName);
        }
    }

    // 7. unmodified in givenCommit but not present in curCommit
    private static void rule7(String fileName, String curFileSha1, String givenFileSha1,
                              String latestCommonAncestorFileSha1, String branchName,
                              File curFile, File givenFile) throws IOException {
        if (latestCommonAncestorFileSha1 != null && givenFileSha1 != null
                && latestCommonAncestorFileSha1.equals(givenFileSha1)
                && curFile == null) {
            // 什么也不做
        }
    }
    private static void writeConflict(File file, String curContent, String givenContent) throws IOException {
        String lineSeparator = System.lineSeparator();
        String mergeContent = "<<<<<<< HEAD" + lineSeparator
                + curContent + "=======" + lineSeparator
                + givenContent + ">>>>>>>";
        writeContents(file, mergeContent);
        // 再修改暂存区
        addCommand(file.getName(), null);
    }
    private static Set<String> getUnionFileNames(Commit a, Commit b, Commit c) {
        Set<String> fileNames1 = a.getFileNames();
        Set<String> fileNames2 = b.getFileNames();
        Set<String> fileNames3 = c.getFileNames();
        Set<String> fileNames = new HashSet<>(a.getFileNames());  // 创建一个新的集合
        fileNames.addAll(fileNames2);
        fileNames.addAll(fileNames3);
        return fileNames;
    }

    private static void checkStagingAreaIsEmpty() {
        if (getStagedFileNames().size() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    private static void checkBranchExist(String branchName) {
        File file = join(BRANCHES_DIR, branchName);
        if (!file.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    private static Commit getBranchCommitByName(String name) {
        File file = join(BRANCHES_DIR, name);
        String commitSha1 = readContentsAsString(file);
        Commit commit = getCommitById(commitSha1);
        return commit;
    }

    private static Commit getLatestCommonAncestor(Commit commitA, Commit commitB) {
        List<String> ancestorsOfCommitA = getAllAncestors(commitA);
        Queue<Commit> ancestorsQueue = new LinkedList<>();
        ancestorsQueue.add(commitB);
        while (!ancestorsQueue.isEmpty()) {
            commitB = ancestorsQueue.poll();
            if (ancestorsOfCommitA.contains(commitB.getId())) {
                return commitB;
            }
            addParentsIdToQueue(commitB, ancestorsQueue);
        }
        return null;
    }

    private static List<String> getAllAncestors(Commit commit) {
        List<String> ancestors = new LinkedList<>();
        Queue<Commit> ancestorsQueue = new LinkedList<>();
        ancestorsQueue.add(commit);
        ancestors.add(commit.getId());
        while (!ancestorsQueue.isEmpty()) {
            commit = ancestorsQueue.poll();
            addParentsCommitAndIdToQueue(commit, ancestors, ancestorsQueue);
        }
        return ancestors;
    }

    private static void addParentsCommitAndIdToQueue(Commit commit, List<String> ancestors, Queue<Commit> ancestorsQueue) {
        if (commit.getParent1Id() != null) {
            ancestorsQueue.add(getCommitById(commit.getParent1Id()));
            ancestors.add(commit.getParent1Id());
        }
        if (commit.getParent2Id() != null) {
            ancestorsQueue.add(getCommitById(commit.getParent2Id()));
            ancestors.add(commit.getParent2Id());
        }
    }

    private static void addParentsIdToQueue(Commit commit, Queue<Commit> ancestorsQueue) {
        if (commit.getParent1Id() != null) {
            ancestorsQueue.add(getCommitById(commit.getParent1Id()));
        }
        if (commit.getParent2Id() != null) {
            ancestorsQueue.add(getCommitById(commit.getParent2Id()));
        }
    }
}

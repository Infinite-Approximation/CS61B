# Gitlet Design Document

**Name**:

## Classes and Data Structures

### Class 1

#### Fields

1. Field 1
2. Field 2


### Class 2

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence

# 概念解释

## 未跟踪文件

在工作目录下，但是没有被添加到暂存区或者没有被跟踪的文件。比如一个文件需要删除被暂存了，但是又重新创建了，那么这个文件是未被跟踪的。

example： **执行 `git rm example.txt`**:

- Git 检查 `example.txt` 是否在当前 commit 的追踪列表中。
- 如果在追踪列表中：
  - 标记为删除并从工作目录中删除。
- 如果不在追踪列表中：
  - 输出“No reason to remove the file”。

```java
private static List<String> getUntrackedFiles() {
    List<String> untrackedFiles = new ArrayList<>();
    Set<String> trackedFiles = new HashSet<>(getTrackedFiles()); // 从当前提交中获取已跟踪文件
    Set<String> stagedFiles = new HashSet<>(getStagedFiles()); // 从暂存区获取文件

    File workingDir = new File(WORKING_DIR);
    for (File file : workingDir.listFiles()) {
        String fileName = file.getName();
        // 如果文件不是目录且不在已跟踪或暂存的文件中，则它是未跟踪文件
        if (!file.isDirectory() && !trackedFiles.contains(fileName) && !stagedFiles.contains(fileName)) {
            untrackedFiles.add(fileName);
        }
    }
    return untrackedFiles;
}
```



# 指令设计

## init

## add

注意点

1. 当rm移除了文件a.txt，添加到了暂存删除，当我再次创建a.txt并进行stage的时候，需要把rm_a.txt删除，然后添加a.txt。

```java
public static void addCommand(String fileName) {
    File fileToAdd = new File(fileName);
    
    // 检查文件是否存在
    if (!fileToAdd.exists()) {
        System.out.println("File does not exist.");
        System.exit(-1);
    }

    // 读取文件内容
    byte[] fileContents = readContents(fileToAdd);

    // 计算 SHA-1 哈希
    String fileSha1 = sha1(fileContents);

    // 创建暂存区的 index 文件路径
    File indexFile = join(GITLET_DIR, "index", fileSha1);
    
    // 检查当前提交中是否有相同内容的文件
    Commit currentCommit = getCurrentCommit(); // 你需要实现这个方法
    String currentFileSha1 = sha1(readContents(currentCommit.getFile(fileName))); // 获取当前提交中文件的 SHA-1

    if (currentFileSha1.equals(fileSha1)) {
        // 如果内容相同，从暂存区移除
        indexFile.delete();
    } else {
        // 否则，将文件内容写入暂存区
        writeContents(indexFile, fileContents);
    }
}

// 辅助方法：读取文件内容
private static byte[] readContents(File file) {
    try {
        return Files.readAllBytes(file.toPath());
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

// 辅助方法：写入文件内容
private static void writeContents(File file, byte[] contents) {
    try {
        Files.write(file.toPath(), contents);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

## rm

```java
public static void rmCommand(String fileName) {
    // 检查文件名是否为空
    if (fileName == null || fileName.trim().isEmpty()) {
        System.out.println("No reason to remove the file.");
        return;
    }

    // 1. 检查文件是否已暂存
    File stagedFile = join(STAGE_DIR, fileName);
    if (stagedFile.exists()) {
        stagedFile.delete(); // 取消暂存
        System.out.println("Removed from staging area: " + fileName);
        return;
    }

    // 2. 检查文件是否被跟踪
    Commit currentCommit = readHeadCommit(); // 获取当前提交
    if (currentCommit.getFileSHA1ByFileName(fileName) != null) {
        // 从工作目录中删除文件
        File workingFile = join(WORKING_DIR, fileName);
        if (workingFile.exists()) {
            workingFile.delete();
        }

        // 标记文件为删除
        markFileForRemoval(fileName);
        System.out.println("Removed: " + fileName);
        return;
    }

    // 3. 文件既不被暂存也不被跟踪
    System.out.println("No reason to remove the file.");
}

// 标记文件为删除（可以在暂存区或提交中处理）
private static void markFileForRemoval(String fileName) {
    File removalFile = join(STAGE_DIR, fileName);
    // 在暂存区创建或更新标记为删除的文件
    try {
        removalFile.createNewFile();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

```



## status

```java
public static void status() {
    // 1. 获取当前分支
    String currentBranch = getCurrentBranch();

    // 2. 获取所有分支
    List<String> branches = getAllBranches();

    // 3. 获取已暂存的文件
    List<String> stagedFiles = getStagedFiles();

    // 4. 获取已删除的文件
    List<String> removedFiles = getRemovedFiles();

    // 5. 获取未暂存的修改
    List<String> modifiedNotStagedFiles = getModifiedNotStagedFiles();

    // 6. 获取未追踪的文件
    List<String> untrackedFiles = getUntrackedFiles();

    // 7. 输出格式化
    System.out.println("=== Branches ===");
    for (String branch : branches) {
        if (branch.equals(currentBranch)) {
            System.out.println("*" + branch);
        } else {
            System.out.println(branch);
        }
    }
    System.out.println();

    System.out.println("=== Staged Files ===");
    for (String file : stagedFiles) {
        System.out.println(file);
    }
    System.out.println();

    System.out.println("=== Removed Files ===");
    for (String file : removedFiles) {
        System.out.println(file);
    }
    System.out.println();

    System.out.println("=== Modifications Not Staged For Commit ===");
    for (String file : modifiedNotStagedFiles) {
        System.out.println(file);
    }
    System.out.println();

    System.out.println("=== Untracked Files ===");
    for (String file : untrackedFiles) {
        System.out.println(file);
    }
    System.out.println();
}

// 各个辅助方法的实现
private static String getCurrentBranch() {
    // 实现获取当前分支的逻辑
}

private static List<String> getAllBranches() {
    // 实现获取所有分支的逻辑
}

private static List<String> getStagedFiles() {
    // 实现获取已暂存文件的逻辑
}

private static List<String> getRemovedFiles() {
    // 实现获取已删除文件的逻辑
}

private static List<String> getModifiedNotStagedFiles() {
    // 实现获取未暂存的修改文件的逻辑
}

private static List<String> getUntrackedFiles() {
    // 实现获取未追踪文件的逻辑
}

```



## log

```java
public static void logCommand() {
    // 1. 获取当前 HEAD 的 commit ID
    String headCommitId = readHeadCommitId();

    // 2. 初始化一个当前 commit 变量
    Commit currentCommit = getCommitById(headCommitId);

    // 3. 遍历 commit 历史
    while (currentCommit != null) {
        // 4. 输出 commit 信息
        System.out.println("===");
        System.out.println("commit " + currentCommit.getId());
        
        // 处理合并 commit 的情况
        if (currentCommit.isMerge()) {
            String firstParentId = currentCommit.getFirstParentId();
            String secondParentId = currentCommit.getSecondParentId();
            System.out.println("Merge: " + firstParentId.substring(0, 7) + " " + secondParentId.substring(0, 7));
        }
        
        // 5. 输出时间戳
        Date date = currentCommit.getDate(); // 假设这个方法返回一个 Date 对象
        System.out.println("Date: " + formatDate(date)); // 你可以使用 java.util.Date 和 java.util.Formatter 来格式化
        
        // 6. 输出 commit 消息
        System.out.println(currentCommit.getMessage());
        
        // 7. 移动到下一个父 commit
        currentCommit = getCommitById(currentCommit.getParentId());
    }
}

private static String readHeadCommitId() {
    // 读取 HEAD 文件，返回 commit ID
}

private static Commit getCommitById(String commitId) {
    // 从对象存储中获取 commit 对象
}

private static String formatDate(Date date) {
    // 使用 Formatter 格式化时间戳
}
```


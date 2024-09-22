package gitlet;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Jack
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    /** 父提交的SHA-1值 */
    private String parent1Id;
//    private transient Commit parent1; // 瞬态字段，不参与序列化
    /** merge需要用到parent2ID */
    private String parent2Id;
//    private transient Commit parent2;
    /** 自己的SHA-1值 */
    private String Id;
    /** 用来说明这个commit是HEAD指向的分支合并到哪个分支上产生的 */
//    private String mergedBranch;
    /** fileName->blobs文件哈希值 的Map */
    private Map<String, String> blobs = new TreeMap<String, String>();
    /** 用于初始提交的Commit构造函数 */
    public Commit() {
        message = "initial commit";
        timestamp = new Date(0);
    }
    /** 用于其他提交的Commit构造函数 */
    public Commit(String message, String parent1Id) {
        this(message, parent1Id, null);
    }
    public Commit(String message, String parent1Id, String parent2Id) {
        this.message = message;
        this.parent1Id = parent1Id;
        this.parent2Id = parent2Id;
    }

    public String getFileSHA1ByFileName(String fileName) {
        return blobs.get(fileName);
    }

    public void addFile(String fileName, String sha1) {
        blobs.put(fileName, sha1);
    }

    public Set<String> getFileNames() {
        return blobs.keySet();
    }

    public File getFileByName(String fileName) {
        String sha1 = getFileSHA1ByFileName(fileName);
        File file = Repository.getFileBySHA1(sha1);
        return file;
    }

    public void removeFile(String fileName) {
        blobs.remove(fileName);
    }

    public String getId() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(timestamp);
    }

    public String getParent1Id() {
        return parent1Id;
    }

    public String getParent2Id() {
        return parent2Id;
    }

//    public Commit getParent1() {
//        return parent1;
//    }
//
//    public Commit getParent2() {
//        return parent2;
//    }
//
//    public String getMergedBranch() {
//        return mergedBranch;
//    }

    public Map<String, String> getBlobs() {
        return blobs;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setParent1Id(String parent1Id) {
        this.parent1Id = parent1Id;
    }

//    public void setParent1(Commit parent1) {
//        this.parent1 = parent1;
//    }

    public void setParent2Id(String parent2Id) {
        this.parent2Id = parent2Id;
    }

//    public void setParent2(Commit parent2) {
//        this.parent2 = parent2;
//    }

    public void setId(String id) {
        Id = id;
    }

//    public void setMergedBranch(String mergedBranch) {
//        this.mergedBranch = mergedBranch;
//    }

    public void setBlobs(Map<String, String> blobs) {
        this.blobs = blobs;
    }
}

package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> list = new AListNoResizing<>();
        BuggyAList<Integer> buggyList = new BuggyAList<>();
        list.addLast(4);
        list.addLast(5);
        list.addLast(6);
        buggyList.addLast(4);
        buggyList.addLast(5);
        buggyList.addLast(6);
        assertEquals(list.removeLast(), buggyList.removeLast());
        assertEquals(list.removeLast(), buggyList.removeLast());
        assertEquals(list.removeLast(), buggyList.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggyList = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggyList.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                System.out.println("size: " + size);
            } else if (operationNumber == 2) {
                if (L.size() >= 1) {
                    Integer x = L.removeLast();
                    Integer y = buggyList.removeLast();
                    assertEquals(x, y);
                    System.out.println("RemoveLast(" + x + ")");
                }
            } else if (operationNumber == 3) {
                if (L.size() >= 1) {
                    Integer x = L.getLast();
                    Integer y = buggyList.getLast();
                    assertEquals(x, y);
                    System.out.println("getLast(" + x + ")");
                }
            }
        }
    }
}

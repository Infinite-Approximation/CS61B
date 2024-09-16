package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;
public class MaxArrayDequeTest {
    private static class IntergerComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    }
    @Test
    public void maxWithLocalComparatorTest() {
        MaxArrayDeque<Integer> deque = new MaxArrayDeque<>(new IntergerComparator());
        deque.addLast(1);
        deque.addLast(100);
        deque.addLast(3);
        deque.addLast(4);
        assertEquals(100, (int) deque.max());
    }
}

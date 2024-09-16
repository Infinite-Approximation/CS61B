package deque;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;
public class ArrayDequeTest {
    @Test
    public void addFirstTest() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i < 8; i++) {
            deque.addFirst(i);
        }
        deque.addFirst(8);
        deque.printDeque();
        assertEquals(9, deque.size());
    }

    @Test
    public void addLastAndFirstTest() {
        ArrayDeque<String> deque = new ArrayDeque<>();
        deque.addLast("a");
        deque.addLast("b");
        deque.addFirst("c");
        deque.addLast("d");
        deque.addLast("e");
        deque.addFirst("f");
        deque.addLast("g");
        deque.addLast("h");
        deque.addLast("Z");
        assertEquals("f", deque.get(0));
    }

    @Test
    public void iteratorTest() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        Iterator<Integer> it = deque.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    @Test
    public void equalsTest() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        ArrayDeque<Integer> deque2 = new ArrayDeque<>();
        deque.addFirst(1);
        deque.addFirst(2);
        deque.addFirst(3);
        deque2.addFirst(1);
        deque2.addFirst(2);
        deque2.addFirst(3);
        assertEquals("Not equal", deque, deque2);
    }
}

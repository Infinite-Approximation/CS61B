package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.In;
import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    /**
     * @source: AssertEqualsStringDemo.java
     */
    @Test
    public void test1() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> sad2 = new ArrayDequeSolution<>();
        String s = "";
        int N = 5000;
        for (int i = 0; i < 5000; i += 1) {
            double operationNum = StdRandom.uniform(0, 4);
            if (operationNum == 0) {
                sad1.addLast(i);
                sad2.addLast(i);
                s = s + "addLast(" + i + ")" + "\n";
            } else if (operationNum == 1){
                sad1.addFirst(i);
                sad2.addFirst(i);
                s = s + "addFirst(" + i + ")" + "\n";
            } else if (operationNum == 2 && !sad2.isEmpty()){
                s = s + "removeLast()" + "\n";
                assertEquals(s, sad2.removeLast(), sad1.removeLast());
            } else if (operationNum == 3 && !sad2.isEmpty()){
                s = s + "removeLast()" + "\n";
                assertEquals(s, sad2.removeFirst(), sad1.removeFirst());
            }
        }
    }
}

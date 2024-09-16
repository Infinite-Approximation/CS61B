package deque;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size = 0;
    private int nextFirst = 4;
    private int nextLast = 5;
    private int FACTOR = 2;
    public ArrayDeque() {
        items = (T[]) new Object[8];
    }

    private int getRealIndex(int index, int offset) {
        return (index + offset + items.length) % items.length;
    }
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * FACTOR);
        }
        items[nextFirst] = item;
        nextFirst = getRealIndex(nextFirst, -1);
        size++;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(size * FACTOR);
        }
        items[nextLast] = item;
        nextLast = getRealIndex(nextLast, +1);
        size++;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 4);
        }
        nextFirst = getRealIndex(nextFirst, +1);
        T item = items[nextFirst];
        items[nextFirst] = null;
        size--;
        return item;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 4);
        }
        nextLast = getRealIndex(nextLast, -1);
        T item = items[nextLast];
        items[nextLast] = null;
        size--;
        return item;
    }

    public void printDeque() {
        int start = getRealIndex(nextFirst, +1);
        int count = 0;
        int printTimes = size;
        while (printTimes-- != 0) {
            System.out.print(items[start] + " ");
            start = (start + 1) % items.length;
        }
        System.out.println();
    }

    public int size() {
        return size;
    }

    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        int start = getRealIndex(nextFirst, +1);
        int count = 0;
        int copyTimes = size;
        while (copyTimes-- != 0) {
            newItems[count++] = items[start];
            start = getRealIndex(start, +1);
        }
        items = newItems;
        nextFirst = capacity - 1;
        nextLast = count;
    }

    public T get(int index) {
        int getIndex = getRealIndex(nextFirst + 1, index);
        return items[getIndex];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int start = getRealIndex(nextFirst, +1);
        private int sizeOfArray = size;
        @Override
        public boolean hasNext() {
            return (sizeOfArray > 0);
        }

        @Override
        public T next() {
            T data = items[start];
            start = getRealIndex(start, +1);
            sizeOfArray--;
            return data;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Deque<?> that = (Deque<?>) o;
        if (this.size() != that.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(that.get(i))) {
                return false;
            }
        }
        return true;
    }
}

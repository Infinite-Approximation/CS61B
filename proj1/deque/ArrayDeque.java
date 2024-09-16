package deque;

public class ArrayDeque<T> {
    private T[] items;
    private int size = 0;
    private int nextFirst = 4;
    private int nextLast = 5;

    public ArrayDeque() {
        items = (T[]) new Object[8];
    }

    public int getItemsLength() {
        return items.length;
    }
    private int getRealIndex(int index, int offset) {
        return (index + offset + items.length) % items.length;
    }
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size + 1);
        }
        items[nextFirst] = item;
        nextFirst = getRealIndex(nextFirst, -1);
        size++;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(size + 1);
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

    public boolean isEmpty() {
        return size == 0;
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
}

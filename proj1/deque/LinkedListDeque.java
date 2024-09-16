package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    private class Node {
        private Node prev;
        private T data;
        private Node next;

        Node(T data) {
            this.data = data;
        }
    }

    private int size;
    private Node sentinel;

    public LinkedListDeque() {
        sentinel = new Node(null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    public void addFirst(T item) {
        if (size == 0) {
            addLast(item);
            return;
        }
        Node node = new Node(item);
        node.next = sentinel.next;
        sentinel.next.prev = node;
        sentinel.next = node;
        node.prev = sentinel;
        size++;
    }

    public void addLast(T item) {
        Node node = new Node(item);
        sentinel.prev.next = node;
        node.prev = sentinel.prev;
        node.next = sentinel;
        sentinel.prev = node;
        size++;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.data + " ");
            p = p.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return removeLast();
        }
        Node removeNode = sentinel.next;
        sentinel.next.next.prev = sentinel;
        sentinel.next = sentinel.next.next;
        size--;
        return removeNode.data;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node removeNode = sentinel.prev;
        removeNode.prev.next = sentinel;
        sentinel.prev = removeNode.prev;
        size--;
        return removeNode.data;
    }

    public T get(int index) {
        Node p = sentinel.next;
        while (index != 0) {
            p = p.next;
            index--;
        }
        return p.data;
    }

    public T getRecursive(int index) {
        return getRecursiveNode(sentinel.next, index);
    }

    private T getRecursiveNode(Node p, int index) {
        if (index == 0) {
            return p.data;
        }
        return getRecursiveNode(p.next, index - 1);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinkedListDeque that = (LinkedListDeque) o;
        if (this.size() != that.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!this.get(i).equals(that.get(i))) {
                return false;
            }
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        Node p = sentinel.next;

        @Override
        public boolean hasNext() {
            return p != sentinel;
        }

        @Override
        public T next() {
            T data = p.data;
            p = p.next;
            return data;
        }
    }
}

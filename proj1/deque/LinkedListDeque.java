package deque;

public class LinkedListDeque<T> {
    public class Node {
        public Node prev;
        public T data;
        public Node next;
        public Node(T data) {
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

    public Node getSentinel() {
        return sentinel;
    }
    public void addFirst(T item) {
        Node node = new Node(item);
        node.next = sentinel.next;
        sentinel.next = node;
        node.prev = sentinel;
        if (size == 0) {
            sentinel.prev = node;
        }
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

    public boolean isEmpty() {
        return size == 0;
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
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
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

    public T getRecursiveNode(Node p, int index) {
        if (index == 0) {
            return p.data;
        }
        return getRecursiveNode(p.next, index - 1);
    }

    public boolean equals(Object o) {
        if (o instanceof LinkedListDeque) {
            Node p1 = sentinel.next;
            Node p2 = ((LinkedListDeque) o).getSentinel().next;
            while (p1 != sentinel) {
                if (!p1.data.equals(p2.data)) {
                    return false;
                }
                p1 = p1.next;
                p2 = p2.next;
            }
            return true;
        }
        return false;
    }

    // TODO: public Iterator<T> iterator()
}

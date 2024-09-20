package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node root;
    private int size = 0;
    private Set<K> set = new HashSet<>();
    private class Node {
        private K key;
        private V value;
        private Node left, right;
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }
        return getNode(root, key) != null;
    }

    /**
     * 这个函数是get(K key)的加强版，为了区分返回值为null是因为 key对应的value是null，还是说因为没有找到返回null
     * @param node
     * @param key
     * @return 如果为null，说明没有key对应的Node，否则就返回key对应的Node
     */
    private Node getNode(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return getNode(node.right, key);
        } else if (cmp < 0) {
            return getNode(node.left, key);
        } else {
            return node;  // 返回找到的节点，而不是值
        }
    }

    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }
        return get(root, key);
    }

    private V get(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return get(node.right, key);
        } else if (cmp < 0) {
            return get(node.left, key);
        } else {
            return node.value;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }
        if (get(key) == null) {
            size++;
        }
        root = put(root, key, value);
    }

    private Node put(Node node, K key, V value) {
        if (node == null) {
            return new Node(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node node) {
        if (node == null) {
            return;
        }
        printInOrder(node.left);
        System.out.println("key: " + node.key + " value: " + node.value);
        printInOrder(node.right);
    }

    @Override
    public Set<K> keySet() {
        generateKeySet(root);
        return set;
    }

    private void generateKeySet(Node cur) {
        if (cur == null) {
            return;
        }
        generateKeySet(cur.left);
        set.add(cur.key);
        generateKeySet(cur.right);
    }

    @Override
    public V remove(K key) {
        Node node = getNode(root, key);
        if (node == null) {
            return null;
        }
        size--;
        root = remove(root, key);
        return node.value;
    }

    private Node remove(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = remove(node.right, key);
        } else if (cmp < 0) {
            node.left = remove(node.left, key);
        } else {
            // 前两个if处理了只有一个子节点和没有子节点的情况。
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            // 开始处理有两个子节点的情况，这里采用取右边的最小值来替代key对应的节点。
            Node tempNode = node;
            node = min(node.right);
            node.right = deleteMin(tempNode.right);
            node.left = tempNode.left;
        }
        return node;
    }

    private Node deleteMin(Node node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = deleteMin(node.left);
        return node;
    }
    private K min() {
        return min(root).key;
    }

    private Node min(Node node) {
        if (node.left == null) {
            return node;
        }
        return min(node.left);
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }
}

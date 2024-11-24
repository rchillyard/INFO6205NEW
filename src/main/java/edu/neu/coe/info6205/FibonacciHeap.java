package edu.neu.coe.info6205;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FibonacciHeap<T> {
    private Node<T> min;
    private int size;
    private final Comparator<T> comparator;

    public FibonacciHeap(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    private static class Node<T> {
        T value;
        Node<T> parent;
        Node<T> child;
        Node<T> left;
        Node<T> right;
        int degree;
        boolean mark;

        Node(T value) {
            this.value = value;
            this.left = this;
            this.right = this;
        }
    }

    public void insert(T value) {
        Node<T> node = new Node<>(value);
        if (min == null) {
            min = node;
        } else {
            addNode(node, min);
            if (comparator.compare(value, min.value) < 0) {
                min = node;
            }
        }
        size++;
    }

    public T removeMin() {
        Node<T> z = min;
        if (z != null) {
            if (z.child != null) {
                Node<T> child = z.child;
                do {
                    Node<T> next = child.right;
                    addNode(child, z);
                    child.parent = null;
                    child = next;
                } while (child != z.child);
            }
            removeNode(z);
            if (z == z.right) {
                min = null;
            } else {
                min = z.right;
                consolidate();
            }
            size--;
        }
        return z != null ? z.value : null;
    }

    private void consolidate() {
        Map<Integer, Node<T>> degreeTable = new HashMap<>();
        Node<T> start = min;
        Node<T> w = min;
        do {
            Node<T> x = w;
            int d = x.degree;
            while (degreeTable.containsKey(d)) {
                Node<T> y = degreeTable.get(d);
                if (comparator.compare(x.value, y.value) > 0) {
                    Node<T> temp = x;
                    x = y;
                    y = temp;
                }
                link(y, x);
                degreeTable.remove(d);
                d++;
            }
            degreeTable.put(d, x);
            w = w.right;
        } while (w != start);
        min = null;
        for (Node<T> node : degreeTable.values()) {
            if (min == null) {
                min = node;
            } else {
                addNode(node, min);
                if (comparator.compare(node.value, min.value) < 0) {
                    min = node;
                }
            }
        }
    }

    private void link(Node<T> y, Node<T> x) {
        removeNode(y);
        y.left = y.right = y;
        addChild(x, y);
        y.mark = false;
    }

    private void addNode(Node<T> node, Node<T> root) {
        node.left = root;
        node.right = root.right;
        root.right = node;
        node.right.left = node;
    }

    private void removeNode(Node<T> node) {
        node.left.right = node.right;
        node.right.left = node.left;
    }

    private void addChild(Node<T> parent, Node<T> child) {
        if (parent.child == null) {
            parent.child = child;
        } else {
            addNode(child, parent.child);
        }
        child.parent = parent;
        parent.degree++;
    }

    public boolean isEmpty() {
        return min == null;
    }

    public int size() {
        return size;
    }
}
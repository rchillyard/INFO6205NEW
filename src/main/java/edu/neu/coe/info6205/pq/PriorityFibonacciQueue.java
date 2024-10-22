package edu.neu.coe.info6205.pq;

import edu.neu.coe.info6205.util.Benchmark_Timer;

import java.util.*;



/**
 * Priority Queue Data Structure which uses a fibonacci heap.
 * @param <K>
 */
public class PriorityFibonacciQueue<K> {
    final static String description = "PriorityQueue with FibonacciHeap";
    int totalSpils;
    List<K> spills;
    boolean show;
    K extreme;
    int capacity;
    private final Comparator<K> comparator;

    private Node<K> maxNode;
    private int size;
    private static class Node<K> {
        K data;
        Node<K> parent;
        Node<K> child;
        Node<K> left;
        Node<K> right;
        int numchildren;
        boolean lostchild;

        Node(K data) {
            this.data = data;
            this.right = this;
            this.left = this;
        }
    }

    /**
     * Pass extreme value to give least priority
     *
     * @param n          the desired maximum capacity.
     * @param comparator a comparator for the type K
     */
    public PriorityFibonacciQueue(int n, Comparator<K> comparator, K extreme) {
        this.comparator = comparator;
        this.totalSpils = 0;
        this.capacity = n;
        this.show = false;
        this.spills = new ArrayList<K>();
        this.extreme = extreme;
    }


    /**
     * Insert an element with the given key into this Priority Queue.
     *
     * @param key the value of the key to give
     */
    public void give(K key) {

        Node<K> node = new Node<>(key);
        if (maxNode == null) {
            maxNode = node;
        } else {
            if (Integer.compare(size, capacity) == 0) {
                K minElement = this.removeMin();
                spills.add(minElement);
                this.totalSpils++;
            }
            addNodeToRootList(node);
            if (comparator.compare(node.data, maxNode.data) > 0) {
                maxNode = node;
            }
        }
        size++;
    }

    public K take(K v) {
        return this.extractMax();
    }


    public K findMax() {
        if (maxNode == null) {
            return null;
        }
        return maxNode.data;
    }




    public K extractMax() {
        Node<K> max = maxNode;
        if (max != null) {
            if (max.child != null) {
                Node<K> child = max.child;
                do {
                    Node<K> next = child.right;
                    addNodeToRootList(child);
                    child.parent = null;
                    child = next;
                } while (child != max.child);
            }
            removeNodeFromRootList(max);

            if (max == max.right) {
                maxNode = null;
            } else {
                maxNode = max.right;
                consolidate();
            }
            size--;
        }
        return max != null ? max.data : null;
    }

    private K removeMin() {
        if (maxNode == null) {
            return null;
        }

        Node<K> minNode = maxNode;
        Node<K> current = maxNode;

        do {
            if (comparator.compare(current.data, minNode.data) < 0) {
                minNode = current;
            }
            current = current.right;
        } while (current != maxNode);


        removeNodeFromRootList(minNode);


        if (minNode == minNode.right) {
            maxNode = null;
        } else {
            maxNode = minNode.right;
            consolidate();
        }

        size--;
        return minNode.data;
    }





    boolean unordered(Node<K> node, Node<K> node2) {
        return (comparator.compare(node.data, node2.data) > 0);
    }
    public void increaseKey(Node<K> node, K newKey) throws Exception {
        if (comparator.compare(newKey, node.data) < 0) {
            throw new Exception("New key is less than current key");
        }
        node.data = newKey;
        Node<K> parent = node.parent;
        if (parent != null && this.unordered(node, parent)) {
            cut(node, parent);
            cascadingCut(parent);
        }
        if (unordered(node, maxNode)) {
            maxNode = node;
        }
    }

    public void delete(Node<K> node) throws Exception {
        increaseKey(node, this.extreme);
        extractMax();
    }

    private void addNodeToRootList(Node<K> node) {
        if (maxNode == null) {
            maxNode = node;
            node.left = node.right = node;
        } else {
            Node<K> left = maxNode.left;
            left.right = node;
            node.left = left;
            node.right = maxNode;
            maxNode.left = node;
        }
    }

    private void removeNodeFromRootList(Node<K> node) {
        if (node == node.right) {
            maxNode = null;
        } else {
            node.left.right = node.right;
            node.right.left = node.left;
        }
    }

    private void consolidate() {
        List<Node<K>> aux = new ArrayList<>(45); // Max degree for a large Fibonacci heap
        for (int i = 0; i < 45; i++) {
            aux.add(null);
        }

        List<Node<K>> rootList = new ArrayList<>();
        Node<K> current = maxNode;
        if (current != null) {
            do {
                rootList.add(current);
                current = current.right;
            } while (current != maxNode);
        }

        for (Node<K> node : rootList) {
            int degree = node.numchildren;
            while (aux.get(degree) != null) {
                Node<K> other = aux.get(degree);
                if (!unordered(node, other)) {
                    Node<K> temp = node;
                    node = other;
                    other = temp;
                }
                link(other, node);
                aux.set(degree, null);
                degree++;
            }
            aux.set(degree, node);
        }

        maxNode = null;
        for (Node<K> node : aux) {
            if (node != null) {
                if (maxNode == null) {
                    maxNode = node;
                } else {
                    addNodeToRootList(node);
                    if (unordered(node, maxNode)) {
                        maxNode = node;
                    }
                }
            }
        }
    }

    private void link(Node<K> child, Node<K> parent) {
        removeNodeFromRootList(child);
        child.left = child.right = child;
        child.parent = parent;
        if (parent.child == null) {
            parent.child = child;
        } else {
            Node<K> sibling = parent.child;
            child.right = sibling.right;
            sibling.right.left = child;
            sibling.right = child;
            child.left = sibling;
        }
        parent.numchildren++;
        child.lostchild = false;
    }

    private void cut(Node<K> node, Node<K> parent) {
        if (node.right != node) {
            node.left.right = node.right;
            node.right.left = node.left;
        }
        parent.numchildren--;
        if (parent.child == node) {
            parent.child = node.right != node ? node.right : null;
        }
        addNodeToRootList(node);
        node.parent = null;
        node.lostchild = false;
    }

    private void cascadingCut(Node<K> node) {
        Node<K> parent = node.parent;
        if (parent != null) {
            if (!node.lostchild) {
                node.lostchild = true;
            } else {
                cut(node, parent);
                cascadingCut(parent);
            }
        }
    }


    public boolean isEmpty() {
        return maxNode == null;
    }

    public int size() {
        return size;
    }

    public static void runBenchmark(int size, int insertions, int deletions) {
        PriorityFibonacciQueue<Integer> pq = new PriorityFibonacciQueue<Integer>(size,  Integer::compare, Integer.MIN_VALUE);
        Random random = new Random();
        int i;
        for (i = 0; i < 4095; i++) {
            pq.give(random.nextInt());
        }
        String currentDescription = PriorityFibonacciQueue.description +  ", Size: " + size;
        Benchmark_Timer<Integer> insertionTimer = new Benchmark_Timer<>(currentDescription, null, pq::give, null);
        double time = insertionTimer.runFromSupplier(() -> random.nextInt(), insertions);
        System.out.println("Insertions time taken for " + insertions + " runs: " + time*insertions);

        Integer maxElement = pq.findMax();
        Benchmark_Timer<Integer> removalTimer = new Benchmark_Timer<Integer>(currentDescription, null, pq::take, null);
        time = removalTimer.runFromSupplier(() -> random.nextInt(), deletions);
        System.out.println("Top element is " + maxElement + ". Deletions time taken for " + deletions + " deletions: " + time*deletions);

    }
}
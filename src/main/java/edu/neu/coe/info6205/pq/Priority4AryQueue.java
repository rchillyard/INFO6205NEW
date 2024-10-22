
package edu.neu.coe.info6205.pq;

import edu.neu.coe.info6205.util.Benchmark_Timer;
import edu.neu.coe.info6205.util.TimeLogger;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Priority Queue Data Structure which uses a binary heap.
 * <p/>
 * It is unlimited in capacity, although there is no code to grow it after it has been constructed.
 * It can serve as a minPQ or a maxPQ (define "max" as either false or true, respectively).
 * <p/>
 * It can support the root at index 1 or the root at index 2 variants.
 * <p/>
 * It follows the code from Sedgewick and Wayne more or less. I have changed the names a bit. For example,
 * the methods to insert and remove the max (or min) element are called "give" and "take," respectively.
 * <p/>
 * It operates on arbitrary Object types which implies that it requires a Comparator to be passed in.
 * <p/>
 * For all details on usage, please see PriorityQueueTest.java
 *
 * @param <K>
 */
public class Priority4AryQueue<K> extends PriorityQueue<K> {
    static String description = "PriorityQueue with 4-ary heap";

    public Priority4AryQueue(boolean max, K[] binHeap, int first, int last, Comparator<K> comparator, boolean floyd) {
        super(max, binHeap, first, last, comparator, floyd);
        this.show = false;
    }

    /**
     * Get the index of the parent of the element at index k
     */
    private int parent(int k) {
        return (k + 1 - first) / 4 + first - 1;
    }

    /**
     * Get the index of the first child of the element at index k.
     * The index of the second child will be one greater than the result.
     */
    private int firstChild(int k) {
        return (k + 1 - first) * 4 + first - 1;
    }

    public static void runBenchmark(int size, boolean floyd, int insertions, int deletions) {
        Integer[] heap = new Integer[size];
        Priority4AryQueue<Integer> pq = new Priority4AryQueue<Integer>(true, heap, 1, 0, Integer::compare, floyd);
        Random random = new Random();
        int i;
        for (i = 0; i < 4095; i++) {
            pq.give(random.nextInt());
        }

        String currentDescription = Priority4AryQueue.description + ", Floyd=" + (floyd ? "True": "False") + ", Size:" + size;

        Benchmark_Timer<Integer> insertionTimer = new Benchmark_Timer<Integer>(currentDescription, null, pq::give, null);
        double time = insertionTimer.runFromSupplier(() -> random.nextInt(), insertions);
        System.out.println("" + pq.totalSpils + " spills, Insertions time taken for " + insertions + " runs: " + time*insertions);

        Benchmark_Timer<Integer> removalTimer = new Benchmark_Timer<Integer>(currentDescription, null, pq::intTakeOut, null);
        time = removalTimer.runFromSupplier(() -> random.nextInt(), deletions);
        System.out.println("Top element is " + pq.topElement + ". Deletions time taken for " + deletions + " deletions: " + time*deletions);

    }

}

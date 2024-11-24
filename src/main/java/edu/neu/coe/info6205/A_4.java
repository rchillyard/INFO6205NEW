package edu.neu.coe.info6205;
import edu.neu.coe.info6205.A_4.BinaryHeap;
import edu.neu.coe.info6205.A_4.FourAryHeap;
import edu.neu.coe.info6205.A_4.FourAryHeapFloyd;
import edu.neu.coe.info6205.pq.PriorityQueue;
import edu.neu.coe.info6205.util.Benchmark;
import edu.neu.coe.info6205.util.Benchmark_Timer;
import java.util.Random;
import java.util.Arrays;
import java.util.Comparator;


public class A_4 { 
public static void main(String[] args) {
        int M = 4095;
        int insertions = 16000;
        int removals = 4000;

        Random random = new Random();

        // Benchmark for Basic Binary Heap
        Benchmark<BinaryHeap<Integer>> binaryHeapBenchmark = new Benchmark_Timer<>(
                "Binary Heap",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        BinaryHeap<Integer> binaryHeap = new BinaryHeap<>(M, Comparator.comparingInt(a -> a));
        double binaryHeapTime = binaryHeapBenchmark.run(binaryHeap, 1);
        System.out.println("Binary Heap Time: " + binaryHeapTime + " ms");

        // Benchmark for Binary Heap with Floyd's trick
        Benchmark<BinaryHeapFloyd<Integer>> floydHeapBenchmark = new Benchmark_Timer<>(
                "Binary Heap with Floyd's Trick",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        BinaryHeapFloyd<Integer> floydHeap = new BinaryHeapFloyd<>(M, Comparator.comparingInt(a -> a));
        double floydHeapTime = floydHeapBenchmark.run(floydHeap, 1);
        System.out.println("Binary Heap with Floyd's Trick Time: " + floydHeapTime + " ms");

        // Benchmark for 4-ary Heap
        Benchmark<FourAryHeap<Integer>> fourAryHeapBenchmark = new Benchmark_Timer<>(
                "4-ary Heap",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        FourAryHeap<Integer> fourAryHeap = new FourAryHeap<>(M, Comparator.comparingInt(a -> a));
        double fourAryHeapTime = fourAryHeapBenchmark.run(fourAryHeap, 1);
        System.out.println("4-ary Heap Time: " + fourAryHeapTime + " ms");

        // Benchmark for 4-ary Heap with Floyd's trick
        Benchmark<FourAryHeapFloyd<Integer>> fourAryFloydHeapBenchmark = new Benchmark_Timer<>(
                "4-ary Heap with Floyd's Trick",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        FourAryHeapFloyd<Integer> fourAryFloydHeap = new FourAryHeapFloyd<>(M, Comparator.comparingInt(a -> a));
        double fourAryFloydHeapTime = fourAryFloydHeapBenchmark.run(fourAryFloydHeap, 1);
        System.out.println("4-ary Heap with Floyd's Trick Time: " + fourAryFloydHeapTime + " ms");



}

public static class FourAryHeapFloyd<T> extends FourAryHeap<T> {
    public FourAryHeapFloyd(int capacity, Comparator<T> comparator) {
        super(capacity, comparator);
    }

    @Override
    public void insert(T value) {
        if (size == heap.length) throw new IllegalStateException("Heap is full");
        heap[size++] = value;
        if (size == heap.length) {
            for (int i = (size / 4) - 1; i >= 0; i--) {
                siftDown(i);
            }
        }
    }
}

public static class FourAryHeap<T> {
    protected T[] heap;
    protected int size;
    private final Comparator<T> comparator;

    @SuppressWarnings("unchecked")
    public FourAryHeap(int capacity, Comparator<T> comparator) {
        this.heap = (T[]) new Object[capacity];
        this.size = 0;
        this.comparator = comparator;
    }

    public void insert(T value) {
        if (size == heap.length) throw new IllegalStateException("Heap is full");
        heap[size] = value;
        siftUp(size++);
    }

    public T remove() {
        if (size == 0) throw new IllegalStateException("Heap is empty");
        T result = heap[0];
        heap[0] = heap[--size];
        siftDown(0);
        return result;
    }

    private void siftUp(int index) {
        T value = heap[index];
        while (index > 0) {
            int parentIndex = (index - 1) / 4;
            if (comparator.compare(value, heap[parentIndex]) >= 0) break;
            heap[index] = heap[parentIndex];
            index = parentIndex;
        }
        heap[index] = value;
    }

    protected void siftDown(int index) {
        T value = heap[index];
        while (index * 4 + 1 < size) {
            int childIndex = index * 4 + 1;
            int minChildIndex = childIndex;
            for (int i = 1; i < 4; i++) {
                if (childIndex + i < size && comparator.compare(heap[childIndex + i], heap[minChildIndex]) < 0) {
                    minChildIndex = childIndex + i;
                }
            }
            if (comparator.compare(value, heap[minChildIndex]) <= 0) break;
            heap[index] = heap[minChildIndex];
            index = minChildIndex;
        }
        heap[index] = value;
    }
}


public static class BinaryHeapFloyd<T> {
    private T[] heap;
    private int size;
    private final Comparator<T> comparator;

    @SuppressWarnings("unchecked")
    public BinaryHeapFloyd(int capacity, Comparator<T> comparator) {
        this.heap = (T[]) new Object[capacity];
        this.size = 0;
        this.comparator = comparator;
    }

    public void insert(T item) {
        if (size == heap.length) {
            heap = Arrays.copyOf(heap, size * 2);
        }
        heap[size] = item;
        size++;
        heapifyUp(size - 1);
    }

    public T remove() {
        if (size == 0) {
            return null;
        }
        T item = heap[0];
        heap[0] = heap[size - 1];
        size--;
        heapifyDown(0);
        return item;
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (comparator.compare(heap[index], heap[parentIndex]) >= 0) {
                break;
            }
            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    private void heapifyDown(int index) {
        while (index < size / 2) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallestChild = leftChild;

            if (rightChild < size && comparator.compare(heap[rightChild], heap[leftChild]) < 0) {
                smallestChild = rightChild;
            }

            if (comparator.compare(heap[index], heap[smallestChild]) <= 0) {
                break;
            }

            swap(index, smallestChild);
            index = smallestChild;
        }
    }

    private void swap(int i, int j) {
        T temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
}

public static class BinaryHeap<T> {
    private final PriorityQueue<T> heap;

    public BinaryHeap(int initialCapacity, Comparator<? super T> comparator) {
        // Explicitly specify the type arguments for PriorityQueue
        this.heap = new PriorityQueue<T>(initialCapacity, 1, true, (Comparator<T>) comparator, true);
    }

    public void insert(T item) {
        heap.add(item);
    }

    public T remove() {
        return heap.poll();
    }
}

}
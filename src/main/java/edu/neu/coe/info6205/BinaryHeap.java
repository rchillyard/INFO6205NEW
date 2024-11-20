package edu.neu.coe.info6205;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class BinaryHeap<T> {
    private T[] heap;
    private int size;
    private final Comparator<T> comparator;

    @SuppressWarnings("unchecked")
    public BinaryHeap(int capacity, Comparator<T> comparator) {
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

    private void heapifyUp(int index) {
        T item = heap[index];
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            T parent = heap[parentIndex];
            if (comparator.compare(item, parent) >= 0) {
                break;
            }
            heap[index] = parent;
            index = parentIndex;
        }
        heap[index] = item;
    }

    public T remove() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        T result = heap[0];
        T lastItem = heap[size - 1];
        heap[0] = lastItem;
        size--;
        heapifyDown(0);
        return result;
    }

    private void heapifyDown(int index) {
        T item = heap[index];
        while (index < size / 2) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = 2 * index + 2;
            int minChildIndex = leftChildIndex;
            if (rightChildIndex < size && comparator.compare(heap[rightChildIndex], heap[leftChildIndex]) < 0) {
                minChildIndex = rightChildIndex;
            }
            if (comparator.compare(item, heap[minChildIndex]) <= 0) {
                break;
            }
            heap[index] = heap[minChildIndex];
            index = minChildIndex;
        }
        heap[index] = item;
    }

    
}
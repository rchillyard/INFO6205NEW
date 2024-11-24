package edu.neu.coe.info6205;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;


public class FourAryHeap<T> {
    protected T[] heap;
    protected int size;
    protected final Comparator<T> comparator;

    @SuppressWarnings("unchecked")
    public FourAryHeap(int capacity, Comparator<T> comparator) {
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
            int parentIndex = (index - 1) / 4;
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
        while (index < size / 4) {
            int minChildIndex = 4 * index + 1;
            for (int i = 2; i <= 4; i++) {
                int childIndex = 4 * index + i;
                if (childIndex < size && comparator.compare(heap[childIndex], heap[minChildIndex]) < 0) {
                    minChildIndex = childIndex;
                }
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
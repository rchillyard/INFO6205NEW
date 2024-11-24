package edu.neu.coe.info6205;

import java.util.Arrays;
import java.util.Comparator;

public class FourAryHeapFloyd<T> extends FourAryHeap<T> {
    public FourAryHeapFloyd(int capacity, Comparator<T> comparator) {
        super(capacity, comparator);
    }

    @Override
       public void insert(T value) {
        if (size == heap.length) {
            heap = Arrays.copyOf(heap, size * 2); // Double the size of the heap array
        }
        heap[size++] = value;
        if (size == heap.length) {
            for (int i = (size / 4) - 1; i >= 0; i--) {
                siftDown(i);
            }
        }
    }
    // public void insert(T value) {
    //     if (size == heap.length) throw new IllegalStateException("Heap is full");
    //     heap[size++] = value;
    //     if (size == heap.length) {
    //         for (int i = (size / 4) - 1; i >= 0; i--) {
    //             siftDown(i);
    //         }
    //     }
    // }

    private void siftDown(int index) {
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
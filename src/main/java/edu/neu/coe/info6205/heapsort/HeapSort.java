package edu.neu.coe.info6205.heapsort;

public class HeapSort<X extends Comparable<X>> implements Sort<X> {

    /**
     * Constructor for InsertionSort
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public HeapSort(Helper<X> helper) {
        this.helper = helper;
    }

    public HeapSort() {
        this(new Helper<>("HeapSort"));
    }

    @Override
    public void sort(X[] a, int from, int to) {
        int n = a.length; 
        for (int i = n / 2 - 1; i >= 0; i--) 
            sink(a, n, i); 
        for (int i=n-1; i>=0; i--) 
        { 
            helper.swap(a, 0, i);
            sink(a, i, 0); 
        };
    }
    void sink(X arr[], int n, int i) 
    { 
        int largest = i;
        int l = 2*i + 1;
        int r = 2*i + 2; 
        if (l < n && helper.compare(arr[l], arr[largest]) > 0) 
            largest = l; 
        if (r < n && helper.compare(arr[r], arr[largest]) > 0) 
            largest = r; 
        if (largest != i) 
        {   
            helper.swap(arr, i, largest);
            sink(arr, n, largest); 
        } 
    } 
 
		

    @Override
    public String toString() {
        return helper.toString();
    }

    @Override
    public Helper<X> getHelper() {
        return helper;
    }

    private final Helper<X> helper;
}

package edu.neu.coe.info6205.sort.elementary;

import java.util.Comparator;

/**
 * This is a basic (no frills) implementation of insertion sort.
 * It does not extend Sort, it doesn't have a Helper, nor does it employ any optimizations.
 */
public class InsertionSortBasic<S> {

    public static <X> InsertionSortBasic<X> create() {
        //noinspection unchecked
        return new InsertionSortBasic<>((o1, o2) ->  ((Comparable<X>) o1).compareTo(o2));
    }

    public void sort(S[] a) {
        sort(a, 0, a.length);
    }

    /**
     * Sort the array a starting with index from and end just before index to.
     *
     * @param a    the array of which we must sort a partition.
     * @param from the lowest index of the partition to be sorted.
     * @param to   one more than the highest index of the partition to be sorted.
     */
    public void sort(S[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            insert(from, i, a);
        }
    }

    public InsertionSortBasic(Comparator<S> comparator) {
        this.comparator = comparator;
    }

    /**
     * Move (insert) the element a[i] into its proper place amongst the (sorted) part of the array
     * (starting with from and ending at i-1).
     *
     * @param from the first (left-most) element of the partition being sorted.
     * @param i    the index of the transitional element.
     * @param a    the (sorted) array into which the transitional element should be moved.
     */
    private void insert(int from, int i, S[] a) {
        // TO BE IMPLEMENTED  : implement inner loop of insertion sort using comparator
        S key = a[i];
        int j = i - 1;
        while (j>=from && comparator.compare(a[j], key) > 0) {
            a[j+1] = a[j];
            j--;
        }
        a[j+1] = key;
        // END SOLUTION
    }

    private void swap(Object[] a, int j, int i) {
        Object temp = a[j];
        a[j] = a[i];
        a[i] = temp;
    }

    private final Comparator<S> comparator;
}
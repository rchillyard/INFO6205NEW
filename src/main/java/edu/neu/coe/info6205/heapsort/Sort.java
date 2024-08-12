package edu.neu.coe.info6205.heapsort;


import java.util.Arrays;

public interface Sort<X extends Comparable<X>> {

    /**
     * Generic, non-mutating sort method which allows for explicit determination of the makeCopy option.
     *
     * @param xs sort the array xs, returning the sorted result, leaving xs unchanged.
     * @param makeCopy if set to true, we make a copy first and sort that.
     */
    default X[] sort(X[] xs, boolean makeCopy) {
        getHelper().setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        sort(result, 0, result.length);
        return result;
    }

    /**
     * Generic, non-mutating sort method.
     *
     * @param xs sort the array xs, returning the sorted result, leaving xs unchanged.
     */
    default X[] sort(X[] xs) {
        return sort(xs, true);
    }

    /**
     * Generic, mutating sort method which operates on a sub-array
     *
     * @param xs   sort the array xs from "from" to "to".
     * @param from the index of the first element to sort
     * @param to   the index of the first element not to sort
     */
    void sort(X[] xs, int from, int to);

    /**
     * Get the Helper associated with this Sort.
     * @return the Helper
     */
    Helper<X> getHelper();
}

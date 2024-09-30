package edu.neu.coe.info6205.threesum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

/**
 * Implementation of ThreeSum which follows the approach of dividing the solution-space into
 * N sub-spaces where each sub-space corresponds to a fixed value for the middle index of the three values.
 * Each sub-space is then solved by expanding the scope of the other two indices outwards from the starting point.
 * Since each sub-space can be solved in O(N) time, the overall complexity is O(N^2).
 * <p>
 * NOTE: The array provided in the constructor MUST be ordered.
 */
public class ThreeSumQuadratic implements ThreeSum {
    /**
     * Construct a ThreeSumQuadratic on a.
     *
     * @param a a sorted array.
     */
    public ThreeSumQuadratic(int[] a) {
        this.a = a;
        length = a.length;
    }

    public Triple[] getTriples() {
        List<Triple> triples = new ArrayList<>();
        for (int i = 0; i < length; i++){
             triples.addAll(getTriples(i));
        }
        Collections.sort(triples);
        return triples.stream().distinct().toArray(Triple[]::new);
    }

    /**
     * Get a list of Triples such that the middle index is the given value j.
     *
     * @param j the index of the middle value.
     * @return a Triple such that
     */
    public List<Triple> getTriples(int j) {
        List<Triple> triples = new ArrayList<>();
        // TO BE IMPLEMENTED  : for each candidate, test if a[i] + a[j] + a[k] = 0.
       //int sum =  a[i] + a[k];
        //int leftPointer = j + 1;
        int leftPointer = 0;
        int rightPointer = length - 1;
        //int sum = a[i] + a[j] + a[k];
       // while (leftPointer < rightPointer) {
         while(leftPointer < j && j < rightPointer){
            int sum = a[leftPointer] + a[j] + a[rightPointer];
            if (sum == 0){
                triples.add(new Triple(a[leftPointer], a[j], a[rightPointer]));
                leftPointer++;
                rightPointer--;
            // while (leftPointer < rightPointer && a[leftPointer] == a[leftPointer - 1]) {leftPointer++;};
            // while (leftPointer < rightPointer && a[rightPointer] == a[rightPointer + 1]) {rightPointer--;};
            while (leftPointer < j && a[leftPointer] == a[leftPointer - 1]) {leftPointer++;};
            while (j < rightPointer && a[rightPointer] == a[rightPointer + 1]) {rightPointer--;};
         }
            else if (sum < 0){
                leftPointer++;
            }else{
                rightPointer--;
            }
        }
        return triples;


//throw new RuntimeException("implementation missing");
    }

    private final int[] a;
    private final int length;
}
package edu.neu.coe.info6205.threesum;

import scala.Int;

import java.util.ArrayList;
import java.util.Arrays;

public class ThreeSumTimer {
    public static void main(String[] args) {


        // Three sum example with increasing length of array
        int[][] testArrays = {
                {-1, 0, 1},                          // Example 1 (Size = 3)
                {-2, -1, 1, 2},                      // Example 2 (Size = 4)
                {-3, -2, -1, 1, 2},                  // Example 3 (Size = 5)
                {-4, -1, 0, 1, 2, 3},                // Example 4 (Size = 6)
                {-5, -3, -2, 0, 1, 2, 4},            // Example 5 (Size = 7)
                {-6, -5, -3, -1, 0, 1, 2, 3},        // Example 6 (Size = 8)
                {-7, -5, -4, -3, 0, 1, 2, 3, 4},     // Example 7 (Size = 9)
                {-8, -6, -4, -2, 0, 1, 3, 4, 5, 6},  // Example 8 (Size = 10)
                {-10, -7, -4, -3, 0, 1, 2, 3, 5, 7, 10}, // Example 9 (Size = 11)
                {-10, -8, -5, -3, 0, 2, 3, 5, 8, 10, 12, 14} // Example 10 (Size = 12)
        };

        for(int[] arr : testArrays){
            ThreeSum target = new ThreeSumQuadraticWithCalipers(arr);
            long startTime = System.nanoTime();
            target.getTriples();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println(duration + " length: " + arr.length);
        }



    }
}

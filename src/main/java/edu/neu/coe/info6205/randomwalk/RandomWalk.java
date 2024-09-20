/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.info6205.randomwalk;

import java.util.Random;

public class RandomWalk {

    private int x = 0;
    private int y = 0;

    private final Random random = new Random();

    /**
     * Private method to move the current position, that's to say the drunkard moves
     *
     * @param dx the distance he moves in the x direction
     * @param dy the distance he moves in the y direction
     */
    private void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * Perform a random walk of m steps
     *
     * @param m the number of steps the drunkard takes
     */
    private void randomWalk(int m) {
        for (int i = 0; i < m; i++) {
            randomMove();
        }
    }

    /**
     * Private method to generate a random move according to the rules of the situation.
     * That's to say, moves can be (+-1, 0) or (0, +-1).
     */
    private void randomMove() {
        boolean ns = random.nextBoolean();
        int step = random.nextBoolean() ? 1 : -1;
        move(ns ? step : 0, ns ? 0 : step);
    }

    /**
     * Method to compute the distance from the origin (the lamp-post where the drunkard starts) to his current position.
     *
     * @return the (Euclidean) distance from the origin to the current position.
     */
    public double distance() {
        // Since, we start from origin (0,0), omitting the 0 in the equation below
         return Math.sqrt(
                 Math.pow(x, 2) + Math.pow(y, 2)
         );
    }

    /**
     * Perform multiple random walk experiments, returning the mean distance.
     *
     * @param m the number of steps for each experiment
     * @param n the number of experiments to run
     * @return the mean distance
     */
    public static double randomWalkMulti(int m, int n) {
        double totalDistance = 0;
        for (int i = 0; i < n; i++) {
            RandomWalk walk = new RandomWalk();
            walk.randomWalk(m);
            totalDistance = totalDistance + walk.distance();
        }
        return totalDistance / n;
    }

    public static void main(String[] args) {
//        if (args.length == 0)
//            throw new RuntimeException("Syntax: RandomWalk steps [experiments]");
        int totalRuns = 60;
        int[] stepValues = new int[] {0, 1, 2, 3, 4,9,16,25,36,49,64,81,100,144,169, 196,225,256,289,324,361, 500,1000, 2000, 5000, 7500};
        int n = 30;
//        final Random r = new Random();
        for ( int m:  stepValues) {
            double stepTotal = 0;
            for (int i = 0; i < totalRuns; i++) {
                double meanDistance = randomWalkMulti(m, n);
//                System.out.println(m + " steps: " + meanDistance + " over " + n + " experiments");
                stepTotal += meanDistance;
            }
            double formula = Math.sqrt(m);
            double avgResults = stepTotal / totalRuns;
            double diff = (formula - avgResults);
            System.out.println("Average of all "+ totalRuns + " Runs is " + avgResults + " for " + m + " steps over " + n + " experiments");
//            System.out.println(
//                    "Average of all above "+ totalRuns + " Runs is "
//                            + avgResults
//            );
//             System.out.println(m + "," + avgResults + "," +  formula + "," + diff);
        //   System.out.println(m + "," + avgResults);
        }
    }

}
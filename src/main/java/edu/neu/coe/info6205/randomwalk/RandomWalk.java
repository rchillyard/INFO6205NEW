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
        // TO BE IMPLEMENTED  do move
        x = x + dx;
        y = y + dy;
         //throw new RuntimeException("Not implemented");
        // END SOLUTION
    }

    /**
     * Perform a random walk of m steps
     *
     * @param m the number of steps the drunkard takes
     */
    private void randomWalk(int m) {
        // TO BE IMPLEMENTED 
        for (int i = 0; i < m; i++){
            randomMove();
        }
  //throw new RuntimeException("implementation missing");
    }

    /**
     * Private method to generate a random move according to the rules of the situation.
     * That's to say, moves can be (+-1, 0) or (0, +-1).
     */
    private void randomMove() {
        boolean ns = random.nextBoolean();  // true if we move north-south or the east-west(false);
        int step = random.nextBoolean() ? 1 : -1;  //it generates either 1 or -1; and 1 means move in the positive direction and -1 means move in the negative direction;
        move(ns ? step : 0, ns ? 0 : step); //if ns is true, it moves ns, and calls move(step,0);
    }

    /**
     * Method to compute the distance from the origin (the lamp-post where the drunkard starts) to his current position.
     *
     * @return the (Euclidean) distance from the origin to the current position.
     */
    public double distance() {
        // TO BE IMPLEMENTED 
        return Math.sqrt(x*x + y*y);
        //return 0.0;
        // END SOLUTION
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
        // if (args.length == 0)
        //     throw new RuntimeException("Syntax: RandomWalk steps [experiments]");
        // int m = Integer.parseInt(args[0]);
        int [] stepValues= {10,20,30,40,50,60};
        int n = 1000;
        for (int m : stepValues){
            double meanDistance = randomWalkMulti(m, n);
            System.out.println(m + " steps: " + meanDistance + " over " + n + " experiments");
    }
        }
    }


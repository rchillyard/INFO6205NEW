package edu.neu.coe.info6205.sort.elementary;


import edu.neu.coe.info6205.util.Stopwatch;
import java.util.Random;


public class InsertionSortBenchmark {
    static Random rd = new Random();

    public static Integer[] getRandomInt(int n) {

        Integer[] arr = new Integer[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rd.nextInt();
        }
        return arr;
    }
    public static Integer[] getOrdered(int n) {

        Integer[] arr = new Integer[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i+100;
        }
        return arr;
    }
    public static Integer[] getReveresed(int n) {
        Integer[] arr = new Integer[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = n-i;
        }
        return arr;
    }
    public static Integer[] getPartial(int n) {
        Integer[] arr = new Integer[n];
        int mid = n/2;

        for (int i = 0; i < arr.length; i++) {
            if (i < mid) {
                arr[i] = rd.nextInt();
            } else {
                arr[i] = i;
            }
        }
        return arr;
    }

    public static void main(String[] args) {
        int [] nValues = new int[] {500, 1000, 2000, 4000, 8000, 16000};
        String[] methods = new String[] {"Random", "Partial", "Ordered", "Reversed"};
        int runs = 100;
        InsertionSortBasic<Integer> sorter = InsertionSortBasic.create();
        for (String method: methods) {
            System.out.println("Running " + method);
            for(int n: nValues) {
                Integer[] input;
                if (method.equals("Random")) {
                    input = getRandomInt(n);
                } else if (method.equals("Partial")) {
                    input = getPartial(n);
                } else if (method.equals("Ordered")) {
                    input= getOrdered(n);
                } else {
                    input = getReveresed(n);
                }
                Stopwatch watch = new Stopwatch();
                for (int i = 0; i < runs; i++) {
                    sorter.sort(input);
                }
                double totalTimeRuntimeInMS =watch.lap();
                double totalTimeInMSPerRun = totalTimeRuntimeInMS / runs;
                watch.close();
                System.out.println("N="+ n + ",Method=" + method + ",Total runtime in ms: " + totalTimeInMSPerRun);

            }
        }

    }
}

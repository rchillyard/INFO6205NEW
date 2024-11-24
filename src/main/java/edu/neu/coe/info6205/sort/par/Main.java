package edu.neu.coe.info6205.sort.par;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * This code has been fleshed out by Ziyao Qiao. Thanks very much.
 * CONSIDER tidy it up a bit.
 */
public class Main {

    public static void main(String[] args) {
        processArgs(args);
        System.out.println("Degree of parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        int cutoff = args.length > 0 ? Integer.parseInt(args[0]) : 1000;
        ParSort.cutoff = cutoff;

        Random random = new Random();
        int[] arraySizes = {500000, 1000000, 2000000, 4000000, 8000000}; // Different array sizes
        ArrayList<Long> timeList = new ArrayList<>();

        for (int size : arraySizes) {
            int[] array = new int[size];
            System.out.println("Array size: " + size);

            for (int j = 10; j < 200; j += 10) { // Larger cutoff values
                ParSort.cutoff = 1000 * (j + 1);
                long totalTime = 0;

                for (int t = 0; t < 10; t++) {
                    for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
                    long startTime = System.currentTimeMillis();
                    ParSort.sort(array, 0, array.length);
                    long endTime = System.currentTimeMillis();
                    totalTime += (endTime - startTime);
                }

                long avgTime = totalTime / 10;
                timeList.add(avgTime);

                System.out.println("cutoff: " + ParSort.cutoff + "\t\tAverage Time: " + avgTime + "ms");
            }
        }

        System.out.println("\nAverage times for different cutoffs:");
        for (int i = 0; i < timeList.size(); i++) {
            System.out.println("Cutoff: " + (1000 * (10 + (i * 10))) + "\t\tAverage Time: " + timeList.get(i) + "ms");
        }
        

        // Random random = new Random();
        // int[] array = new int[2000000];
        // ArrayList<Long> timeList = new ArrayList<>();
        // for (int j = 50; j < 100; j++) {
        //     ParSort.cutoff = 10000 * (j + 1);
        //     // for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
        //     long time;
        //     long startTime = System.currentTimeMillis();
        //     for (int t = 0; t < 10; t++) {
        //         for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
        //         ParSort.sort(array, 0, array.length);
        //     }
        //     long endTime = System.currentTimeMillis();
        //     time = (endTime - startTime);
        //     timeList.add(time);


        //     System.out.println("cutoff：" + (ParSort.cutoff) + "\t\t10times Time:" + time + "ms");

        // }
        try {
            FileOutputStream fis = new FileOutputStream("./src/result.csv");
            OutputStreamWriter isr = new OutputStreamWriter(fis);
            BufferedWriter bw = new BufferedWriter(isr);
            int j = 0;
            for (long i : timeList) {
                String content = (double) 10000 * (j + 1) / 2000000 + "," + (double) i / 10 + "\n";
                j++;
                bw.write(content);
                bw.flush();
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processArgs(String[] args) {
        String[] xs = args;
        while (xs.length > 0)
            if (xs[0].startsWith("-")) xs = processArg(xs);
    }

    private static String[] processArg(String[] xs) {
        String[] result = new String[0];
        System.arraycopy(xs, 2, result, 0, xs.length - 2);
        processCommand(xs[0], xs[1]);
        return result;
    }

    private static void processCommand(String x, String y) {
        if (x.equalsIgnoreCase("N")) setConfig(x, Integer.parseInt(y));
        else
            // TODO sort this out
            if (x.equalsIgnoreCase("P")) //noinspection ResultOfMethodCallIgnored
                ForkJoinPool.getCommonPoolParallelism();
    }

    private static void setConfig(String x, int i) {
        configuration.put(x, i);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<String, Integer> configuration = new HashMap<>();


}
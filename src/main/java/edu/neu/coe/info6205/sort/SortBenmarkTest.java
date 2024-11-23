package edu.neu.coe.info6205.sort;

import edu.neu.coe.info6205.sort.elementary.HeapSort;
import edu.neu.coe.info6205.sort.linearithmic.MergeSort;
import edu.neu.coe.info6205.sort.linearithmic.QuickSort_DualPivot;
import edu.neu.coe.info6205.util.Benchmark;
import edu.neu.coe.info6205.util.Benchmark_Timer;
import edu.neu.coe.info6205.util.Config;
import edu.neu.coe.info6205.util.SortBenchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class SortBenmarkTest {

    public static void main(String[] args) throws IOException {
        int [] sizes = {10000, 20000, 40000, 80000, 160000, 320000, 640000, 1280000, 2560000};
        Config config = Config.load(SortBenmarkTest.class);
        for(int size : sizes){
            runBenchmark(size, config);
        }
    }

    private static void runBenchmark(int size, Config config) {
        Random random = new Random();
        Integer[] array = new Integer[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt();
        }

        // MergeSort
        Helper<Integer> mergeHelper = HelperFactory.create("MergeSort", size, config);
        MergeSort<Integer> mergeSort = new MergeSort<>(mergeHelper);
        runSortBenchmark("MergeSort", array, mergeSort, mergeHelper);

        // QuickSort Dual Pivot
        Helper<Integer> quickHelper = HelperFactory.create("QuickSortDualPivot", size, config);
        QuickSort_DualPivot<Integer> quickSort = new QuickSort_DualPivot<>(quickHelper);
        runSortBenchmark("QuickSortDualPivot", array, quickSort, quickHelper);

        // HeapSort
        Helper<Integer> heapHelper = HelperFactory.create("HeapSort", size, config);
        HeapSort<Integer> heapSort = new HeapSort<>(heapHelper);
        runSortBenchmark("HeapSort", array, heapSort, heapHelper);
    }

    private static void runSortBenchmark(String name, Integer[] array, SortWithHelper<Integer> sorter, Helper<Integer> helper) {
        Benchmark<Integer[]> benchmark = new Benchmark_Timer<>(
                name,
                (xs) -> {
                    helper.init(xs.length);
                    return xs;
                },
                sorter::sort,
                null
        );

        double time = benchmark.run(array, 10);
        System.out.printf("%s (size: %d) took %.2f ms%n", name, array.length, time);

        // Collect instrumentation data
        long comparisons = 0, swaps = 0, hits = 0;
        if (helper instanceof InstrumentedComparableHelper<Integer> instrumentedHelper) {
            System.out.println("Instrumentation data:");
            comparisons = instrumentedHelper.getCompares();
            swaps = instrumentedHelper.getSwaps();
            hits = instrumentedHelper.getHits();
            System.out.printf("%s (size: %d) - Comparisons: %d, Swaps: %d, Hits: %d%n",
                    name, array.length, comparisons, swaps, hits);
        }
        File file = new File("benchmark_results_without_instrumentation.csv");
        boolean isNewFile = !file.exists();

        try (FileWriter fileWriter = new FileWriter(file, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            if (isNewFile) {
                printWriter.println("Name,Size,Time,Comparisons,Swaps,Hits");
            }
            printWriter.printf("%s,%d,%.2f,%d,%d,%d%n", name, array.length, time, comparisons, swaps, hits);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

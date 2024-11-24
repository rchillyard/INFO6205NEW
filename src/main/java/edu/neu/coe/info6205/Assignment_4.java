package edu.neu.coe.info6205;
import edu.neu.coe.info6205.BinaryHeap;
import edu.neu.coe.info6205.FourAryHeap;
import edu.neu.coe.info6205.FourAryHeapFloyd;
import edu.neu.coe.info6205.FibonacciHeap;
import edu.neu.coe.info6205.pq.PriorityQueue;
import edu.neu.coe.info6205.util.Benchmark;
import edu.neu.coe.info6205.util.Benchmark_Timer;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;


public class Assignment_4 { 
public static void main(String[] args) {
        int M = 4095;
        int insertions = 16000;
        int removals = 4000;

        Random random = new Random();

        try (FileWriter writer = new FileWriter("benchmark_results.csv")) {
            writer.append("Heap Type,Time (ms)\n");

        // Benchmark for Basic Binary Heap
        Benchmark<BinaryHeap<Integer>> binaryHeapBenchmark = new Benchmark_Timer<>(
                "Binary Heap",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        BinaryHeap<Integer> binaryHeap = new BinaryHeap<>(M, Integer::compareTo);
        double binaryHeapTime = binaryHeapBenchmark.run(binaryHeap, 10);
        writer.append("Binary Heap," + binaryHeapTime + "\n");
        System.out.println("Basic Binary Heap Time: " + binaryHeapTime + " ms");

        // Benchmark for Binary Heap with Floyd's trick
        Benchmark<BinaryHeapFloyd<Integer>> floydHeapBenchmark = new Benchmark_Timer<>(
                "Binary Heap with Floyd's Trick",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        BinaryHeapFloyd<Integer> binaryHeapFloyd = new BinaryHeapFloyd<>(M, Integer::compareTo);
        double binaryHeapFloydTime = floydHeapBenchmark.run(binaryHeapFloyd, 10);
       writer.append("Binary Heap with Floyd's Trick," + binaryHeapFloydTime + "\n");
       System.out.println("Binary Heap with Floyd's Trick Time: " + binaryHeapFloydTime + " ms");

        // Benchmark for 4-ary Heap
        Benchmark<FourAryHeap<Integer>> fourAryHeapBenchmark = new Benchmark_Timer<>(
                "4-ary Heap",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        FourAryHeap<Integer> fourAryHeap = new FourAryHeap<>(M, Comparator.comparingInt(a -> a));
        double fourAryHeapTime = fourAryHeapBenchmark.run(fourAryHeap, 10);
        writer.append("4-ary Heap," + fourAryHeapTime + "\n");
        System.out.println("4-ary Heap Time: " + fourAryHeapTime + " ms");
    
        // Benchmark for 4-ary Heap with Floyd's trick
        Benchmark<FourAryHeapFloyd<Integer>> fourAryFloydHeapBenchmark = new Benchmark_Timer<>(
                "4-ary Heap with Floyd's Trick",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );

        FourAryHeapFloyd<Integer> fourAryFloydHeap = new FourAryHeapFloyd<>(M, Comparator.comparingInt(a -> a));
        double fourAryFloydHeapTime = fourAryFloydHeapBenchmark.run(fourAryFloydHeap, 1);
        writer.append("4-ary Heap with Floyd's Trick," + fourAryFloydHeapTime + "\n");
        System.out.println("4-ary Heap with Floyd's Trick Time: " + fourAryFloydHeapTime + " ms");
      

        // Benchmark for Fibonacci Heap
        Benchmark<FibonacciHeap<Integer>> fibonacciHeapBenchmark = new Benchmark_Timer<>(
            "Fibonacci Heap",
            null,
            pq -> {
                for (int i = 0; i < insertions; i++) {
                    pq.insert(random.nextInt());
                }
                for (int i = 0; i < removals; i++) {
                    pq.removeMin();
                }
            },
            null
    );

      FibonacciHeap<Integer> fibonacciHeap = new FibonacciHeap<>(Comparator.comparingInt(a -> a));
      double fibonacciHeapTime = fibonacciHeapBenchmark.run(fibonacciHeap, 10);
      writer.append("Fibonacci Heap," + fibonacciHeapTime + "\n");
      System.out.println("Fibonacci Heap Time: " + fibonacciHeapTime + " ms");

            // Close the writer
            writer.flush();
            writer.close();
            } catch (IOException e) {
            e.printStackTrace();
            }
    }
}
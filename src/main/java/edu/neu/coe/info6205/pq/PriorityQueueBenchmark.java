package edu.neu.coe.info6205.pq;


public class PriorityQueueBenchmark {

    public static void main(String[] args) {
        int m = 4095;
        int MAX_INPUTS = 5;

        int insertions = 16000;
        int deletions = 4000;
        int i = 1;
        while(i < MAX_INPUTS) {
            int totalElements = (int)Math.round(m * Math.pow(2, i));
            PriorityQueue.runBenchmark(totalElements, false, insertions, deletions);
            PriorityQueue.runBenchmark(totalElements, true, insertions, deletions);
            Priority4AryQueue.runBenchmark(totalElements, false, insertions, deletions);
            Priority4AryQueue.runBenchmark(totalElements, true, insertions, deletions);
            PriorityFibonacciQueue.runBenchmark(totalElements, insertions, deletions);
            i+=1;
            System.out.println("********");
        }
    }
}

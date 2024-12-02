package edu.neu.coe.info6205.final_project;

import edu.neu.coe.info6205.util.Benchmark_Timer;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

public class WebCrawlerBenchmark {
    static class BenchmarkResult {
        int threadPoolSize;
        int depth;
        double averageTime;

        BenchmarkResult(int threadPoolSize, int depth, double averageTime) {
            this.threadPoolSize = threadPoolSize;
            this.depth = depth;
            this.averageTime = averageTime;
        }
    }

    public static void main(String[] args) {
        String startUrl = "https://www.linkedin.com/feed/";
        int[] threadPoolSizes = {2, 4, 6};
        int[] depths = {2};
        List<BenchmarkResult> results = new ArrayList<>();

        for (int threadPoolSize : threadPoolSizes) {
            for (int depth : depths) {
                WebCrawler crawler = new WebCrawler("neo4j+s://ab8b5b9b.databases.neo4j.io", "neo4j", "nwMDNiR--8ipy-ad92tZ3-LPsCZiVn7OTrz8ZTRe9zE", threadPoolSize, depth);
                double averageTime = benchmarkWebCrawler(startUrl, crawler, threadPoolSize, depth);
                results.add(new BenchmarkResult(threadPoolSize, depth, averageTime));
                crawler.close();
            }
        }

        printSummary(results);
    }

    private static double benchmarkWebCrawler(String startUrl, WebCrawler crawler, int threadPoolSize, int depth) {
        Consumer<WebCrawler> crawlFunction = c -> c.crawl(startUrl);

        Benchmark_Timer<WebCrawler> benchmarkTimer = new Benchmark_Timer<>(
                "WebCrawler (Threads: " + threadPoolSize + ", Depth: " + depth + ")",
                crawlFunction
        );

        double averageTime = benchmarkTimer.runFromSupplier(() -> crawler, 3);

        System.out.printf("Thread Pool Size: %d, Depth: %d, Average Time: %.2f ms%n", threadPoolSize, depth, averageTime);
        return averageTime;
    }

    private static void printSummary(List<BenchmarkResult> results) {
        System.out.println("\n--- Benchmark Summary ---");
        System.out.println("Threads | Depth | Avg Time (ms)");
        System.out.println("--------|-------|---------------");
        for (BenchmarkResult result : results) {
            System.out.printf("%7d | %5d | %13.2f%n", result.threadPoolSize, result.depth, result.averageTime);
        }

        BenchmarkResult fastest = results.stream().min((r1, r2) -> Double.compare(r1.averageTime, r2.averageTime)).orElse(null);
        BenchmarkResult slowest = results.stream().max((r1, r2) -> Double.compare(r1.averageTime, r2.averageTime)).orElse(null);

        if (fastest != null) {
            System.out.printf("%nFastest configuration: Threads: %d, Depth: %d, Time: %.2f ms%n", 
                fastest.threadPoolSize, fastest.depth, fastest.averageTime);
        }
        if (slowest != null) {
            System.out.printf("Slowest configuration: Threads: %d, Depth: %d, Time: %.2f ms%n", 
                slowest.threadPoolSize, slowest.depth, slowest.averageTime);
        }
    }
}
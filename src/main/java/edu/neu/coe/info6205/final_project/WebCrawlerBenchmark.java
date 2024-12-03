package edu.neu.coe.info6205.final_project;

import edu.neu.coe.info6205.util.Benchmark_Timer;
import java.util.Scanner;

public class WebCrawlerBenchmark {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter thread pool size: ");
        int threadPoolSize = scanner.nextInt();

        System.out.print("Enter depth: ");
        int depth = scanner.nextInt();

        String startUrl = "https://www.northeastern.edu/campuses/";

        double averageTime = benchmarkWebCrawler(startUrl, threadPoolSize, depth);

        System.out.printf("Thread Pool Size: %d, Depth: %d, Average Time: %.2f ms%n", threadPoolSize, depth, averageTime);
    }

    private static double benchmarkWebCrawler(String startUrl, int threadPoolSize, int depth) {
        Benchmark_Timer<String> benchmarkTimer = new Benchmark_Timer<>(
                "WebCrawler (Threads: " + threadPoolSize + ", Depth: " + depth + ")",
                s -> {
                    try (WebCrawler crawler = new WebCrawler("neo4j+s://ab8b5b9b.databases.neo4j.io", "neo4j", "nwMDNiR--8ipy-ad92tZ3-LPsCZiVn7OTrz8ZTRe9zE", threadPoolSize, depth)) {
                        crawler.crawl(s);
                    }
                }
        );

        double averageTime = benchmarkTimer.runFromSupplier(() -> startUrl, 1);

        return averageTime;
    }
}

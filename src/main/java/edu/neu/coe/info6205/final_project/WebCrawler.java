package edu.neu.coe.info6205.final_project;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {
    private static final int MAX_DEPTH = 3;
    private static final Logger logger = LogManager.getLogger(WebCrawler.class);

    // 優先級隊列，基於 heuristic 排序
    private final PriorityBlockingQueue<UrlDepthPair> queue = new PriorityBlockingQueue<>(100, Comparator.comparingInt(o -> o.priority));
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Driver neo4jDriver;

    public WebCrawler(String neo4jUri, String user, String password) {
        neo4jDriver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(user, password));
    }

    public void crawl(String startUrl) {

        clearDatabase();

        processInitialUrl(startUrl);

        ExecutorService executor = Executors.newFixedThreadPool(10); // create a thread pool with 10 threads
        AtomicInteger activeTasks = new AtomicInteger(0);

        while (true) {
            UrlDepthPair current = queue.poll();

            if (current == null) {
                // If the queue is empty, check if there are active tasks
                if (activeTasks.get() == 0) {
                    // No active tasks; safe to exit
                    break;
                } else {
                    // Wait for a short period before checking again
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while waiting", e);
                    }
                    continue;
                }
            }

            if (current.depth > MAX_DEPTH || visited.contains(current.url)) {
                continue;
            }

            visited.add(current.url);
            activeTasks.incrementAndGet(); // Increment active task count
            executor.submit(() -> {
                try {
                    processUrl(current);
                } finally {
                    activeTasks.decrementAndGet(); // Decrement active task count when done
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.error("Executor interrupted", e);
        }
        listUrlsByInDegree();
        neo4jDriver.close();
    }

    private void processInitialUrl(String startUrl) {
        try {
            System.out.println("Crawling initial URL: " + startUrl);
            Document doc = Jsoup.connect(startUrl)
                    .ignoreContentType(true)
                    .get(); // make the HTTP request and get the HTML content
            Elements links = doc.select("a[href]");
            saveUrlToGraph(startUrl, 0);
            visited.add(startUrl); // mark the URL as visited so we don't process it again

            for (Element link : links) {
                String url = link.absUrl("href");
                if (url.isEmpty() || visited.contains(url) || isJavascriptLink(url)) { // skip empty URLs, visited URLs, and JavaScript links
                    continue;
                }
                int priority = calculatePriority(url);
                queue.add(new UrlDepthPair(url, 1, priority)); // 初始深度設為1，加入計算的優先級
                saveLinkToGraph(startUrl, url);
                System.out.println("Added to queue: " + url + " (priority: " + priority + ")");
            }
        } catch (IOException e) {
            logger.error("Failed to crawl initial URL: " + startUrl, e);
        }
    }

    private void processUrl(UrlDepthPair current) {
        try {
            System.out.println("Processing URL: " + current.url + " (depth: " + current.depth + ", priority: " + current.priority + ")");
            Document doc = Jsoup.connect(current.url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(30000)
                    .get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String url = link.absUrl("href");
                if (url.isEmpty() || visited.contains(url) || isJavascriptLink(url)) {
                    continue;
                }
                int newDepth = current.depth + 1;
                if (newDepth <= MAX_DEPTH) {
                    int priority = calculatePriority(url);
                    queue.add(new UrlDepthPair(url, newDepth, priority));
                    saveLinkToGraph(current.url, url);
                    saveUrlToGraph(url, newDepth);
                    System.out.println("Added to queue: " + url + " (depth: " + newDepth + ", priority: " + priority + ")");
                }
            }
            Thread.sleep(2000);
        } catch (IOException e) {
            logger.error("Failed to crawl URL: " + current.url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int calculatePriority(String url) {
        // 設定 heuristic：優先抓取 job 相關的內容
        if (url.contains("job") || url.contains("career")) {
            return 1; // 優先級最高
        } else if (url.contains("help") || url.contains("policy")) {
            return 10; // 次要內容
        } else {
            return 5; // 默認優先級
        }
    }

    private boolean isJavascriptLink(String url) {
        return url.startsWith("javascript:");
    }


    private void saveUrlToGraph(String url, int depth) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (n:Page {url: $url}) " +
                       "ON CREATE SET n.depth = $depth, n.in_degree = 0 " +  // Set initial in_degree as 0
                       "ON MATCH SET n.depth = $depth",
                       Map.of("url", url, "depth", depth));
                System.out.println("Saved URL to graph: " + url + " with depth: " + depth);
                return null;
            });
        }
    }

    // private void saveUrlToGraph(String url, int depth) {
    //     try (Session session = neo4jDriver.session()) {
    //         session.executeWrite(tx -> {
    //             tx.run("MERGE (n:Page {url: $url}) " +
    //                             "ON MATCH SET n.depth = $depth " +
    //                             "ON CREATE SET n.depth = $depth",
    //                     Map.of("url", url, "depth", depth));
    //             System.out.println("Saved URL to graph: " + url + " with depth: " + depth);
    //             return null;
    //         });
    //     }
    // }

    private void saveLinkToGraph(String fromUrl, String toUrl) {
    try (Session session = neo4jDriver.session()) {
        session.executeWrite(tx -> {
            // Create the relationship between fromUrl and toUrl
            tx.run("MERGE (a:Page {url: $fromUrl}) " +
                   "MERGE (b:Page {url: $toUrl}) " +
                   "MERGE (a)-[:LINKS_TO]->(b)",
                   Map.of("fromUrl", fromUrl, "toUrl", toUrl));

            // Increment in_degree for the target node (toUrl)
            tx.run("MATCH (b:Page {url: $toUrl}) " +
                   "SET b.in_degree = COALESCE(b.in_degree, 0) + 1",
                   Map.of("toUrl", toUrl));

            return null;
        });
    }
}

    private void listUrlsByInDegree() {
        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                // Query to list URLs by in-degree
                String query = "MATCH (p:Page) " +
                               "RETURN p.url AS url, coalesce(p.in_degree, 0) AS in_degree " +
                               "ORDER BY in_degree DESC";
                var result = tx.run(query);
                while (result.hasNext()) {
                    var record = result.next();
                    String url = record.get("url").asString();
                    int inDegree = record.get("in_degree").asInt();
                    System.out.println("URL: " + url + " | In-degree: " + inDegree);
                }
                return null;
            });
        }
    }

    // private void saveLinkToGraph(String fromUrl, String toUrl) {
    //     try (Session session = neo4jDriver.session()) {
    //         session.executeWrite(tx -> {
    //             tx.run("MERGE (a:Page {url: $fromUrl}) " +
    //                             "MERGE (b:Page {url: $toUrl}) " +
    //                             "MERGE (a)-[:LINKS_TO]->(b)",
    //                     Map.of("fromUrl", fromUrl, "toUrl", toUrl));
    //             return null;
    //         });
    //     }
    // }

    private static class UrlDepthPair {
        String url;
        int depth;
        int priority; // 新增優先級屬性

        UrlDepthPair(String url, int depth, int priority) {
            this.url = url;
            this.depth = depth;
            this.priority = priority;
        }
    }

    private void clearDatabase() {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the starting URL: ");
        String startUrl = scanner.nextLine();
        scanner.close();

        WebCrawler crawler = new WebCrawler("neo4j+s://ab8b5b9b.databases.neo4j.io", "neo4j", "nwMDNiR--8ipy-ad92tZ3-LPsCZiVn7OTrz8ZTRe9zE");
        crawler.crawl(startUrl);
    }
}
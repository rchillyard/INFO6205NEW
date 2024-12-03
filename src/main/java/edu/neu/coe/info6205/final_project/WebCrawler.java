package edu.neu.coe.info6205.final_project;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.neo4j.driver.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements AutoCloseable {
    private final int maxDepth;
    private final int threadPoolSize;
    private static final Logger logger = LogManager.getLogger(WebCrawler.class);

    // Priority queue sorted based on heuristic (priority)
    private final PriorityBlockingQueue<UrlDepthPair> queue = new PriorityBlockingQueue<>(100, Comparator.comparingInt(o -> o.priority));
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Driver neo4jDriver;

    public WebCrawler(String neo4jUri, String user, String password, int threadPoolSize, int maxDepth) {
        neo4jDriver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(user, password));
        this.threadPoolSize = threadPoolSize;
        this.maxDepth = maxDepth;
    }

    public void crawl(String startUrl) {

        clearDatabase();

        processInitialUrl(startUrl);

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize); // Use the specified thread pool size
        AtomicInteger activeTasks = new AtomicInteger(0);

        while (true) {
            UrlDepthPair current = queue.poll();

            if (current == null) {
                // If the queue is empty, check for active tasks
                if (activeTasks.get() == 0) {
                    // No active tasks, safe to exit
                    break;
                } else {
                    // Wait for a while and check again
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while waiting", e);
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
            }

            if (current.depth > maxDepth || visited.contains(current.url)) {
                continue;
            }

            visited.add(current.url);
            activeTasks.incrementAndGet(); // Increase active task count
            executor.submit(() -> {
                try {
                    processUrl(current);
                } finally {
                    activeTasks.decrementAndGet(); // Decrease active task count after completion
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.error("Executor interrupted", e);
            Thread.currentThread().interrupt();
        }

        listUrlsByInDegree(); // List all URLs sorted by in-degree
    }

    private void processInitialUrl(String startUrl) {
        try {
            String normalizedUrl = normalizeUrl(startUrl);
            if (normalizedUrl == null) {
                logger.error("Normalized URL is null: " + startUrl);
                return;
            }
            System.out.println("Crawling initial URL: " + normalizedUrl);
            Document doc = Jsoup.connect(normalizedUrl)
                    .ignoreContentType(true)
                    .get(); // Send HTTP request and fetch HTML content
            Elements links = doc.select("a[href]");
            saveUrlToGraph(normalizedUrl, 0);
            visited.add(normalizedUrl); // Mark URL as visited
            logger.info("Saved initial URL to database: " + normalizedUrl);

            for (Element link : links) {
                String url = link.absUrl("href");
                if (isUnsupportedLink(url)) {
                    continue;
                }
                url = normalizeUrl(url);
                if (url == null || visited.contains(url)) {
                    continue;
                }
                int priority = calculatePriority(url);
                queue.add(new UrlDepthPair(url, 1, priority)); // Set initial depth to 1 and add with calculated priority
                saveUrlToGraph(url, 1); // Record URL with depth 1
                saveLinkToGraph(normalizedUrl, url);
                System.out.println("Added to queue: " + url + " (priority: " + priority + ")");
            }
        } catch (IOException e) {
            logger.error("Failed to crawl initial URL: " + startUrl, e);
        }
    }

    private void processUrl(UrlDepthPair current) {
        try {
            String normalizedUrl = normalizeUrl(current.url);
            if (normalizedUrl == null) {
                return;
            }
            System.out.println("Processing URL: " + normalizedUrl + " (depth: " + current.depth + ", priority: " + current.priority + ")");
            Document doc = Jsoup.connect(normalizedUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(30000)
                    .get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String url = link.absUrl("href");
                if (isUnsupportedLink(url)) {
                    continue;
                }
                url = normalizeUrl(url);
                if (url == null || visited.contains(url)) {
                    continue;
                }
                int newDepth = current.depth + 1;
                if (newDepth <= maxDepth) {
                    int priority = calculatePriority(url);
                    queue.add(new UrlDepthPair(url, newDepth, priority));
                    saveUrlToGraph(url, newDepth);
                    saveLinkToGraph(normalizedUrl, url);
                    //System.out.println("Added to queue: " + url + " (depth: " + newDepth + ", priority: " + priority + ")");
                }
            }
            Thread.sleep(2000);
        } catch (IOException e) {
            logger.error("Failed to crawl URL: " + current.url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int calculatePriority(String url) {
        // Set heuristic: prioritize content related to graduate and Boston
        if (url.contains("graduate") && url.toLowerCase().contains("boston")) {
            return 1; // Highest priority
        } else if (url.contains("admission") && url.toLowerCase().contains("boston")) {
            return 2; // High priority
        } else if (url.contains("help") || url.contains("policy")) {
            return 10; // Secondary content
        } else {
            return 5; // Default priority
        }
    }

    public boolean isUnsupportedLink(String url) {
        return isJavascriptLink(url) || isImageLink(url) || isInvalidScheme(url) || url.contains("#");
    }

    private boolean isJavascriptLink(String url) {
        return url.startsWith("javascript:");
    }

    // New method: check if the link is an image link
    public boolean isImageLink(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".png") || lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")
                || lowerUrl.endsWith(".gif") || lowerUrl.endsWith(".bmp") || lowerUrl.endsWith(".svg")
                || lowerUrl.endsWith(".webp") || lowerUrl.endsWith(".tiff") || lowerUrl.endsWith(".ico");
    }

    // New method: check if the scheme is unsupported
    private boolean isInvalidScheme(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.startsWith("mailto:") || lowerUrl.startsWith("tel:") || lowerUrl.startsWith("sms:")
                || lowerUrl.startsWith("ftp:") || lowerUrl.startsWith("file:");
    }

    // Modified method: normalize the URL with exception handling
    public String normalizeUrl(String url) {
        try {
            URI uri = new URI(url);
            // Normalize the URI
            uri = uri.normalize();
            // Construct the normalized URI, remove default ports, handle case sensitivity, etc.
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "http";
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return null; // Handle only http and https protocols
            }
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            int port = uri.getPort();
            String path = uri.getPath() != null ? uri.getPath() : "";
            // Retain the slash for root paths
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1); // Remove trailing slash
            }
            String query = uri.getQuery();
            String fragment = null; // Exclude the fragment

            URI normalizedUri = new URI(scheme, null, host, port, path, query, fragment);
            return normalizedUri.toString();
        } catch (URISyntaxException e) {
            // Skip URLs that cannot be parsed
            return null;
        }
    }

    private void saveUrlToGraph(String url, int depth) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (n:Page {url: $url}) " +
                        "ON CREATE SET n.depth = $depth, n.in_degree = 0 " +
                        "ON MATCH SET n.depth = CASE WHEN n.depth > $depth THEN $depth ELSE n.depth END",
                        Map.of("url", url, "depth", depth));
                //System.out.println("Saved URL to graph: " + url + " with depth: " + depth);
                return null;
            });
        }
    }

    private void saveLinkToGraph(String fromUrl, String toUrl) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (a:Page {url: $fromUrl}) " +
                        "MERGE (b:Page {url: $toUrl}) " +
                        "MERGE (a)-[:LINKS_TO]->(b) " +
                        "SET b.in_degree = coalesce(b.in_degree, 0) + 1",
                        Map.of("fromUrl", fromUrl, "toUrl", toUrl));
                return null;
            });
        }
    }

    private void listUrlsByInDegree() {
        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                // Query URLs sorted by in-degree
                String query = "MATCH (p:Page) " +
                        "RETURN p.url AS url, coalesce(p.in_degree, 0) AS in_degree " +
                        "ORDER BY in_degree DESC";
                var result = tx.run(query);
                System.out.println("\nURLs ordered by In-degree:");
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

    private static class UrlDepthPair {
        String url;
        int depth;
        int priority; // Priority attribute

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
            logger.info("Database cleared successfully.");
        }
    }

    @Override
    public void close() {
        if (neo4jDriver != null) {
            neo4jDriver.close();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the starting URL: ");
        String startUrl = scanner.nextLine();
        scanner.close();

        // Set default thread pool size and max depth
        int threadPoolSize = 10;
        int maxDepth = 3;
        
        try (WebCrawler crawler = new WebCrawler("neo4j+s://ab8b5b9b.databases.neo4j.io", "neo4j", "nwMDNiR--8ipy-ad92tZ3-LPsCZiVn7OTrz8ZTRe9zE", threadPoolSize, maxDepth)) {
            crawler.crawl(startUrl);
        }
    }
}

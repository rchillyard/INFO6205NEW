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
import java.util.regex.Pattern;

public class WebCrawler {
    private static final int MAX_DEPTH = 5;
    private static final Pattern ORG_EDU_REGEX = Pattern.compile(".*(\\.org|\\.edu).*");
    private static final Logger logger = LogManager.getLogger(WebCrawler.class);

    private final PriorityBlockingQueue<UrlDepthPair> queue = new PriorityBlockingQueue<>(100, Comparator.comparingInt(o -> o.depth));
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Driver neo4jDriver;

    public WebCrawler(String neo4jUri, String user, String password) {
        neo4jDriver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(user, password));
    }

    public void crawl(String startUrl) {
        processInitialUrl(startUrl);

        ExecutorService executor = Executors.newFixedThreadPool(10); // create a thread pool with 10 threads

        while (!queue.isEmpty()) {
            UrlDepthPair current = queue.poll();

            if (current == null || current.depth > MAX_DEPTH || visited.contains(current.url)) {
                continue;
            }

            visited.add(current.url);
            CompletableFuture.runAsync(() -> processUrl(current), executor).exceptionally(ex -> { // process the URL asynchronously
                logger.error("Error processing URL: " + current.url, ex);
                return null;
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.error("Executor interrupted", e);
        }

        neo4jDriver.close();
    }

    private void processInitialUrl(String startUrl) {
        try {
            System.out.println("Crawling initial URL: " + startUrl);
            Document doc = Jsoup.connect(startUrl)
                                .ignoreContentType(true)
                                .get(); // make the HTTP request and get the HTML content
            Elements links = doc.select("a[href]");
            saveUrlToGraph(startUrl);
            visited.add(startUrl); // mark the URL as visited so we don't process it again
    
            for (Element link : links) {
                String url = link.absUrl("href");
                if (url.isEmpty() || visited.contains(url) || isJavascriptLink(url)) { // skip empty URLs, visited URLs, and JavaScript links
                    continue;
                }
    
                int depth = ORG_EDU_REGEX.matcher(url).matches() ? 1 : 2; // if the URL is an .org or .edu domain, set the depth to 1, otherwise set it to 2
                queue.add(new UrlDepthPair(url, depth)); // add the URL and its depth to the queue
                saveLinkToGraph(startUrl, url); // connect the start URL to the new URL in the graph
                System.out.println("Added to queue: " + url + " (depth: " + depth + ")");
            }
        } catch (IOException e) {
            logger.error("Failed to crawl initial URL: " + startUrl, e);
        }
    }

    private void processUrl(UrlDepthPair current) {
        try {
            System.out.println("Processing URL: " + current.url + " (depth: " + current.depth + ")");
            Document doc = Jsoup.connect(current.url)
                                .ignoreContentType(true)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                .referrer("http://www.google.com")
                                .timeout(10000)
                                .get();
            Elements links = doc.select("a[href]");
    
            for (Element link : links) {
                String url = link.absUrl("href");
                if (url.isEmpty() || visited.contains(url) || isJavascriptLink(url)) {
                    continue;
                }
    
                int newDepth = ORG_EDU_REGEX.matcher(url).matches() ? current.depth + 1 : current.depth + 2;
                if (newDepth <= MAX_DEPTH) {
                    queue.add(new UrlDepthPair(url, newDepth)); 
                    saveLinkToGraph(current.url, url);
                    System.out.println("Added to queue: " + url + " (depth: " + newDepth + ")");
                }
            }
            Thread.sleep(1000);
        } catch (IOException e) {
            if (e instanceof org.jsoup.HttpStatusException) {
                org.jsoup.HttpStatusException httpError = (org.jsoup.HttpStatusException) e;
                int statusCode = httpError.getStatusCode();
                if (statusCode == 400 || statusCode == 999) {
                    logger.warn("Access denied or rate limited for URL: " + current.url + " (Status: " + statusCode + ")");
                } else {
                    logger.error("HTTP error fetching URL: " + current.url + " (Status: " + statusCode + ")", e);
                }
            } else {
                logger.error("Failed to crawl URL: " + current.url, e);
            }
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while processing URL: " + current.url, e);
            Thread.currentThread().interrupt();
        }
    }

    private boolean isJavascriptLink(String url) {
        return url.startsWith("javascript:");
    }

    private void saveUrlToGraph(String url) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (n:Page {url: $url})", Map.of("url", url));
                System.out.println("Saved URL to graph: " + url);
                return null;
            });
        }
    }

    private void saveLinkToGraph(String fromUrl, String toUrl) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (a:Page {url: $fromUrl}) " +
                       "MERGE (b:Page {url: $toUrl}) " +
                       "MERGE (a)-[:LINKS_TO]->(b)", 
                       Map.of("fromUrl", fromUrl, "toUrl", toUrl));
                System.out.println("Saved link to graph: " + fromUrl + " -> " + toUrl);
                return null;
            });
        }
    }

    private static class UrlDepthPair {
        String url;
        int depth;

        UrlDepthPair(String url, int depth) {
            this.url = url;
            this.depth = depth;
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
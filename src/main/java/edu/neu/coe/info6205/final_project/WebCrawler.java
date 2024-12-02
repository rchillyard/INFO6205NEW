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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements AutoCloseable {
    private static final int MAX_DEPTH = 3;

    private static final Logger logger = LogManager.getLogger(WebCrawler.class);

    // 优先级队列，基于 heuristic 排序
    private final PriorityBlockingQueue<UrlDepthPair> queue = new PriorityBlockingQueue<>(100, Comparator.comparingInt(o -> o.priority));
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Driver neo4jDriver;

    public WebCrawler(String neo4jUri, String user, String password) {
        neo4jDriver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(user, password));
    }

    public void crawl(String startUrl) {

        clearDatabase();

        processInitialUrl(startUrl);

        ExecutorService executor = Executors.newFixedThreadPool(10); // 创建具有 10 个线程的线程池
        AtomicInteger activeTasks = new AtomicInteger(0);

        while (true) {
            UrlDepthPair current = queue.poll();

            if (current == null) {
                // 如果队列为空，检查是否有活跃的任务
                if (activeTasks.get() == 0) {
                    // 没有活跃任务，可以安全退出
                    break;
                } else {
                    // 等待一段时间后再次检查
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
            activeTasks.incrementAndGet(); // 增加活跃任务计数
            executor.submit(() -> {
                try {
                    processUrl(current);
                } finally {
                    activeTasks.decrementAndGet(); // 任务完成后减少活跃任务计数
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
            String normalizedUrl = normalizeUrl(startUrl);
            System.out.println("Crawling initial URL: " + normalizedUrl);
            Document doc = Jsoup.connect(normalizedUrl)
                    .ignoreContentType(true)
                    .get(); // 发起 HTTP 请求并获取 HTML 内容
            Elements links = doc.select("a[href]");
            saveUrlToGraph(normalizedUrl, 0);
            visited.add(normalizedUrl); // 标记 URL 已访问

            for (Element link : links) {
                String url = link.absUrl("href");
                url = normalizeUrl(url);
                if (url.isEmpty() || visited.contains(url) || isJavascriptLink(url)) { // 跳过空链接、已访问链接和 JavaScript 链接
                    continue;
                }
                int priority = calculatePriority(url);
                queue.add(new UrlDepthPair(url, 1, priority)); // 初始深度设为1，加入计算的优先级
                saveUrlToGraph(url, 1); // 记录深度为 1 的 URL
                saveLinkToGraph(normalizedUrl, url);
                System.out.println("Added to queue: " + url + " (priority: " + priority + ")");
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("Failed to crawl initial URL: " + startUrl, e);
        }
    }

    private void processUrl(UrlDepthPair current) {
        try {
            String normalizedUrl = normalizeUrl(current.url);
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
                url = normalizeUrl(url);
                if (url.isEmpty() || visited.contains(url) || isJavascriptLink(url)) {
                    continue;
                }
                int newDepth = current.depth + 1;
                if (newDepth <= MAX_DEPTH) {
                    int priority = calculatePriority(url);
                    queue.add(new UrlDepthPair(url, newDepth, priority));
                    saveUrlToGraph(url, newDepth);
                    saveLinkToGraph(normalizedUrl, url);
                    System.out.println("Added to queue: " + url + " (depth: " + newDepth + ", priority: " + priority + ")");
                }
            }
            Thread.sleep(2000);
        } catch (IOException | URISyntaxException e) {
            logger.error("Failed to crawl URL: " + current.url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int calculatePriority(String url) {
        // 设置启发式：优先抓取 edu 和 graduate admission 相关的内容
        if (url.contains(".edu") && url.toLowerCase().contains("graduate")) {
            return 1; // 优先级最高
        } else if (url.contains("admission") && url.toLowerCase().contains("boston")) {
            return 2; // 高优先级
        } else if (url.contains("help") || url.contains("policy")) {
            return 10; // 次要内容
        } else {
            return 5; // 默认优先级
        }
    }

    private boolean isJavascriptLink(String url) {
        return url.startsWith("javascript:");
    }

    // 新增方法：规范化 URL
    private String normalizeUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);
        // 对 URI 进行规范化处理
        uri = uri.normalize();
        // 构建规范化的 URI，移除默认端口，处理大小写等
        String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "http";
        String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
        int port = uri.getPort();
        String path = uri.getPath() != null ? uri.getPath() : "";
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1); // 移除末尾的斜杠
        }
        String query = uri.getQuery();
        String fragment = uri.getFragment();

        URI normalizedUri = new URI(scheme, null, host, port, path, query, fragment);
        return normalizedUri.toString();
    }

    private void saveUrlToGraph(String url, int depth) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (n:Page {url: $url}) " +
                        "ON CREATE SET n.depth = $depth, n.in_degree = coalesce(n.in_degree, 0) " +
                        "ON MATCH SET n.depth = CASE WHEN n.depth > $depth THEN $depth ELSE n.depth END", // 更新 depth 为更小的值
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
            session.executeWrite(tx -> {
                tx.run("MERGE (a:Page {url: $fromUrl}) " +
                        "MERGE (b:Page {url: $toUrl}) " +
                        "MERGE (a)-[:LINKS_TO]->(b) " +
                        "SET b.in_degree = coalesce(b.in_degree, 0) + 1", // 增加 b 的 in_degree
                        Map.of("fromUrl", fromUrl, "toUrl", toUrl));

                return null;
            });
        }
    }


    private void listUrlsByInDegree() {
        try (Session session = neo4jDriver.session()) {
            session.executeRead(tx -> {
                // 查询按入度列出 URL
                String query = "MATCH (p:Page) " +
                               "RETURN p.url AS url, p.in_degree AS in_degree " +
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
        int priority; // 优先级属性

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

        try (WebCrawler crawler = new WebCrawler("neo4j+s://ab8b5b9b.databases.neo4j.io", "neo4j", "nwMDNiR--8ipy-ad92tZ3-LPsCZiVn7OTrz8ZTRe9zE")) {
            crawler.crawl(startUrl);
        }
    }
}

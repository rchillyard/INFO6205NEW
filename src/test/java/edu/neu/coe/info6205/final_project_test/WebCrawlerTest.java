package edu.neu.coe.info6205.final_project_test;

import org.junit.jupiter.api.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import edu.neu.coe.info6205.final_project.WebCrawler;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebCrawlerTest {

    private static final String NEO4J_URI = "neo4j+s://ab8b5b9b.databases.neo4j.io";
    private static final String NEO4J_USER = "neo4j";
    private static final String NEO4J_PASSWORD = "nwMDNiR--8ipy-ad92tZ3-LPsCZiVn7OTrz8ZTRe9zE"; // Replace with your actual password

    private static final String START_URL = "https://www.northeastern.edu/campuses/";

    private static Driver neo4jDriver;

    @BeforeAll
    static void setUpNeo4j() {
        neo4jDriver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
    }

    @AfterAll
    static void closeNeo4j() {
        if (neo4jDriver != null) {
            neo4jDriver.close();
        }
    }

    @Test
    @Order(1)
    void testCrawl() {
        int threadPoolSize = 4;
        int maxDepth = 1;

        try (WebCrawler crawler = new WebCrawler(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD, threadPoolSize, maxDepth)) {
            crawler.crawl(START_URL);
        }

        // Verify that the expected nodes and relationships exist in the database
        try (Session session = neo4jDriver.session()) {
            // Check if the starting URL node exists
            boolean startUrlExists = session.run("MATCH (n:Page {url: $url}) RETURN n", Map.of("url", START_URL))
                    .hasNext();
            assertTrue(startUrlExists, "The starting URL node should exist in the database");

            // Check if there are nodes with the specified depth
            List<Record> depthNodes = session.run("MATCH (n:Page) WHERE n.depth <= $maxDepth RETURN n",
                    Map.of("maxDepth", maxDepth)).list();
            assertFalse(depthNodes.isEmpty(), "There should be nodes with depth not exceeding " + maxDepth);

            // Check if there are nodes with an in-degree attribute
            List<Record> nodesWithInDegree = session.run("MATCH (n:Page) WHERE exists(n.in_degree) RETURN n").list();
            assertFalse(nodesWithInDegree.isEmpty(), "There should be nodes with an in_degree attribute");
        }
    }

    @Test
    @Order(2)
    void testNormalizeUrl() throws URISyntaxException {
        WebCrawler crawler = new WebCrawler(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD, 1, 1);
        String url = "https://student.me.northeastern.edu/";
        String expected = "https://student.me.northeastern.edu/";
        String normalized = crawler.normalizeUrl(url);
        assertEquals(expected, normalized, "URL normalization should handle case sensitivity and trailing slashes correctly");
        crawler.close();
    }

    @Test
    @Order(3)
    void testIsImageLink() {
        WebCrawler crawler = new WebCrawler(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD, 1, 1);
        assertTrue(crawler.isImageLink("http://example.com/image.png"));
        assertTrue(crawler.isImageLink("http://example.com/photo.JPG"));
        assertFalse(crawler.isImageLink("http://example.com/index.html"));
        crawler.close();
    }

    @Test
    @Order(4)
    void testIsUnsupportedLink() {
        WebCrawler crawler = new WebCrawler(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD, 1, 1);
        assertTrue(crawler.isUnsupportedLink("javascript:void(0);"));
        assertTrue(crawler.isUnsupportedLink("mailto:someone@example.com"));
        assertTrue(crawler.isUnsupportedLink("tel:+123456789"));
        assertFalse(crawler.isUnsupportedLink("http://example.com/page"));
        crawler.close();
    }

    @Test
    @Order(5)
    void testCalculatePriority() {
        WebCrawler crawler = new WebCrawler(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD, 1, 1);
        assertEquals(1, crawler.calculatePriority("http://example.com/graduate-programs-boston"));
        assertEquals(2, crawler.calculatePriority("http://example.com/admission-boston"));
        assertEquals(10, crawler.calculatePriority("http://example.com/privacy-policy"));
        assertEquals(5, crawler.calculatePriority("http://example.com/other-page"));
        crawler.close();
    }

    @Test
    @Order(6)
    void testDatabaseInteractions() {
        // Clear the database
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }

        // Create a WebCrawler instance and crawl a simple page
        String testUrl = "https://www.mfa.org/";
        int threadPoolSize = 2;
        int maxDepth = 1;

        try (WebCrawler crawler = new WebCrawler(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD, threadPoolSize, maxDepth)) {
            crawler.crawl(testUrl);
        }

        // Verify that nodes and relationships exist in the database
        try (Session session = neo4jDriver.session()) {
            // Check the number of nodes
            long nodeCount = session.run("MATCH (n:Page) RETURN count(n) AS count")
                    .single().get("count").asLong();
            assertTrue(nodeCount > 0, "There should be nodes in the database");

            // Check the number of relationships
            long relCount = session.run("MATCH ()-[r:LINKS_TO]->() RETURN count(r) AS count")
                    .single().get("count").asLong();
            assertTrue(relCount > 0, "There should be relationships in the database");
        }
    }

    @Test
    @Order(7)
    void testMaxDepthLimit() {
        // Clear the database
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }

        String testUrl = "https://www.northeastern.edu/";
        int threadPoolSize = 2;
        int maxDepth = 1;

        try (WebCrawler crawler = new WebCrawler(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD, threadPoolSize, maxDepth)) {
            crawler.crawl(testUrl);
        }

        // Verify that no nodes exceed the maximum depth
        try (Session session = neo4jDriver.session()) {
            List<Record> nodesExceedingDepth = session.run("MATCH (n:Page) WHERE n.depth > $maxDepth RETURN n",
                    Map.of("maxDepth", maxDepth)).list();
            assertTrue(nodesExceedingDepth.isEmpty(), "There should be no nodes with depth exceeding " + maxDepth);
        }
    }
}

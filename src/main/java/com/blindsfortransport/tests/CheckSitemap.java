package com.blindsfortransport.tests;

import com.blindsfortransport.tests.config.AppConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class CheckSitemap {
    private static final Logger logger = LoggerFactory.getLogger(CheckSitemap.class);
    private static final String baseUrl = AppConfig.baseUrl;

    /*
    public static void main(String [] args) {
        String sitemapIndexUrl = AppConfig.sitemapUrl;
        logger.info("SitemapIndexUrl: {}", sitemapIndexUrl);
        Set<String> allUrls = new HashSet<>();

        allUrls = extractUrlsFromSitemap(sitemapIndexUrl);
    }

     */

    public static Set<String> extractUrlsFromSitemap(String url) {
        String sitemapSelector = "sitemap > loc";
        String urlSelector = "url > loc";
        Set<String> extractedUrls = new HashSet<>();

        Elements sitemapLinks = extractTags(url, sitemapSelector);

        if (sitemapLinks.isEmpty()) {
            //Elements pageLinks = extractTags(url, urlSelector);
            extractedUrls = extractUrl(extractTags(url, urlSelector));
        } else {
            for (var sitemap : sitemapLinks) {
                String sitemapUrl = sitemap.text();
                extractedUrls.addAll(extractUrl(extractTags(sitemapUrl, urlSelector)));
            }
        }
        return extractedUrls;
    }

    public static Set<String> readUrlsFromCsv(String filePath) {
        Set<String> urls = new HashSet<>();
        Path path = Paths.get(filePath);

        try {
            if (!Files.exists(path)) {
                logger.warn("File {} does not exist, creating it with header.", filePath);
                Files.createFile(path);
                Files.writeString(path, "url\n");
            }

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                boolean isFirst = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirst) {
                        isFirst = false;
                        continue;
                    }
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        urls.add(trimmed);
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Error handling CSV file {}: {}", path, e.getMessage());
        }
        return urls;
    }

    public static void appendUrlsToCsv(String filePath, Set<String> newUrls) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String url : newUrls) {
                writer.write(url);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Can not write new urls {} in {} file", newUrls, filePath);
        }
    }

    private static Elements extractTags(String url, String selector) {
        Elements elements = new Elements();
        try {
            Document document = Jsoup
                    .connect(url)
                    .timeout(10000)
                    .get();
            elements = document.select(selector);
            logger.info("Found {} elements for selector '{}' on {}", elements.size(), selector, url);
        } catch (IOException e) {
            logger.error("Error fetching content from {} with selector '{}': {}", url, selector, e.getMessage());
        }
        return elements;
    }

    private static Set<String> extractUrl(Elements pageLinks) {
        Set<String> urls = new HashSet<>();
        for (var page : pageLinks) {
            String url = page.text();
            if ( url.startsWith(baseUrl)) {
                logger.info("Add Url: {}", url);
                urls.add(url);
            } else {
                logger.warn("Skipped URL (doesn't match baseUrl {}): {}",baseUrl, url);
            }
        }
        return urls;
    }
}

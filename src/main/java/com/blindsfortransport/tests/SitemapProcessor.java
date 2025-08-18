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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SitemapProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SitemapProcessor.class);
    private static final String baseUrl = AppConfig.baseUrl;

    public static Set<String> extractUrlsFromSitemap(String url) {
        String sitemapSelector = "sitemap > loc";
        String urlSelector = "url > loc";
        Set<String> extractedUrls = new HashSet<>();

        Elements sitemapLinks = extractTags(url, sitemapSelector);

        if (sitemapLinks.isEmpty()) {
            extractedUrls = extractUrl(extractTags(url, urlSelector));
        } else {
            for (var sitemap : sitemapLinks) {
                String sitemapUrl = sitemap.text();
                extractedUrls.addAll(extractUrl(extractTags(sitemapUrl, urlSelector)));
            }
        }
        return extractedUrls;
    }

    public static Set<String> readLinesFromCsv(String filePath, String fileLine) {
        Set<String> urls = new HashSet<>();
        Path path = Paths.get(filePath);

        try {
            if (!Files.exists(path)) {
                logger.warn("File {} does not exist, creating it with header.", filePath);
                Files.createFile(path);
                Files.writeString(path, fileLine + System.lineSeparator());
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

    public static void appendUrlsToCsv(String urlsFilePath, String urlsAndStatusFilePath, Set<String> newUrls) {

           try (BufferedWriter urlsWriter = new BufferedWriter(new FileWriter(urlsFilePath, true));
                BufferedWriter statusWriter = new BufferedWriter(new FileWriter(urlsAndStatusFilePath, true))
           ) {
               for (String url : newUrls) {
                   urlsWriter.write(url+ System.lineSeparator());

                   statusWriter.write(url + ",200" + System.lineSeparator());
               }
        } catch (IOException e) {
            logger.error("Failed to write new URLs to files {} and {}. Error:", urlsFilePath, urlsAndStatusFilePath, e);
        }
           deleteEmptyLine(urlsFilePath);
           deleteEmptyLine(urlsAndStatusFilePath);
    }

    public enum ResourceType {
        CSS, JS, IMAGE, MEDIA, ALL
    }

    public static Set<String> extractResources(String pageUrl, ResourceType type) {
        Set<String> resources = new HashSet<>();

        try {
            Document document = Jsoup.connect(pageUrl).timeout(5000).get();

            if (type == ResourceType.CSS || type == ResourceType.ALL) {
                document.select("link[rel=stylesheet]").forEach(element -> resources.add(element.absUrl("href")));
            }
            if (type == ResourceType.JS || type == ResourceType.ALL) {
                document.select("script[src]").forEach(element -> resources.add(element.absUrl("src")));
            }
            if (type == ResourceType.IMAGE || type == ResourceType.ALL) {
                document.select("img[src]").forEach(element -> resources.add(element.absUrl("src")));
            }
            if (type == ResourceType.MEDIA || type == ResourceType.ALL) {
                document.select("source[src], video[src], audio[src]").forEach(element -> resources.add(element.absUrl("src")));
            }
        } catch (IOException e) {
            logger.error("Can not extract {} resources for page: {}", type, pageUrl);
        }
        return resources;
    }

    private static void deleteEmptyLine(String filePath) {
        Path path = Paths.get(filePath);
        try {
            List<String> lines = Files.readAllLines(path).stream()
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());
            Files.write(path, lines);
        } catch (IOException e) {
            logger.error("Can not read or write file: {}", filePath, e);
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

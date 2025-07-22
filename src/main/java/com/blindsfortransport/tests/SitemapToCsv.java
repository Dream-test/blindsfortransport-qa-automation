package com.blindsfortransport.tests;

import com.blindsfortransport.tests.config.AppConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SitemapToCsv {
    private static final Logger logger = LoggerFactory.getLogger(SitemapToCsv.class);
    private static final String baseUrl = AppConfig.baseUrl;

    public static void main(String[] args) {
        String sitemapIndexUrl = AppConfig.sitemapUrl;
        logger.info("SitemapIndexUrl: {}", sitemapIndexUrl);
        Set<String> allUrls = new HashSet<>();

        try { 
            // try-catch to handle IOException at the top level
            // Download all nested sitemap's
            Elements sitemapLinks = Jsoup
                                    .connect(sitemapIndexUrl)
                                    .get()
                                    .select("sitemap > loc");

            allUrls = processElements(sitemapLinks);
            writeToCsv(allUrls);

            logger.info("Collected {} unique URLs from sitemap(s).", allUrls.size());
            logger.info("""
                    CSV files written:
                    - {}
                    - {}
                    Total URLs: {}
                    """,
                    AppConfig.URLS_CSV_PATH,
                    AppConfig.URLS_WITH_STATUS_CSV_PATH,
                    allUrls.size());
        } catch (IOException e) {
            logger.error("Error downloading or parsing sitemap index {}: {}", sitemapIndexUrl, e.getMessage());
        }
    }

    private static Set<String> processElements(Elements sitemapLinks) {
        // Handle the case where sitemapIndexUrl might be a direct sitemap, not an index
        if (sitemapLinks.isEmpty()) {
            logger.warn("No nested sitemaps found in sitemap_index.xml using selector 'sitemap > loc'. Attempting to parse as a direct sitemap.");
            logger.info("Processing nested SitemapIndex how Sitemap Url: {}", sitemapIndexUrl);

            return extractUrls(indexDoc, allUrls);
        }
        
        return processSitemapLinks(sitemapLinks);
    }

    private static Set<String> processSitemapLinks(Elements sitemapLinks) {
        for (var sitemap : sitemapLinks) {
            String sitemapUrl = sitemap.text();
            logger.info("Processing nested Sitemap Url: {}", sitemapUrl);

            try { // try-catch to handle errors when loading nested sitemaps
                Document sitemapDoc = Jsoup.connect(sitemapUrl).get();

                return extractUrls(sitemapDoc, allUrls);
            } catch (IOException e) {
                logger.error("Error downloading or parsing sitemap {}: {}", sitemapUrl, e.getMessage());
            }
        }
    }

    private static Set<String>  extractUrls(Document sitemapDoc) {
        Elements pageLinks = sitemapDoc.select("url > loc");
        Set<String> allUrls = new HashSet<>();

        for (var page : pageLinks) {
            String url = page.text();
            // check for the domain to make sure they are the right URLs
            if (url.startsWith(baseUrl)) {
                logger.info("Add Url: {}", url);
                allUrls.add(url);
            }
        }

        return allUrls;
    }

    private void writeToCsv(Set<String> allUrls) {
        try (FileWriter urlWriter = new FileWriter(AppConfig.URLS_CSV_PATH);
            FileWriter urlAndStatusWriter = new FileWriter(AppConfig.URLS_WITH_STATUS_CSV_PATH)) {
                urlWriter.write("url\n");
                urlAndStatusWriter.write("url,expected_status\n");

                if (allUrls.isEmpty()) {
                    logger.warn("No URLs were extracted from the sitemap. Files will still be created but will be empty.");
                    return;
                }
                
                for (String url : allUrls) {
                    urlWriter.write(url + "\n");
                    urlAndStatusWriter.write(url + ",200\n");
                }
            }
    }
}
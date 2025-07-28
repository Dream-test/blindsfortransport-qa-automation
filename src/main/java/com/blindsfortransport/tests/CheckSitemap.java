package com.blindsfortransport.tests;

import com.blindsfortransport.tests.config.AppConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CheckSitemap {
    private static final Logger logger = LoggerFactory.getLogger(CheckSitemap.class);
    private static final String baseUrl = AppConfig.baseUrl;

    public static void main(String [] args) {
        String sitemapIndexUrl = AppConfig.sitemapUrl;
        logger.info("SitemapIndexUrl: {}", sitemapIndexUrl);
        Set<String> allUrls = new HashSet<>();

        allUrls = extractUrlsFromSitemap(sitemapIndexUrl);
    }

    public static Set<String> extractUrlsFromSitemap(String url) {
        String sitemapSelector = "sitemap > loc";
        String urlSelector = "url > loc";
        Set<String> extractedUrls = new HashSet<>();

        Elements sitemapLinks = extractTags(url, sitemapSelector);

        if (sitemapLinks.isEmpty()) {
            Elements pageLinks = extractTags(url, urlSelector);
            extractedUrls = extractUrl(pageLinks);
        } else {
            for (var sitemap : sitemapLinks) {
                String sitemapUrl = sitemap.text();
                extractedUrls.addAll(extractUrl(extractTags(sitemapUrl, urlSelector)));
            }
        }
        return extractedUrls;
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

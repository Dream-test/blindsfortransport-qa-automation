package com.blindsfortransport.tests;

import com.blindsfortransport.tests.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SitemapUrlUpdater {
    private static final Logger logger = LoggerFactory.getLogger(SitemapUrlUpdater.class);
    private static final String filePath = AppConfig.URLS_CSV_PATH;

    public static void main(String[] args) {
        //1. Read existing URLs
        Set<String> existingUrls = CheckSitemap.readUrlsFromCsv(filePath);

        //2. Extract new URLs from sitemap
        Set<String> allExtractedUrls = CheckSitemap.extractUrlsFromSitemap(AppConfig.sitemapUrl);

        //3. Find new URLs
        Set<String> newUrls = new HashSet<>(allExtractedUrls);
        newUrls.removeAll(existingUrls);

        //4. Add to the file (if there are any new ones)
        if (!newUrls.isEmpty()) {
            CheckSitemap.appendUrlsToCsv(filePath, newUrls);
            logger.info("Added {} new URLs to {}.", newUrls.size(), filePath);
            for (String url : newUrls) {
                logger.info("➕ {}", url);
            }
        } else {
            logger.info("No new URLs found. File {} is up to date.", filePath);
        }

        logger.info("Total URLs in {} now must be: {}", filePath, existingUrls.size() + newUrls.size());

        // 5. Total
        Set<String> updatedUrls = CheckSitemap.readUrlsFromCsv(filePath);
        logger.info("Real total URLs in {} file now: {}", filePath, updatedUrls.size());

        // Optional exit code
        System.exit(newUrls.isEmpty() ? 0 : 1);

    }
}

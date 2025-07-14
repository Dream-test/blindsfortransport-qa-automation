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

    public static void main(String[] args) throws IOException {
        String sitemapIndexUrl = AppConfig.getSitemapUrl();
        logger.info("SitemapIndexUrl: {}", sitemapIndexUrl);
        Set<String> allUrls = new HashSet<>();

        try { // try-catch to handle IOException at the top level
            //Download all nested sitemap's
            Document indexDoc = Jsoup.connect(sitemapIndexUrl).get();

            Elements sitemapLinks = indexDoc.select("sitemap > loc");

            // Handle the case where sitemapIndexUrl might be a direct sitemap, not an index
            if (sitemapLinks.isEmpty()) {
                logger.warn("No nested sitemaps found in sitemap_index.xml using selector 'sitemap > loc'. Attempting to parse as a direct sitemap.");
                Elements pageLinks = indexDoc.select("url > loc");
                for (var pageLoc : pageLinks) {
                    allUrls.add(pageLoc.text());
                }
            } else {
                for (var sitemap : sitemapLinks) {
                    String sitemapUrl = sitemap.text();
                    logger.info("Processing nested Sitemap Url: {}", sitemapUrl);

                    try { // try-catch to handle errors when loading nested sitemaps
                        Document sitemapDoc = Jsoup.connect(sitemapUrl).get();

                        Elements pageLinks = sitemapDoc.select("url > loc");

                        for (var page : pageLinks) {
                            String url = page.text();
                            // check for the domain to make sure they are the right URLs
                            if (url.startsWith("https://www.blindsfortransport.com/")) {
                                logger.info("Add Url: {}", url);
                                allUrls.add(url);
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Error downloading or parsing sitemap {}: {}", sitemapUrl, e.getMessage());
                    }
                }
            }

            try (FileWriter writer = new FileWriter("src/test/resources/testdata/urlsandstatus.csv")) {
                writer.write("url,expected_status\n");
                for (String url : allUrls) {
                    writer.write(url + ",200\n");
                }
            }

            logger.info("AllUrl list size: {}", allUrls.size());
            logger.info("CSV-file 'urls.csv' was created successfully. It contains {} URLs.", allUrls.size());
        } catch (IOException e) {
            logger.error("Error downloading or parsing sitemap index {}: {}", sitemapIndexUrl, e.getMessage());
        }
    }
}
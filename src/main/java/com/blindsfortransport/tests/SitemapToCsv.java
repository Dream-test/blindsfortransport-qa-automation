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

        try { // try-catch to handle IOException at the top level
            //Download all nested sitemap's
            Document indexDoc = Jsoup.connect(sitemapIndexUrl).get();

            Elements sitemapLinks = indexDoc.select("sitemap > loc");

            // Handle the case where sitemapIndexUrl might be a direct sitemap, not an index
            if (sitemapLinks.isEmpty()) {
                logger.warn("No nested sitemaps found in sitemap_index.xml using selector 'sitemap > loc'. Attempting to parse as a direct sitemap.");
                logger.info("Processing nested SitemapIndex as Sitemap Url: {}", sitemapIndexUrl);

                /*
                Elements pageLinks = indexDoc.select("url > loc");
                for (var pageLoc : pageLinks) {
                    allUrls.add(pageLoc.text());
                }
                 */

                extractUrls(indexDoc, allUrls);
            } else {
                for (var sitemap : sitemapLinks) {
                    String sitemapUrl = sitemap.text();
                    logger.info("Processing nested Sitemap Url: {}", sitemapUrl);

                    try { // try-catch to handle errors when loading nested sitemaps
                        Document sitemapDoc = Jsoup.connect(sitemapUrl).get();

                        /*
                        Elements pageLinks = sitemapDoc.select("url > loc");

                        for (var page : pageLinks) {
                            String url = page.text();
                            // check for the domain to make sure they are the right URLs
                            if (url.startsWith("https://www.blindsfortransport.com/")) {
                                logger.info("Add Url: {}", url);
                                allUrls.add(url);
                            }
                        }
                         */

                        extractUrls(sitemapDoc, allUrls);
                    } catch (IOException e) {
                        logger.error("Error downloading or parsing sitemap {}: {}", sitemapUrl, e.getMessage());
                    }
                }
            }


                try (FileWriter urlWriter = new FileWriter(AppConfig.URLS_CSV_PATH);
                     FileWriter urlAndStatusWriter = new FileWriter(AppConfig.URLS_WITH_STATUS_CSV_PATH)) {

                    urlWriter.write("url\n");
                    urlAndStatusWriter.write("url,expected_status\n");

                    if (allUrls.isEmpty()) {
                        logger.warn("No URLs were extracted from the sitemap. Files will still be created but will be empty.");
                    } else {
                    for (String url : allUrls) {
                        urlWriter.write(url + "\n");
                        urlAndStatusWriter.write(url + ",200\n");
                    }
                }
            }

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
            //logger.info("CSV-file 'urls.csv' was created successfully. It contains {} URLs.", allUrls.size());
        } catch (IOException e) {
            logger.error("Error downloading or parsing sitemap index {}: {}", sitemapIndexUrl, e.getMessage());
        }
    }

    private static void  extractUrls(Document sitemapDoc, Set<String> allUrls) {
        Elements pageLinks = sitemapDoc.select("url > loc");
        for (var page : pageLinks) {
            String url = page.text();
            // check for the domain to make sure they are the right URLs
            if (url.startsWith(baseUrl)) {
                logger.info("Add Url: {}", url);
                allUrls.add(url);
            }
        }
    }
}
package com.blindsfortransport.tests.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppConfig {
    public static final String sitemapUrl = "https://www.blindsfortransport.com/sitemap_index.xml";
    public static final String baseUrl = "https://www.blindsfortransport.com";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String TIMESTAMP = LocalDateTime.now().format(formatter);

    public static final String TEST_URLS_CSV_PATH = "src/test/resources/testdata/urls.csv";
    //public static final String URLS_CSV_PATH = "src/test/resources/testdata/current/urls_" + TIMESTAMP + ".csv";
    public static final String URLS_CSV_PATH = "src/test/resources/testdata/current/currenturls.csv";
    //public static final String URLS_WITH_STATUS_CSV_PATH = "src/test/resources/testdata/current/urlsandstatus_" + TIMESTAMP + ".csv";
    public static final String URLS_WITH_STATUS_CSV_PATH = "src/test/resources/testdata/current/currenturlsandstatus.csv";
    public static final String TITLES_CSV_PATH = "src/test/resources/testdata/current/urlsandtitles_" + TIMESTAMP + ".csv";
    public static final String TEST_URLS_WITH_STATUS_CSV_PATH = "src/test/resources/testdata/urlsandstatus.csv" ;

    /*
    public static String getSitemapUrl() {
        return sitemapUrl;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
     */
}

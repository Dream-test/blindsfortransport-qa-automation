package com.blindsfortransport.tests;

import com.blindsfortransport.tests.config.AppConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class TitlesCsvGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TitlesCsvGenerator.class);

    public static void main(String[] args) {
        int total = 0;
        int errors = 0;

        logger.info("Starting Titles CSV generation process...");

        try (BufferedReader reader = new BufferedReader(new FileReader(AppConfig.TEST_URLS_CSV_PATH));
             FileWriter writer = new FileWriter(AppConfig.TITLES_CSV_PATH)) {
            // Skip input CSV header
            String header = reader.readLine();
            logger.info("Read file {} with header: {}", AppConfig.TEST_URLS_CSV_PATH, header);

            // Write output file header
            writer.write("url,title\n");

            String url;
            while ((url = reader.readLine()) != null) {
                total++;
                try {
                    String title = fetchTitle(url);

                    writer.write(String.format("\"%s\",\"%s\"\n", url, title));
                    logger.info("Fetched title for URL {}: {}", url, title);
                } catch (IOException e) {
                    logger.error("Failed to fetch title for URL {} : {}", url, e.getMessage());
                    writer.write(String.format("\"%s\",\"ERROR: %s\"\n", url, e.getClass().getSimpleName()));
                    errors++;
                }
            }
            logger.info("Finished processing. Total URLs: {}, Errors: {}", total, errors);
        } catch (Exception e) {
            logger.error("Error reading {} or writing{} file: {}. Message: {}", AppConfig.TEST_URLS_CSV_PATH, AppConfig.TITLES_CSV_PATH, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static String fetchTitle(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .timeout(10000)
                .get();
        String title = doc.title();
        if (title.isBlank()) {
            logger.warn("Title for URL {} is empty or missing", url);
            return "<NO_TITLE>";
        }
        return title.replaceAll("\"", "\"\""); // Change quotes type;
    }
}

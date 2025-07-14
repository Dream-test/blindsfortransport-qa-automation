package com.blindsfortransport.tests.responsetests.tests;

import io.restassured.RestAssured;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.assertj.core.api.Assertions;

import java.util.stream.Stream;

import com.blindsfortransport.tests.responsetests.base.TestConfig;
import com.blindsfortransport.tests.responsetests.utils.CsvDataUtils;

import static io.restassured.RestAssured.given;

@DisplayName("Paths status & titles tests")
public class PathStatusTests {
    private static final Logger logger = LoggerFactory.getLogger(PathStatusTests.class);
    private static final String baseUrl = TestConfig.getTestBaseUrl();

    @BeforeEach
    void logTestStart(TestInfo testInfo) {
        logger.info(">>> Starting test: {}", testInfo.getDisplayName());
    }

    @AfterEach
    void logTestEnd(TestInfo testInfo) {
        logger.info("<<< Finished test: {}", testInfo.getDisplayName());
    }

    @Disabled("Disabled temporarily until configuration is fixed")
    @ParameterizedTest(name = "Ptah: {0}")
    @MethodSource("pathProvider")
    void responseShouldReturn200(String path) {
        logger.info("GET request for baseUrl+path: {}", (baseUrl + path));
        var response = given()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115 Safari/537.36")
                .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                .when()
                .get(baseUrl + path)
                .then()
                .statusCode(200);

        //logger.warn("Received status {} for url {}. Body:\n{}", response.getStatusCode(), baseUrl + path, response.getBody().asString());
    }

    static Stream<String> pathProvider() {
        return CsvDataUtils.readPathsFromCsv("allpaths.csv");
    }

    @ParameterizedTest(name = "URL: {0} -> Expected Status: {1}")
    @MethodSource("urlStatusProvider")
    void responseShouldReturnExpectedStatus(String url, int expectedStatus) {
        logger.info("GET request for fullUrl: {} expected staus: {}", url, expectedStatus);
        given()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115 Safari/537.36")
                .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                .when().get(url)
                .then().statusCode(expectedStatus);
    }

    static Stream<Arguments> urlStatusProvider() {
        Stream<Arguments> stream = CsvDataUtils.readUrlsWithStatusFromCsv("urlsandstatus");
        if (stream == null) {
            throw new RuntimeException("CSV file could not be loaded. Check logs for details.");
        }
        return stream;
    }

    @ParameterizedTest(name = "URL: {0}")
    @CsvFileSource(resources = "/testdata/urls.csv", numLinesToSkip = 1)
    void responsePagesShouldReturn200(String url) {
        logger.info("GET request for URL: {}", (url));
        var response = given()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115 Safari/537.36")
                .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                .when()
                .get(url)
                .then()
                .statusCode(200);
    }

    @ParameterizedTest(name = "Checking <title> for {0}")
    @CsvFileSource(resources = "/testdata/urlsandtitle.csv", numLinesToSkip = 1)
    void checkPageTitle(String url, String expectedTitle) {
        //Executing GET request and get the response body as a string
        String responseBody = RestAssured
                .given()
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        //Performing html parsing using jsoup and extracting <title> tag value
        Document html = Jsoup.parse(responseBody);
        String actualTitle = html.title();

        Assertions.assertThat(actualTitle)
                .as("Title for URL: %s", url)
                .isEqualTo(expectedTitle);

    }
}

package responsetests.tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import utils.CsvDataUtils;

import static io.restassured.RestAssured.given;

@DisplayName("Paths status & titles tests")
public class PathStatusTests {
    private static final Logger logger = LoggerFactory.getLogger(PathStatusTests.class);
    private static final String baseUrl = "https://www.blindsfortransport.com";

    @BeforeEach
    void logTestStart(TestInfo testInfo) {
        logger.info(">>> Starting test: {}", testInfo.getDisplayName());
    }

    @AfterEach
    void logTestEnd(TestInfo testInfo) {
        logger.info("<<< Finished test: {}", testInfo.getDisplayName());
    }

    @ParameterizedTest
    @MethodSource("pathProvider")
    void responseShouldReturn200(String path) {
        logger.info("GET request for: {}", (baseUrl + path));
        given()
                .when().get(baseUrl + path)
                .then().statusCode(200);
    }

    static Stream<String> pathProvider() {
        return CsvDataUtils.readPathsFromCsv("allpaths.csv");
    }
}

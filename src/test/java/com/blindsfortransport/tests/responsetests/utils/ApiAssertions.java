package com.blindsfortransport.tests.responsetests.utils;

import static io.restassured.RestAssured.given;

public class ApiAssertions {

    public static void assertStatusCode(String url, int expectedStatus) {
        given()
                .when()
                .get(url)
                .then()
                .statusCode(expectedStatus);
    }


}

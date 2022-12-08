package net.stepniak.morenomodels.serviceserverless;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class GreetingTest
{
    @Test
    public void testJaxrs() {
        RestAssured.when().get("/hello").then()
                .contentType("text/plain")
                .body(equalTo("hello jaxrs"));
    }

    @Test
    public void testModels() {
        RestAssured.when().get("/models").then()
                .contentType("application/json")
                .body(equalTo("xd"));
    }
}

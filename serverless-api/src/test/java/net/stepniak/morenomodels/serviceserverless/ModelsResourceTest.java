package net.stepniak.morenomodels.serviceserverless;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class ModelsResourceTest
{
    @Test
    public void testModels() {
        RestAssured.when().get("/models").then()
                .contentType("application/json")
                .body(equalTo("xd"));
    }
}

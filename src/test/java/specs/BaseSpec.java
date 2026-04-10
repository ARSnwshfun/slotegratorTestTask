package specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import static io.restassured.http.ContentType.JSON;

public final class BaseSpec {

    private BaseSpec() {
    }

    public static RequestSpecification requestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri("https://testslotegrator.com")
                .setContentType(JSON)
                .setAccept(JSON)
                .build();
    }
}

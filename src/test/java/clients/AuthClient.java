package clients;

import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.response.Response;
import models.LoginRequest;
import models.LoginResponse;
import org.apache.http.HttpStatus;
import specs.BaseSpec;

import static io.restassured.RestAssured.given;

public class AuthClient {
    private static final String LOGIN_PATH = "/api/tester/login";

    public static LoginResponse login() {
        Dotenv dotenv = Dotenv.load();

        String email = dotenv.get("TEST_EMAIL");
        String password = dotenv.get("TEST_PASSWORD");

        LoginRequest request = new LoginRequest(email, password);

        return given()
                .spec(BaseSpec.requestSpec())
                .body(request)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .as(LoginResponse.class);
    }

    public static String getAccessToken() {
        return login().getAccessToken();
    }

    public static Response loginRaw() {
        Dotenv dotenv = Dotenv.load();

        String email = dotenv.get("TEST_EMAIL");
        String password = dotenv.get("TEST_PASSWORD");

        LoginRequest request = new LoginRequest(email, password);

        return given()
                .spec(BaseSpec.requestSpec())
                .body(request)
                .log().all()
                .when()
                .post(LOGIN_PATH);
    }
}
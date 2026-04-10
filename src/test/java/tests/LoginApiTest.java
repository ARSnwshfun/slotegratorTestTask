package tests;

import clients.AuthClient;
import io.github.cdimascio.dotenv.Dotenv;
import models.LoginRequest;
import models.LoginResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.apache.http.HttpStatus;
import specs.BaseSpec;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LoginApiTest {

    private static final String LOGIN_PATH = "/api/tester/login";

    @Test
    void loginTest() {
        LoginResponse response = AuthClient.login();

        assertThat(response).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isNotBlank();
        assertThat(response.getUser().getEmail()).isEqualTo("hromnik5ever@gmail.com");
        assertThat(response.getUser().getName()).isEqualTo("Nikita");
        assertThat(response.getUser().getSurname()).isEqualTo("Murauyou");
        assertThat(response.getAccessToken()).isNotBlank();
    }

    @Test
    void loginShouldFailWhenEmailIsNull() {
        Dotenv dotenv = Dotenv.load();
        String password = dotenv.get("TEST_PASSWORD");

        String requestBody = """
                {
                  "email": null,
                  "password": "%s"
                }
                """.formatted(password);

        given()
                .spec(BaseSpec.requestSpec())
                .body(requestBody)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(401))
                .body("message", equalTo("Email or password is incorrect"));
    }

    @Test
    void loginShouldFailWhenPasswordIsNull() {
        Dotenv dotenv = Dotenv.load();
        String email = dotenv.get("TEST_EMAIL");

        String requestBody = """
                {
                  "email": "%s",
                  "password": null
                }
                """.formatted(email);

        given()
                .spec(BaseSpec.requestSpec())
                .body(requestBody)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(HttpStatus.SC_UNAUTHORIZED))
                .body("message", equalTo("Access denied"));
    }

    @Test
    void loginShouldFailWhenEmailIsMissing() {
        Dotenv dotenv = Dotenv.load();
        String password = dotenv.get("TEST_PASSWORD");

        String requestBody = """
                {
                  "password": "%s"
                }
                """.formatted(password);

        given()
                .spec(BaseSpec.requestSpec())
                .body(requestBody)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(HttpStatus.SC_UNAUTHORIZED))
                .body("message", equalTo("Email or password is incorrect"));
    }

    @Test
    void loginShouldFailWhenPasswordIsMissing() {
        Dotenv dotenv = Dotenv.load();
        String email = dotenv.get("TEST_EMAIL");

        String requestBody = """
            {
              "email": "%s"
            }
            """.formatted(email);

        given()
                .spec(BaseSpec.requestSpec())
                .body(requestBody)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(HttpStatus.SC_UNAUTHORIZED))
                .body("message", equalTo("Access denied"));
    }

    @Test
    void loginShouldFailWhenPasswordTooShort() {
        Dotenv dotenv = Dotenv.load();
        String email = dotenv.get("TEST_EMAIL");

        LoginRequest request = new LoginRequest(email, "123");

        given()
                .spec(BaseSpec.requestSpec())
                .body(request)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(HttpStatus.SC_UNAUTHORIZED))
                .body("message", equalTo("Access denied"));
    }

    @Test
    void loginShouldFailWhenEmailFormatIsInvalid() {
        Dotenv dotenv = Dotenv.load();
        String password = dotenv.get("TEST_PASSWORD");

        LoginRequest request = new LoginRequest("invalid-email", password);

        given()
                .spec(BaseSpec.requestSpec())
                .body(request)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(HttpStatus.SC_UNAUTHORIZED))
                .body("message", equalTo("Email or password is incorrect"));
    }

    @Test
    void loginShouldFailWhenPasswordIsWrong() {
        Dotenv dotenv = Dotenv.load();
        String email = dotenv.get("TEST_EMAIL");

        LoginRequest request = new LoginRequest(email, "wrongPassword123");

        given()
                .spec(BaseSpec.requestSpec())
                .body(request)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(HttpStatus.SC_UNAUTHORIZED))
                .body("message", equalTo("Access denied"));
    }

    @Test
    void loginShouldFailWhenUserDoesNotExist() {
        Dotenv dotenv = Dotenv.load();
        String password = dotenv.get("TEST_PASSWORD");

        LoginRequest request = new LoginRequest("not_existing_user_12345@test.com", password);

        given()
                .spec(BaseSpec.requestSpec())
                .body(request)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("statusCode", equalTo(HttpStatus.SC_UNAUTHORIZED))
                .body("message", equalTo("Email or password is incorrect"));
    }
}
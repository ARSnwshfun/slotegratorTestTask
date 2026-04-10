package clients;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.AutomationTaskCreateRequest;
import models.AutomationTaskGetOneRequest;
import org.apache.http.HttpStatus;
import specs.BaseSpec;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public final class AutomationTaskClient {

    private static final String CREATE_PATH = "/api/automationTask/create";
    private static final String GET_ONE_PATH = "/api/automationTask/getOne";
    private static final String GET_ALL_PATH = "/api/automationTask/getAll";
    private static final String DELETE_ONE_PATH = "/api/automationTask/deleteOne/{id}";

    private AutomationTaskClient() {
    }

    public static Response create(String token, AutomationTaskCreateRequest body) {
        return given()
                .spec(BaseSpec.requestSpec())
                .header("Authorization", "Bearer " + token)
                .body(body)
                .log().all()
                .when()
                .post(CREATE_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
    }

    public static String extractCreatedId(Response response) {
        return extractId(response.jsonPath());
    }

    public static Response getOneByEmail(String token, String email) {
        return given()
                .spec(BaseSpec.requestSpec())
                .header("Authorization", "Bearer " + token)
                .body(new AutomationTaskGetOneRequest(email))
                .log().all()
                .when()
                .post(GET_ONE_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
    }

    public static List<Map<String, Object>> getAllUsers(String token) {
        Response response = given()
                .spec(BaseSpec.requestSpec())
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .get(GET_ALL_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        return extractUsersList(response.jsonPath());
    }

    public static void deleteById(String token, String id) {
        given()
                .spec(BaseSpec.requestSpec())
                .header("Authorization", "Bearer " + token)
                .pathParam("id", id)
                .log().all()
                .when()
                .delete(DELETE_ONE_PATH)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK);
    }

    public static String extractId(JsonPath jsonPath) {
        String id = jsonPath.getString("_id");
        if (id == null) {
            id = jsonPath.getString("id");
        }
        return id;
    }

    public static String userIdFromMap(Map<String, Object> user) {
        Object id = user.get("_id");
        if (id == null) {
            id = user.get("id");
        }
        return id != null ? id.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> extractUsersList(JsonPath jsonPath) {
        List<Map<String, Object>> users = jsonPath.getList("");
        if (users == null) {
            users = jsonPath.getList("data");
        }
        if (users == null) {
            users = jsonPath.getList("users");
        }
        return users;
    }
}

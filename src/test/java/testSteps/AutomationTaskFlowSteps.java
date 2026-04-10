package teststeps;

import clients.AuthClient;
import clients.AutomationTaskClient;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import models.AutomationTaskCreateRequest;
import tests.support.AutomationUsersCleanup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AutomationTaskFlowSteps {

    public record CreatedUsers(List<String> ids, List<String> emails) {
    }

    private AutomationTaskFlowSteps() {
    }

    public static void preconditionDeleteAllExisting() {
        Allure.step("Precondition: remove all users returned by getAll", () ->
                AutomationUsersCleanup.deleteAllExisting());
    }

    public static String obtainAccessToken() {
        return Allure.step("Obtain access token", () -> {
            String token = AuthClient.getAccessToken();
            assertNotNull(token);
            assertFalse(token.isBlank());
            return token;
        });
    }

    public static CreatedUsers createTestUsers(String token, int totalUsers, long timestamp) {
        return Allure.step("Create " + totalUsers + " test users", () -> {
            List<String> createdUserIds = new ArrayList<>();
            List<String> createdUserEmails = new ArrayList<>();
            for (int i = 1; i <= totalUsers; i++) {
                int userIndex = i;
                Allure.step("Create user " + userIndex + " of " + totalUsers, () ->
                        createOneUser(token, userIndex, timestamp, createdUserIds, createdUserEmails));
            }
            assertEquals(totalUsers, createdUserIds.size(), "Exactly " + totalUsers + " users should be created");
            assertEquals(totalUsers, createdUserEmails.size(), "Exactly " + totalUsers + " user emails should be stored");
            return new CreatedUsers(createdUserIds, createdUserEmails);
        });
    }

    private static void createOneUser(
            String token,
            int userIndex,
            long timestamp,
            List<String> createdUserIds,
            List<String> createdUserEmails
    ) {
        AutomationTaskCreateRequest body = AutomationTaskCreateRequest.testUser(userIndex, timestamp);
        Response createResponse = AutomationTaskClient.create(token, body);
        String createdId = AutomationTaskClient.extractCreatedId(createResponse);
        assertNotNull(createdId, "Created user id should not be null");
        assertFalse(createdId.isBlank(), "Created user id should not be blank");
        createdUserIds.add(createdId);
        createdUserEmails.add(body.email());
    }

    public static void assertGetOneMatchesFirstUser(String token, CreatedUsers created) {
        Allure.step("Verify getOne matches first created user by email", () -> {
            String firstId = created.ids().get(0);
            String firstEmail = created.emails().get(0);
            Response getOneResponse = AutomationTaskClient.getOneByEmail(token, firstEmail);
            String fetchedId = AutomationTaskClient.extractId(getOneResponse.jsonPath());
            String fetchedEmail = getOneResponse.jsonPath().getString("email");
            assertEquals(firstId, fetchedId, "Fetched profile id should match created user id");
            assertEquals(firstEmail, fetchedEmail, "Fetched profile email should match created user email");
        });
    }

    public static void assertGetAllContainsCreatedAndSortedByName(String token, List<String> createdEmails) {
        Allure.step("Verify getAll: non-empty, contains created users, sorted by name", () -> {
            List<Map<String, Object>> users = AutomationTaskClient.getAllUsers(token);
            assertNotNull(users, "Users list should not be null");
            assertFalse(users.isEmpty(), "Users list should not be empty");
            for (String createdEmail : createdEmails) {
                boolean exists = users.stream()
                        .anyMatch(user -> createdEmail.equals(user.get("email")));
                assertTrue(exists, "Created user should exist in getAll response: " + createdEmail);
            }
            List<Map<String, Object>> sortedByName = new ArrayList<>(users);
            sortedByName.sort(
                    Comparator.comparing(
                            user -> (String) user.get("name"),
                            Comparator.nullsLast(String::compareTo)
                    )
            );
            assertFalse(sortedByName.isEmpty(), "Sorted users list should not be empty");
        });
    }

    public static void deleteUsersByIds(String token, List<String> userIds) {
        Allure.step("Delete created users by id", () -> {
            for (String userId : userIds) {
                AutomationTaskClient.deleteById(token, userId);
            }
        });
    }

    public static void assertGetAllIsEmpty(String token) {
        Allure.step("Verify getAll is empty after deletion", () -> {
            List<Map<String, Object>> usersAfterDelete = AutomationTaskClient.getAllUsers(token);
            assertNotNull(usersAfterDelete, "Users list after delete should not be null");
            assertTrue(usersAfterDelete.isEmpty(), "Users list should be empty after deleting created users");
        });
    }
}

package tests.support;

import clients.AuthClient;
import clients.AutomationTaskClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AutomationUsersCleanup {

    private AutomationUsersCleanup() {
    }

    public static void deleteAllExisting() {
        String token = AuthClient.getAccessToken();
        assertNotNull(token);
        assertFalse(token.isBlank());
        deleteAllExisting(token);
    }

    public static void deleteAllExisting(String token) {
        List<Map<String, Object>> users = AutomationTaskClient.getAllUsers(token);
        assertNotNull(users);
        for (Map<String, Object> user : users) {
            String id = AutomationTaskClient.userIdFromMap(user);
            assertNotNull(id, "User entry should have _id or id: " + user);
            assertFalse(id.isBlank());
            AutomationTaskClient.deleteById(token, id);
        }
        List<Map<String, Object>> remaining = AutomationTaskClient.getAllUsers(token);
        assertNotNull(remaining);
        assertTrue(remaining.isEmpty(), "After deleting all users, getAll should return an empty list");
    }
}

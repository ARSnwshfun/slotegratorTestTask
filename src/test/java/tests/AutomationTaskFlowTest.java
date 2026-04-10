package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import teststeps.AutomationTaskFlowSteps;
import teststeps.AutomationTaskFlowSteps.CreatedUsers;

@Epic("Automation Task API")
@Feature("End-to-end flow")
public class AutomationTaskFlowTest {

    private static final int USERS_TO_CREATE = 2;

    @BeforeEach
    void removeExistingAutomationUsers() {
        AutomationTaskFlowSteps.preconditionDeleteAllExisting();
    }

    @Test
    @DisplayName("Create → getOne → getAll → delete → empty list")
    void shouldCompleteAutomationTaskFlow() {
        String token = AutomationTaskFlowSteps.obtainAccessToken();
        long timestamp = System.currentTimeMillis();

        CreatedUsers created = AutomationTaskFlowSteps.createTestUsers(token, USERS_TO_CREATE, timestamp);

        AutomationTaskFlowSteps.assertGetOneMatchesFirstUser(token, created);

        AutomationTaskFlowSteps.assertGetAllContainsCreatedAndSortedByName(token, created.emails());

        AutomationTaskFlowSteps.deleteUsersByIds(token, created.ids());

        AutomationTaskFlowSteps.assertGetAllIsEmpty(token);
    }
}

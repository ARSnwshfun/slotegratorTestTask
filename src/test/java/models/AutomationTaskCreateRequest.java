package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AutomationTaskCreateRequest(
        @JsonProperty("currency_code") String currencyCode,
        String email,
        String name,
        @JsonProperty("password_change") String passwordChange,
        @JsonProperty("password_repeat") String passwordRepeat,
        String surname,
        String username
) {
    public static AutomationTaskCreateRequest testUser(int index, long timestamp) {
        String ts = Long.toString(timestamp);
        return new AutomationTaskCreateRequest(
                "USD",
                "autoTestEmail" + index + "_" + ts + "@mail.com",
                "autoTestUser" + index,
                "passwordTest",
                "passwordTest",
                "autoTestSurname" + index,
                "autoTestUsername" + index + "_" + ts
        );
    }
}

package org.keycloak.matrix;

import org.junit.jupiter.api.Test;
import org.keycloak.models.AuthenticatorConfigModel;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatrixConfigTest {

    @Test
    void constructor_shouldLoadValidConfig() {
        // Arrange
        Map<String, String> configMap = new HashMap<>();
        configMap.put(MatrixConfig.MATRIX_SERVER_URL, "https://matrix.org");
        configMap.put(MatrixConfig.BOT_USER_ID, "@bot:matrix.org");
        configMap.put(MatrixConfig.BOT_ACCESS_TOKEN, "secret_token");
        configMap.put(MatrixConfig.MESSAGE_TEMPLATE, "Your code is: {code}");
        configMap.put(MatrixConfig.OTP_VALIDITY_SECONDS, "600");
        configMap.put(MatrixConfig.OTP_LENGTH, "8");
        configMap.put(MatrixConfig.USER_ID_ATTRIBUTE, "custom_matrix_id");

        AuthenticatorConfigModel configModel = new AuthenticatorConfigModel();
        configModel.setConfig(configMap);

        // Act
        MatrixConfig config = new MatrixConfig(configModel);

        // Assert
        assertEquals("https://matrix.org", config.getServerUrl());
        assertEquals("@bot:matrix.org", config.getBotUserId());
        assertEquals("secret_token", config.getBotAccessToken());
        assertEquals("Your code is: {code}", config.getMessageTemplate());
        assertEquals(600, config.getOtpValiditySeconds());
        assertEquals(8, config.getOtpLength());
        assertEquals("custom_matrix_id", config.getUserIdAttribute());
    }

    @Test
    void constructor_shouldUseDefaultValuesWhenOptionalConfigMissing() {
        // Arrange
        Map<String, String> configMap = new HashMap<>();
        configMap.put(MatrixConfig.MATRIX_SERVER_URL, "https://matrix.org");
        configMap.put(MatrixConfig.BOT_USER_ID, "@bot:matrix.org");
        configMap.put(MatrixConfig.BOT_ACCESS_TOKEN, "secret_token");

        AuthenticatorConfigModel configModel = new AuthenticatorConfigModel();
        configModel.setConfig(configMap);

        // Act
        MatrixConfig config = new MatrixConfig(configModel);

        // Assert
        assertEquals("Your authentication code is: {code}", config.getMessageTemplate());
        assertEquals(300, config.getOtpValiditySeconds());
        assertEquals(6, config.getOtpLength());
        assertEquals("matrix_id", config.getUserIdAttribute());
    }

    @Test
    void constructor_shouldThrowExceptionWhenRequiredConfigMissing() {
        // Arrange
        Map<String, String> configMap = new HashMap<>();
        configMap.put(MatrixConfig.BOT_USER_ID, "@bot:matrix.org");
        configMap.put(MatrixConfig.BOT_ACCESS_TOKEN, "secret_token");
        // Missing MATRIX_SERVER_URL

        AuthenticatorConfigModel configModel = new AuthenticatorConfigModel();
        configModel.setConfig(configMap);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new MatrixConfig(configModel),
            "Should throw exception when required config is missing");
    }

    @Test
    void formatMessage_shouldReplaceCodePlaceholder() {
        // Arrange
        Map<String, String> configMap = new HashMap<>();
        configMap.put(MatrixConfig.MATRIX_SERVER_URL, "https://matrix.org");
        configMap.put(MatrixConfig.BOT_USER_ID, "@bot:matrix.org");
        configMap.put(MatrixConfig.BOT_ACCESS_TOKEN, "secret_token");
        configMap.put(MatrixConfig.MESSAGE_TEMPLATE, "Your verification code is: {code}");

        AuthenticatorConfigModel configModel = new AuthenticatorConfigModel();
        configModel.setConfig(configMap);
        MatrixConfig config = new MatrixConfig(configModel);

        // Act
        String formattedMessage = config.formatMessage("123456");

        // Assert
        assertEquals("Your verification code is: 123456", formattedMessage);
    }

    @Test
    void constructor_shouldHandleEmptyOrBlankValues() {
        // Arrange
        Map<String, String> configMap = new HashMap<>();
        configMap.put(MatrixConfig.MATRIX_SERVER_URL, "  ");  // Blank value
        configMap.put(MatrixConfig.BOT_USER_ID, "");         // Empty value
        configMap.put(MatrixConfig.BOT_ACCESS_TOKEN, "secret_token");

        AuthenticatorConfigModel configModel = new AuthenticatorConfigModel();
        configModel.setConfig(configMap);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new MatrixConfig(configModel),
            "Should throw exception when required config values are empty or blank");
    }

    @Test
    void constructor_shouldHandleInvalidNumbers() {
        // Arrange
        Map<String, String> configMap = new HashMap<>();
        configMap.put(MatrixConfig.MATRIX_SERVER_URL, "https://matrix.org");
        configMap.put(MatrixConfig.BOT_USER_ID, "@bot:matrix.org");
        configMap.put(MatrixConfig.BOT_ACCESS_TOKEN, "secret_token");
        configMap.put(MatrixConfig.OTP_VALIDITY_SECONDS, "invalid");

        AuthenticatorConfigModel configModel = new AuthenticatorConfigModel();
        configModel.setConfig(configMap);

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> new MatrixConfig(configModel),
            "Should throw exception when numeric values are invalid");
    }
}
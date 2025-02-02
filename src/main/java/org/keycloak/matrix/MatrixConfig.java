package org.keycloak.matrix;

import org.keycloak.models.AuthenticatorConfigModel;

/**
 * Configuration class for Matrix authentication settings.
 */
public class MatrixConfig {
    // Configuration keys
    public static final String MATRIX_SERVER_URL = "matrixServerUrl";
    public static final String BOT_USER_ID = "botUserId";
    public static final String BOT_ACCESS_TOKEN = "botAccessToken";
    public static final String MESSAGE_TEMPLATE = "messageTemplate";
    public static final String OTP_VALIDITY_SECONDS = "otpValiditySeconds";
    public static final String OTP_LENGTH = "otpLength";
    public static final String USER_ID_ATTRIBUTE = "matrixUserIdAttribute";

    // Default values
    private static final String DEFAULT_MESSAGE_TEMPLATE = "Your authentication code is: {code}";
    private static final String DEFAULT_OTP_VALIDITY_SECONDS = "300"; // 5 minutes
    private static final String DEFAULT_OTP_LENGTH = "6";
    private static final String DEFAULT_USER_ID_ATTRIBUTE = "matrix_id";

    private final String serverUrl;
    private final String botUserId;
    private final String botAccessToken;
    private final String messageTemplate;
    private final int otpValiditySeconds;
    private final int otpLength;
    private final String userIdAttribute;

    /**
     * Creates a new MatrixConfig instance from an AuthenticatorConfigModel.
     * @param config The Keycloak authenticator configuration
     */
    public MatrixConfig(AuthenticatorConfigModel config) {
        if (config == null || config.getConfig() == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        this.serverUrl = getRequiredConfig(config, MATRIX_SERVER_URL);
        this.botUserId = getRequiredConfig(config, BOT_USER_ID);
        this.botAccessToken = getRequiredConfig(config, BOT_ACCESS_TOKEN);
        
        this.messageTemplate = config.getConfig().getOrDefault(
            MESSAGE_TEMPLATE, DEFAULT_MESSAGE_TEMPLATE);
        this.otpValiditySeconds = Integer.parseInt(config.getConfig().getOrDefault(
            OTP_VALIDITY_SECONDS, DEFAULT_OTP_VALIDITY_SECONDS));
        this.otpLength = Integer.parseInt(config.getConfig().getOrDefault(
            OTP_LENGTH, DEFAULT_OTP_LENGTH));
        this.userIdAttribute = config.getConfig().getOrDefault(
            USER_ID_ATTRIBUTE, DEFAULT_USER_ID_ATTRIBUTE);
    }

    private String getRequiredConfig(AuthenticatorConfigModel config, String key) {
        String value = config.getConfig().get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Required configuration '" + key + "' is missing");
        }
        return value;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getBotUserId() {
        return botUserId;
    }

    public String getBotAccessToken() {
        return botAccessToken;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public int getOtpValiditySeconds() {
        return otpValiditySeconds;
    }

    public int getOtpLength() {
        return otpLength;
    }

    public String getUserIdAttribute() {
        return userIdAttribute;
    }

    /**
     * Formats the OTP message using the configured template.
     * @param otp The OTP to include in the message
     * @return The formatted message
     */
    public String formatMessage(String otp) {
        return messageTemplate.replace("{code}", otp);
    }
}
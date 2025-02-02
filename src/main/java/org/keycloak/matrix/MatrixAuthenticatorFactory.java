package org.keycloak.matrix;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating Matrix authenticator instances.
 */
public class MatrixAuthenticatorFactory implements AuthenticatorFactory {
    public static final String ID = "matrix-2fa";
    private static final String DISPLAY_NAME = "Matrix 2FA";
    private static final String HELP_TEXT = "Sends a one-time password via Matrix messaging";
    private static final MatrixService matrixService = new MatrixServiceImpl();

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        // Matrix Server URL
        ProviderConfigProperty serverUrl = new ProviderConfigProperty();
        serverUrl.setName(MatrixConfig.MATRIX_SERVER_URL);
        serverUrl.setLabel("Matrix Server URL");
        serverUrl.setType(ProviderConfigProperty.STRING_TYPE);
        serverUrl.setHelpText("URL of the Matrix homeserver (e.g., https://matrix.org)");
        configProperties.add(serverUrl);

        // Bot User ID
        ProviderConfigProperty botUserId = new ProviderConfigProperty();
        botUserId.setName(MatrixConfig.BOT_USER_ID);
        botUserId.setLabel("Bot User ID");
        botUserId.setType(ProviderConfigProperty.STRING_TYPE);
        botUserId.setHelpText("Matrix user ID for the bot (e.g., @securitybot:matrix.org)");
        configProperties.add(botUserId);

        // Bot Access Token
        ProviderConfigProperty botAccessToken = new ProviderConfigProperty();
        botAccessToken.setName(MatrixConfig.BOT_ACCESS_TOKEN);
        botAccessToken.setLabel("Bot Access Token");
        botAccessToken.setType(ProviderConfigProperty.PASSWORD);
        botAccessToken.setHelpText("Access token for the Matrix bot");
        configProperties.add(botAccessToken);

        // Message Template
        ProviderConfigProperty messageTemplate = new ProviderConfigProperty();
        messageTemplate.setName(MatrixConfig.MESSAGE_TEMPLATE);
        messageTemplate.setLabel("Message Template");
        messageTemplate.setType(ProviderConfigProperty.STRING_TYPE);
        messageTemplate.setHelpText("Template for the OTP message. Use {code} as placeholder for the OTP");
        messageTemplate.setDefaultValue("Your authentication code is: {code}");
        configProperties.add(messageTemplate);

        // OTP Validity Period
        ProviderConfigProperty otpValidity = new ProviderConfigProperty();
        otpValidity.setName(MatrixConfig.OTP_VALIDITY_SECONDS);
        otpValidity.setLabel("OTP Validity Period");
        otpValidity.setType(ProviderConfigProperty.STRING_TYPE);
        otpValidity.setHelpText("Time in seconds for which the OTP remains valid");
        otpValidity.setDefaultValue("300");
        configProperties.add(otpValidity);

        // OTP Length
        ProviderConfigProperty otpLength = new ProviderConfigProperty();
        otpLength.setName(MatrixConfig.OTP_LENGTH);
        otpLength.setLabel("OTP Length");
        otpLength.setType(ProviderConfigProperty.STRING_TYPE);
        otpLength.setHelpText("Length of the generated OTP");
        otpLength.setDefaultValue("6");
        configProperties.add(otpLength);

        // Matrix User ID Attribute
        ProviderConfigProperty userIdAttribute = new ProviderConfigProperty();
        userIdAttribute.setName(MatrixConfig.USER_ID_ATTRIBUTE);
        userIdAttribute.setLabel("Matrix User ID Attribute");
        userIdAttribute.setType(ProviderConfigProperty.STRING_TYPE);
        userIdAttribute.setHelpText("User attribute containing the Matrix user ID");
        userIdAttribute.setDefaultValue("matrix_id");
        configProperties.add(userIdAttribute);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new MatrixAuthenticator(matrixService);
    }

    @Override
    public void init(Config.Scope config) {
        // No initialization needed
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No post-initialization needed
    }

    @Override
    public void close() {
        // No cleanup needed
    }
}
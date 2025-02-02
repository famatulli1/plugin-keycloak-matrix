package org.keycloak.matrix;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;

/**
 * Matrix authenticator implementation that handles the 2FA flow using Matrix messages.
 */
public class MatrixAuthenticator implements Authenticator {
    private static final Logger logger = Logger.getLogger(MatrixAuthenticator.class);
    private static final String MATRIX_OTP_FORM_ID = "matrix-2fa-form.ftl";
    
    private final MatrixService matrixService;

    public MatrixAuthenticator(MatrixService matrixService) {
        this.matrixService = matrixService;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        MatrixConfig config = new MatrixConfig(context.getAuthenticatorConfig());

        String matrixUserId = user.getFirstAttribute(config.getUserIdAttribute());
        if (matrixUserId == null || matrixUserId.trim().isEmpty()) {
            logger.warn("No Matrix user ID found for user: " + user.getUsername());
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        }

        try {
            // Initialize Matrix service if not already done
            if (!matrixService.isInitialized()) {
                matrixService.initialize(config);
            }

            // Generate and store OTP
            String otp = OTPUtil.generateOTP(config.getOtpLength());
            OTPUtil.storeOTP(user, otp, config.getOtpValiditySeconds());

            // Send OTP via Matrix
            String message = config.formatMessage(otp);
            matrixService.sendOTP(matrixUserId, message);

            // Show OTP input form
            Response challenge = context.form()
                .setAttribute("matrixUserId", matrixUserId)
                .setAttribute("otpLength", config.getOtpLength())
                .createForm(MATRIX_OTP_FORM_ID);
            
            context.challenge(challenge);
            
        } catch (MatrixInitializationException e) {
            logger.error("Failed to initialize Matrix service", e);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        } catch (MatrixMessageException e) {
            logger.error("Failed to send Matrix message", e);
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        String providedOTP = context.getHttpRequest().getDecodedFormParameters().getFirst("otp");

        if (providedOTP == null || providedOTP.trim().isEmpty()) {
            failWithInvalidOTP(context, "No OTP provided");
            return;
        }

        if (OTPUtil.validateOTP(user, providedOTP)) {
            context.success();
        } else {
            failWithInvalidOTP(context, "Invalid or expired OTP");
        }
    }

    private void failWithInvalidOTP(AuthenticationFlowContext context, String error) {
        logger.debug(error);
        Response challenge = context.form()
            .setError(error)
            .createForm(MATRIX_OTP_FORM_ID);
        context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // Check if user has Matrix ID configured
        String matrixUserId = user.getFirstAttribute(
            new MatrixConfig(null).getUserIdAttribute()
        );
        return matrixUserId != null && !matrixUserId.trim().isEmpty();
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Could add a required action here to force users to set up their Matrix ID
        // if not already configured
    }

    @Override
    public void close() {
        // No cleanup needed
    }
}
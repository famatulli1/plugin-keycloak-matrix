package org.keycloak.matrix;

import org.keycloak.common.util.Time;
import org.keycloak.models.UserModel;

import java.security.SecureRandom;

/**
 * Utility class for OTP generation and validation.
 */
public class OTPUtil {
    private static final String OTP_ATTRIBUTE = "matrix_2fa_otp";
    private static final String OTP_EXPIRY_ATTRIBUTE = "matrix_2fa_otp_expiry";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generates a new OTP with the specified length.
     * @param length The length of the OTP to generate
     * @return The generated OTP
     */
    public static String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Stores the OTP and its expiry time in user attributes.
     * @param user The user model to store the OTP for
     * @param otp The OTP to store
     * @param validitySeconds The number of seconds the OTP should be valid for
     */
    public static void storeOTP(UserModel user, String otp, int validitySeconds) {
        int expiryTime = Time.currentTime() + validitySeconds;
        user.setSingleAttribute(OTP_ATTRIBUTE, otp);
        user.setSingleAttribute(OTP_EXPIRY_ATTRIBUTE, String.valueOf(expiryTime));
    }

    /**
     * Validates the provided OTP against the stored one.
     * @param user The user model to validate the OTP for
     * @param providedOTP The OTP to validate
     * @return true if the OTP is valid and not expired, false otherwise
     */
    public static boolean validateOTP(UserModel user, String providedOTP) {
        String storedOTP = user.getFirstAttribute(OTP_ATTRIBUTE);
        String expiryTimeStr = user.getFirstAttribute(OTP_EXPIRY_ATTRIBUTE);

        // Clear the stored OTP regardless of validation result
        clearOTP(user);

        if (storedOTP == null || expiryTimeStr == null) {
            return false;
        }

        try {
            int expiryTime = Integer.parseInt(expiryTimeStr);
            if (Time.currentTime() > expiryTime) {
                return false;
            }

            return storedOTP.equals(providedOTP);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Clears the stored OTP and expiry time from user attributes.
     * @param user The user model to clear the OTP for
     */
    public static void clearOTP(UserModel user) {
        user.removeAttribute(OTP_ATTRIBUTE);
        user.removeAttribute(OTP_EXPIRY_ATTRIBUTE);
    }
}
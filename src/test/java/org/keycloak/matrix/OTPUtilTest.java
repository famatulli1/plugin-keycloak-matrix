package org.keycloak.matrix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.util.Time;
import org.keycloak.models.UserModel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OTPUtilTest {

    @Mock
    private UserModel userModel;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateOTP_shouldGenerateCorrectLength() {
        // Test different lengths
        for (int length : new int[]{4, 6, 8}) {
            String otp = OTPUtil.generateOTP(length);
            assertEquals(length, otp.length(), "OTP length should match requested length");
            assertTrue(otp.matches("\\d+"), "OTP should contain only digits");
        }
    }

    @Test
    void validateOTP_shouldReturnTrueForValidOTP() {
        // Arrange
        String otp = "123456";
        int validitySeconds = 300;
        int expiryTime = Time.currentTime() + validitySeconds;

        when(userModel.getFirstAttribute("matrix_2fa_otp")).thenReturn(otp);
        when(userModel.getFirstAttribute("matrix_2fa_otp_expiry"))
            .thenReturn(String.valueOf(expiryTime));

        // Act
        boolean isValid = OTPUtil.validateOTP(userModel, otp);

        // Assert
        assertTrue(isValid, "OTP should be valid");
        verify(userModel).removeAttribute("matrix_2fa_otp");
        verify(userModel).removeAttribute("matrix_2fa_otp_expiry");
    }

    @Test
    void validateOTP_shouldReturnFalseForExpiredOTP() {
        // Arrange
        String otp = "123456";
        int expiryTime = Time.currentTime() - 1; // Expired

        when(userModel.getFirstAttribute("matrix_2fa_otp")).thenReturn(otp);
        when(userModel.getFirstAttribute("matrix_2fa_otp_expiry"))
            .thenReturn(String.valueOf(expiryTime));

        // Act
        boolean isValid = OTPUtil.validateOTP(userModel, otp);

        // Assert
        assertFalse(isValid, "Expired OTP should be invalid");
        verify(userModel).removeAttribute("matrix_2fa_otp");
        verify(userModel).removeAttribute("matrix_2fa_otp_expiry");
    }

    @Test
    void validateOTP_shouldReturnFalseForIncorrectOTP() {
        // Arrange
        String storedOtp = "123456";
        String providedOtp = "654321";
        int expiryTime = Time.currentTime() + 300;

        when(userModel.getFirstAttribute("matrix_2fa_otp")).thenReturn(storedOtp);
        when(userModel.getFirstAttribute("matrix_2fa_otp_expiry"))
            .thenReturn(String.valueOf(expiryTime));

        // Act
        boolean isValid = OTPUtil.validateOTP(userModel, providedOtp);

        // Assert
        assertFalse(isValid, "Incorrect OTP should be invalid");
        verify(userModel).removeAttribute("matrix_2fa_otp");
        verify(userModel).removeAttribute("matrix_2fa_otp_expiry");
    }

    @Test
    void storeOTP_shouldStoreOTPAndExpiry() {
        // Arrange
        String otp = "123456";
        int validitySeconds = 300;

        // Act
        OTPUtil.storeOTP(userModel, otp, validitySeconds);

        // Assert
        verify(userModel).setSingleAttribute(eq("matrix_2fa_otp"), eq(otp));
        verify(userModel).setSingleAttribute(
            eq("matrix_2fa_otp_expiry"), 
            argThat(expiry -> {
                int expiryTime = Integer.parseInt(expiry);
                int expectedExpiry = Time.currentTime() + validitySeconds;
                return Math.abs(expiryTime - expectedExpiry) <= 1; // Allow 1 second difference
            })
        );
    }

    @Test
    void validateOTP_shouldReturnFalseForMissingAttributes() {
        // Arrange
        when(userModel.getFirstAttribute("matrix_2fa_otp")).thenReturn(null);
        when(userModel.getFirstAttribute("matrix_2fa_otp_expiry")).thenReturn(null);

        // Act
        boolean isValid = OTPUtil.validateOTP(userModel, "123456");

        // Assert
        assertFalse(isValid, "OTP should be invalid when attributes are missing");
    }
}
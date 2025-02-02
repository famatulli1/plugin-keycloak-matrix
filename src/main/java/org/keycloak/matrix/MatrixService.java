package org.keycloak.matrix;

/**
 * Service interface for Matrix communication operations.
 */
public interface MatrixService {
    /**
     * Initializes the Matrix client with the given configuration.
     * @param config The Matrix configuration containing server URL and credentials
     * @throws MatrixInitializationException if initialization fails
     */
    void initialize(MatrixConfig config) throws MatrixInitializationException;

    /**
     * Sends a one-time password to a Matrix user.
     * @param matrixUserId The Matrix user ID to send the OTP to
     * @param otp The one-time password to send
     * @throws MatrixMessageException if message sending fails
     */
    void sendOTP(String matrixUserId, String otp) throws MatrixMessageException;

    /**
     * Checks if the Matrix service is properly initialized and ready to send messages.
     * @return true if the service is initialized and ready, false otherwise
     */
    boolean isInitialized();
}

/**
 * Exception thrown when Matrix service initialization fails.
 */
class MatrixInitializationException extends Exception {
    public MatrixInitializationException(String message) {
        super(message);
    }

    public MatrixInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when sending a Matrix message fails.
 */
class MatrixMessageException extends Exception {
    public MatrixMessageException(String message) {
        super(message);
    }

    public MatrixMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
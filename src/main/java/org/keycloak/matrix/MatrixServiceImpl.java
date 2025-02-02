package org.keycloak.matrix;

import io.github.ma1uta.matrix.client.MatrixClient;
import io.github.ma1uta.matrix.client.StandaloneClient;
import io.github.ma1uta.matrix.client.model.room.RoomId;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * Implementation of the MatrixService interface using the Matrix SDK.
 */
public class MatrixServiceImpl implements MatrixService {
    private static final Logger logger = Logger.getLogger(MatrixServiceImpl.class);
    
    private MatrixClient client;
    private String accessToken;
    private volatile boolean initialized = false;

    @Override
    public void initialize(MatrixConfig config) throws MatrixInitializationException {
        try {
            client = new StandaloneClient.Builder()
                .domain(extractDomain(config.getServerUrl()))
                .build();

            // Set the homeserver URL
            client.setHomeserver(config.getServerUrl());
            
            // Use the provided access token for authentication
            client.setAccessToken(config.getBotAccessToken());
            this.accessToken = config.getBotAccessToken();

            // Verify the token is valid by trying to get account data
            client.account().whoami().join();
            
            initialized = true;
            logger.info("Matrix service initialized successfully");
        } catch (Exception e) {
            initialized = false;
            throw new MatrixInitializationException("Failed to initialize Matrix client", e);
        }
    }

    @Override
    public void sendOTP(String matrixUserId, String otp) throws MatrixMessageException {
        if (!initialized) {
            throw new MatrixMessageException("Matrix service not initialized");
        }

        try {
            // Create or get direct message room with the user
            RoomId roomId = createOrGetDirectMessageRoom(matrixUserId);
            
            // Send the message
            client.event().sendFormattedMessage(roomId.getRoomId(), otp, null).join();
            
            logger.debug("OTP sent successfully to " + matrixUserId);
        } catch (CompletionException e) {
            logger.error("Failed to send OTP to " + matrixUserId, e);
            throw new MatrixMessageException("Failed to send OTP", e);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    private RoomId createOrGetDirectMessageRoom(String userId) throws MatrixMessageException {
        try {
            // First try to find existing direct message room
            List<String> rooms = client.room().joinedRooms().join().getJoinedRooms();
            
            for (String roomId : rooms) {
                var members = client.room().joinedMembers(roomId).join();
                if (members.getJoined().size() == 2 && 
                    members.getJoined().containsKey(userId)) {
                    return new RoomId(roomId);
                }
            }

            // If no existing room found, create a new one
            var createRoomResponse = client.room().createDirectRoom(userId).join();
            return new RoomId(createRoomResponse.getRoomId());
        } catch (CompletionException e) {
            throw new MatrixMessageException("Failed to create or get direct message room", e);
        }
    }

    private String extractDomain(String serverUrl) {
        return serverUrl.replaceAll("^https?://", "")  // Remove protocol
                       .replaceAll("/$", "")           // Remove trailing slash
                       .split(":")[0];                 // Remove port if present
    }
}
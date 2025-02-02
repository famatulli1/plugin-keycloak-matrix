package org.keycloak.matrix;

import io.github.ma1uta.matrix.Event;
import io.github.ma1uta.matrix.client.MatrixClient;
import io.github.ma1uta.matrix.client.model.room.CreateRoomRequest;
import io.github.ma1uta.matrix.event.RoomMessage;
import io.github.ma1uta.matrix.event.content.RoomMessageContent;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
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
            client = new MatrixClient.Builder()
                .homeserver(config.getServerUrl())
                .accessToken(config.getBotAccessToken())
                .build();

            this.accessToken = config.getBotAccessToken();

            // Verify the token is valid by trying to get account data
            client.auth().whoami().join();
            
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
            String roomId = createOrGetDirectMessageRoom(matrixUserId);
            
            // Create message content
            RoomMessageContent content = new RoomMessageContent();
            content.setBody(otp);
            content.setMsgtype(RoomMessage.TEXT);

            // Send the message
            client.event().sendMessage(roomId, content).join();
            
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

    private String createOrGetDirectMessageRoom(String userId) throws MatrixMessageException {
        try {
            // First try to find existing direct message room
            List<String> rooms = client.room().joinedRooms().join();
            
            for (String roomId : rooms) {
                Map<String, Object> members = client.room().joinedMembers(roomId).join();
                if (members.size() == 2 && members.containsKey(userId)) {
                    return roomId;
                }
            }

            // If no existing room found, create a new one
            CreateRoomRequest createRequest = new CreateRoomRequest();
            createRequest.setDirect(true);
            createRequest.setInvite(List.of(userId));
            createRequest.setPresetEnum(CreateRoomRequest.Preset.PRIVATE_CHAT);

            return client.room().createRoom(createRequest).join().getRoomId();
        } catch (CompletionException e) {
            throw new MatrixMessageException("Failed to create or get direct message room", e);
        }
    }
}
package org.keycloak.matrix;

import io.github.ma1uta.matrix.client.MatrixClient;
import io.github.ma1uta.matrix.client.api.AuthApi;
import io.github.ma1uta.matrix.client.api.EventApi;
import io.github.ma1uta.matrix.client.api.RoomApi;
import io.github.ma1uta.matrix.client.model.auth.WhoamiResponse;
import io.github.ma1uta.matrix.client.model.room.CreateRoomRequest;
import io.github.ma1uta.matrix.client.model.room.CreateRoomResponse;
import io.github.ma1uta.matrix.event.content.RoomMessageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatrixServiceImplTest {

    @Mock
    private MatrixClient matrixClient;
    @Mock
    private AuthApi authApi;
    @Mock
    private RoomApi roomApi;
    @Mock
    private EventApi eventApi;

    private MatrixServiceImpl matrixService;
    private MatrixConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        matrixService = new MatrixServiceImpl();

        // Setup basic configuration
        Map<String, String> configMap = new HashMap<>();
        configMap.put(MatrixConfig.MATRIX_SERVER_URL, "https://matrix.org");
        configMap.put(MatrixConfig.BOT_USER_ID, "@bot:matrix.org");
        configMap.put(MatrixConfig.BOT_ACCESS_TOKEN, "test_token");
        config = new MatrixConfig(createConfigModel(configMap));

        // Setup mock responses
        WhoamiResponse whoami = new WhoamiResponse();
        whoami.setUserId("@bot:matrix.org");
        when(authApi.whoami()).thenReturn(CompletableFuture.completedFuture(whoami));
        when(matrixClient.auth()).thenReturn(authApi);
        when(matrixClient.room()).thenReturn(roomApi);
        when(matrixClient.event()).thenReturn(eventApi);
    }

    @Test
    void initialize_shouldSucceed() throws MatrixInitializationException {
        // Act
        matrixService.initialize(config);

        // Assert
        assertTrue(matrixService.isInitialized(), "Service should be initialized");
    }

    @Test
    void initialize_shouldThrowExceptionOnFailure() {
        // Arrange
        when(authApi.whoami()).thenReturn(CompletableFuture.failedFuture(
            new RuntimeException("Failed to connect")));

        // Act & Assert
        assertThrows(MatrixInitializationException.class, () -> matrixService.initialize(config),
            "Should throw exception when initialization fails");
        assertFalse(matrixService.isInitialized(), "Service should not be initialized after failure");
    }

    @Test
    void sendOTP_shouldSucceedWithExistingRoom() throws MatrixMessageException {
        // Arrange
        String userId = "@user:matrix.org";
        String roomId = "!room:matrix.org";
        String otp = "123456";

        when(roomApi.joinedRooms())
            .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(roomId)));

        Map<String, Object> members = new HashMap<>();
        members.put(userId, new HashMap<>());
        members.put("@bot:matrix.org", new HashMap<>());
        when(roomApi.joinedMembers(roomId))
            .thenReturn(CompletableFuture.completedFuture(members));
        when(eventApi.sendMessage(anyString(), any(RoomMessageContent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        matrixService.initialize(config);

        // Act
        matrixService.sendOTP(userId, otp);

        // Assert
        verify(eventApi).sendMessage(eq(roomId), any(RoomMessageContent.class));
    }

    @Test
    void sendOTP_shouldCreateNewRoomIfNeeded() throws MatrixMessageException {
        // Arrange
        String userId = "@user:matrix.org";
        String newRoomId = "!newroom:matrix.org";
        String otp = "123456";

        when(roomApi.joinedRooms())
            .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        CreateRoomResponse createResponse = new CreateRoomResponse();
        createResponse.setRoomId(newRoomId);
        when(roomApi.createRoom(any(CreateRoomRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(createResponse));
        when(eventApi.sendMessage(anyString(), any(RoomMessageContent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        matrixService.initialize(config);

        // Act
        matrixService.sendOTP(userId, otp);

        // Assert
        verify(roomApi).createRoom(argThat(request -> 
            request.isDirect() && 
            request.getInvite().contains(userId) &&
            request.getPreset().equals(CreateRoomRequest.Preset.PRIVATE_CHAT.name())));
        verify(eventApi).sendMessage(eq(newRoomId), any(RoomMessageContent.class));
    }

    @Test
    void sendOTP_shouldThrowExceptionWhenNotInitialized() {
        // Act & Assert
        assertThrows(MatrixMessageException.class,
            () -> matrixService.sendOTP("@user:matrix.org", "123456"),
            "Should throw exception when service is not initialized");
    }

    @Test
    void sendOTP_shouldThrowExceptionOnMessageFailure() throws MatrixInitializationException {
        // Arrange
        matrixService.initialize(config);
        when(roomApi.joinedRooms()).thenReturn(CompletableFuture.failedFuture(
            new RuntimeException("Network error")));

        // Act & Assert
        assertThrows(MatrixMessageException.class,
            () -> matrixService.sendOTP("@user:matrix.org", "123456"),
            "Should throw exception when message sending fails");
    }

    private org.keycloak.models.AuthenticatorConfigModel createConfigModel(Map<String, String> config) {
        org.keycloak.models.AuthenticatorConfigModel model = 
            new org.keycloak.models.AuthenticatorConfigModel();
        model.setConfig(config);
        return model;
    }
}
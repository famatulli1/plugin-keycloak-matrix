package org.keycloak.matrix;

import io.github.ma1uta.matrix.client.MatrixClient;
import io.github.ma1uta.matrix.client.StandaloneClient;
import io.github.ma1uta.matrix.client.api.AccountApi;
import io.github.ma1uta.matrix.client.api.EventApi;
import io.github.ma1uta.matrix.client.api.RoomApi;
import io.github.ma1uta.matrix.client.model.account.WhoAmI;
import io.github.ma1uta.matrix.client.model.room.CreateRoomResponse;
import io.github.ma1uta.matrix.client.model.room.JoinedRooms;
import io.github.ma1uta.matrix.client.model.room.RoomId;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatrixServiceImplTest {

    @Mock
    private MatrixClient matrixClient;
    @Mock
    private AccountApi accountApi;
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
        WhoAmI whoAmI = new WhoAmI();
        whoAmI.setUserId("@bot:matrix.org");
        when(accountApi.getWhoAmI()).thenReturn(CompletableFuture.completedFuture(whoAmI));
        when(matrixClient.account()).thenReturn(accountApi);
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
        when(accountApi.getWhoAmI()).thenReturn(CompletableFuture.failedFuture(
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

        JoinedRooms joinedRooms = new JoinedRooms();
        joinedRooms.setJoinedRooms(Collections.singletonList(roomId));
        when(roomApi.joinedRooms()).thenReturn(CompletableFuture.completedFuture(joinedRooms));
        when(roomApi.members(roomId)).thenReturn(CompletableFuture.completedFuture(
            createMockMembersResponse(userId)));
        when(eventApi.sendMessage(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        matrixService.sendOTP(userId, otp);

        // Assert
        verify(eventApi).sendMessage(roomId, otp);
    }

    @Test
    void sendOTP_shouldCreateNewRoomIfNeeded() throws MatrixMessageException {
        // Arrange
        String userId = "@user:matrix.org";
        String newRoomId = "!newroom:matrix.org";
        String otp = "123456";

        JoinedRooms joinedRooms = new JoinedRooms();
        joinedRooms.setJoinedRooms(Collections.emptyList());
        when(roomApi.joinedRooms()).thenReturn(CompletableFuture.completedFuture(joinedRooms));

        CreateRoomResponse createRoomResponse = new CreateRoomResponse();
        createRoomResponse.setRoomId(newRoomId);
        when(roomApi.createDirectRoom(userId))
            .thenReturn(CompletableFuture.completedFuture(createRoomResponse));
        when(eventApi.sendMessage(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        matrixService.sendOTP(userId, otp);

        // Assert
        verify(roomApi).createDirectRoom(userId);
        verify(eventApi).sendMessage(newRoomId, otp);
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

    private io.github.ma1uta.matrix.client.model.room.Members createMockMembersResponse(String userId) {
        io.github.ma1uta.matrix.client.model.room.Members members = 
            new io.github.ma1uta.matrix.client.model.room.Members();
        io.github.ma1uta.matrix.client.model.room.RoomMember member = 
            new io.github.ma1uta.matrix.client.model.room.RoomMember();
        member.setUserId(userId);
        members.setChunk(Collections.singletonList(member));
        return members;
    }

    private org.keycloak.models.AuthenticatorConfigModel createConfigModel(Map<String, String> config) {
        org.keycloak.models.AuthenticatorConfigModel model = 
            new org.keycloak.models.AuthenticatorConfigModel();
        model.setConfig(config);
        return model;
    }
}
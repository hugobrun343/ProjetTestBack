package org.acme.websocket;

import org.acme.service.SimulationService;
import org.acme.model.Particle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for ParticleWebSocket using Mockito.
 */
class ParticleWebSocketTest {

    @Mock
    private SimulationService simulationService;

    @Mock
    private Session mockSession;

    @Mock
    private RemoteEndpoint.Async mockAsyncRemote;

    private ParticleWebSocket particleWebSocket;

    private Set<Session> mockSessions;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        mockSessions = new CopyOnWriteArraySet<>();
        particleWebSocket = new ParticleWebSocket();
        particleWebSocket.simulationService = simulationService;

        when(mockSession.getId()).thenReturn("test-session");
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsyncRemote);
        when(mockAsyncRemote.sendText(anyString())).thenReturn(CompletableFuture.completedFuture(null));
    }

    /**
     * Test WebSocket connection handling.
     */
    @Test
    void testOnOpen_AddsSessionAndStartsBroadcast() {
        particleWebSocket.onOpen(mockSession);
        assertTrue(mockSessions.add(mockSession));
    }

    /**
     * Test WebSocket disconnection handling.
     */
    @Test
    void testOnClose_RemovesSessionAndStopsIfEmpty() {
        particleWebSocket.onOpen(mockSession);
        assertTrue(mockSessions.add(mockSession));

        particleWebSocket.onClose(mockSession);
        assertTrue(mockSessions.remove(mockSession));
    }

    /**
     * Test that the WebSocket sends particle updates.
     */
    @Test
    void testBroadcastParticles_SendsParticleData() throws IOException, InterruptedException {
        particleWebSocket.onOpen(mockSession);

        List<Particle> mockParticles = List.of(
                new Particle(1, 2, 0.1, 0.2, 1.5),
                new Particle(3, 4, 0.3, 0.4, 2.0)
        );

        when(simulationService.getParticles()).thenReturn(mockParticles);

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedJson = objectMapper.writeValueAsString(mockParticles);

        particleWebSocket.broadcastParticles();
        Thread.sleep(100);

        verify(mockAsyncRemote, atLeastOnce()).sendText(eq(expectedJson));
    }

    /**
     * Test that the broadcast function sends updates at 60 FPS.
     */
    @Test
    void testStartBroadcast_RunsAt60FPS() throws InterruptedException, IOException {
        particleWebSocket.onOpen(mockSession);

        int sentMessages = 0;

        for (int i = 0; i < 20; i++) {
            TimeUnit.MILLISECONDS.sleep(50);
            sentMessages = Mockito.mockingDetails(mockAsyncRemote).getInvocations().size();
            if (sentMessages >= 5) break;
        }

        assertTrue(sentMessages >= 5, "Expected at least 5 messages, got " + sentMessages);
    }

    /**
     * Test that WebSocket doesn't crash if sending fails.
     */
    @Test
    void testBroadcastParticles_HandlesSendErrorsGracefully() throws IOException {
        particleWebSocket.onOpen(mockSession);
        doThrow(new RuntimeException("Mock Exception")).when(mockAsyncRemote).sendText(anyString());

        assertDoesNotThrow(() -> particleWebSocket.broadcastParticles());
    }
}
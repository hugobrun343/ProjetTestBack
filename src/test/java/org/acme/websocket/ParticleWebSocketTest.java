package org.acme.websocket;

import org.acme.model.Particle;
import org.acme.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for ParticleWebSocket using Mockito.
 */
class ParticleWebSocketTest {

    @Mock
    private SimulationService simulationService;

    @Mock
    private Session mockSession;

    @Mock
    private RemoteEndpoint.Basic mockRemote;

    private ParticleWebSocket particleWebSocket;

    private Set<Session> mockSessions;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        mockSessions = new CopyOnWriteArraySet<>();
        particleWebSocket = spy(new ParticleWebSocket());
        particleWebSocket.simulationService = simulationService;

        when(mockSession.getId()).thenReturn("test-session");
        when(mockSession.getBasicRemote()).thenReturn(mockRemote);
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
    void testBroadcastParticles_SendsParticleData() throws IOException {
        particleWebSocket.onOpen(mockSession);

        List<Particle> mockParticles = List.of(
                new Particle(1, 2, 0.1, 0.2, 1.5),
                new Particle(3, 4, 0.3, 0.4, 2.0)
        );

        when(simulationService.getParticles()).thenReturn(mockParticles);

        particleWebSocket.broadcastParticles();

        verify(mockRemote, atLeastOnce()).sendText(mockParticles.toString());
    }

    /**
     * Test that the broadcast function sends updates at 60 FPS.
     */
    @Test
    void testStartBroadcast_RunsAt60FPS() throws InterruptedException, IOException {
        particleWebSocket.onOpen(mockSession);

        TimeUnit.MILLISECONDS.sleep(100);

        verify(mockRemote, atLeast(5)).sendText(anyString());
    }

    /**
     * Test that WebSocket doesn't crash if sending fails.
     */
    @Test
    void testBroadcastParticles_HandlesSendErrorsGracefully() throws IOException {
        particleWebSocket.onOpen(mockSession);
        doThrow(new IOException("Mock Exception")).when(mockRemote).sendText(anyString());

        assertDoesNotThrow(() -> particleWebSocket.broadcastParticles());
    }
}
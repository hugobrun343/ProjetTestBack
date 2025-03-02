package org.acme.websocket;

import org.acme.service.SimulationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WebSocket server that continuously broadcasts particle updates at 60 FPS.
 * Clients subscribing to this endpoint will receive real-time simulation data.
 */
@ApplicationScoped
@ServerEndpoint("/ws/particles")
public class ParticleWebSocket {

    @Inject
    SimulationService simulationService;

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final ReentrantLock lock = new ReentrantLock();
    private static boolean running = false;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Called when a new WebSocket connection is opened.
     * If no other connections exist, it starts broadcasting particle updates.
     *
     * @param session the WebSocket session of the new connection.
     */
    @OnOpen
    public void onOpen(Session session) {
        lock.lock();
        try {
            sessions.add(session);
            System.out.println("✅ New WebSocket connection: " + session.getId());

            if (!running) {
                startBroadcast();
                running = true;
                System.out.println("🚀 Broadcasting started!");
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called when a WebSocket connection is closed.
     * If no more active sessions remain, broadcasting stops.
     *
     * @param session the WebSocket session that was closed.
     */
    @OnClose
    public void onClose(Session session) {
        lock.lock();
        try {
            sessions.remove(session);
            System.out.println("❌ WebSocket disconnected: " + session.getId());

            if (sessions.isEmpty()) {
                running = false;
                System.out.println("🛑 No active sessions, stopping broadcast.");
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called when an error occurs on the WebSocket connection.
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("⚠️ WebSocket error on session " + session.getId() + ": " + throwable.getMessage());
    }

    /**
     * Starts the broadcasting loop, sending particle updates every 16 milliseconds (~60 FPS).
     */
    private void startBroadcast() {
        scheduler.scheduleAtFixedRate(() -> {
            lock.lock();
            try {
                if (!sessions.isEmpty() && running) {
                    broadcastParticles();
                }
            } finally {
                lock.unlock();
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    /**
     * Broadcasts the current list of particles to all connected clients.
     */
    void broadcastParticles() {
        try {
            String jsonParticles = objectMapper.writeValueAsString(simulationService.getParticles());
            synchronized (sessions) {
                for (Session session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.getAsyncRemote().sendText(jsonParticles);
                        } catch (Exception e) {
                            System.err.println("❌ Error sending message to session " + session.getId() + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error serializing particle data: " + e.getMessage());
        }
    }
}
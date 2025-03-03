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

    @OnOpen
    public void onOpen(Session session) {
        lock.lock();
        try {
            sessions.add(session);
            if (!running) {
                startBroadcast();
                running = true;
            }
        } finally {
            lock.unlock();
        }
    }

    @OnClose
    public void onClose(Session session) {
        lock.lock();
        try {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                running = false;
            }
        } finally {
            lock.unlock();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error on session " + session.getId() + ": " + throwable.getMessage());
    }

    private void startBroadcast() {
        scheduler.scheduleAtFixedRate(() -> {
            lock.lock();
            try {
                if (!sessions.isEmpty() && running) {
                    simulationService.updateSimulation();
                    broadcastParticles();
                }
            } finally {
                lock.unlock();
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    void broadcastParticles() {
        try {
            var particles = simulationService.getParticles();
            if (!particles.isEmpty()) {
                var firstParticle = particles.get(0);
                System.out.println("[SIMULATION] Timestamp: " + System.currentTimeMillis() + " First Particle: " + firstParticle);
            }
            String jsonParticles = objectMapper.writeValueAsString(particles);
            synchronized (sessions) {
                for (Session session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.getAsyncRemote().sendText(jsonParticles);
                        } catch (Exception e) {
                            System.err.println("Error sending message to session " + session.getId() + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error serializing particle data: " + e.getMessage());
        }
    }
}
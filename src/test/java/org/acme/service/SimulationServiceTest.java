package org.acme.service;

import org.acme.model.Particle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SimulationService class.
 */
public class SimulationServiceTest {

    private SimulationService simulationService;

    @BeforeEach
    void setUp() {
        simulationService = new SimulationService();
    }

    @Test
    void testAddParticle() {
        Particle p = new Particle(5, 5, 1, -1, 1);
        simulationService.addParticle(p);
        assertEquals(1, simulationService.getParticles().size());
    }

    @Test
    void testGetParticle() {
        Particle p = new Particle(10, 10, 2, 2, 2);
        simulationService.addParticle(p);
        Particle retrieved = simulationService.getParticle(0);
        assertEquals(10, retrieved.getX());
        assertEquals(10, retrieved.getY());
        assertEquals(2, retrieved.getMass());
    }

    @Test
    void testRemoveParticle() {
        Particle p1 = new Particle(0, 0, 1, 1, 1);
        Particle p2 = new Particle(5, 5, -1, -1, 1);
        simulationService.addParticle(p1);
        simulationService.addParticle(p2);

        simulationService.removeParticle(0);
        assertEquals(1, simulationService.getParticles().size());
        assertEquals(5, simulationService.getParticle(0).getX());
    }

    @Test
    void testRemoveParticleInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            simulationService.removeParticle(0);
        });
    }

    @Test
    void testGetParticleInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            simulationService.getParticle(10);
        });
    }

    @Test
    void testParticleMovement() {
        Particle p = new Particle(0, 0, 2, -1, 1);
        simulationService.addParticle(p);
        simulationService.updateSimulation();
        List<Particle> particles = simulationService.getParticles();

        assertEquals(0.02, particles.get(0).getX(), 0.001);
        assertEquals(-0.01, particles.get(0).getY(), 0.001);
    }

    @Test
    void testGravityEffectCloseToBlackHole() {
        Particle p = new Particle(1, 1, 0, 0, 1);
        simulationService.addParticle(p);

        simulationService.updateSimulation();
        Particle updated = simulationService.getParticles().get(0);

        assertTrue(updated.getX() < 1);
        assertTrue(updated.getY() < 1);
    }

    @Test
    void testGravityEffectFarFromBlackHole() {
        Particle p = new Particle(100, 100, 0, 0, 1);
        simulationService.addParticle(p);

        simulationService.updateSimulation();
        Particle updated = simulationService.getParticles().get(0);

        assertTrue(updated.getX() < 100);
        assertTrue(updated.getY() < 100);
    }

    @Test
    void testElasticCollisionBetweenTwoParticles() {
        Particle p1 = new Particle(0, 0, 1, 0, 1);
        Particle p2 = new Particle(1.0, 0, -1, 0, 1);

        simulationService.addParticle(p1);
        simulationService.addParticle(p2);

        for (int i = 0; i < 10; i++) {
            simulationService.updateSimulation();
        }

        Particle updatedP1 = simulationService.getParticles().get(0);
        Particle updatedP2 = simulationService.getParticles().get(1);

        assertTrue(updatedP1.getVx() < 0);
        assertTrue(updatedP2.getVx() > 0);
    }

    @Test
    void testMultipleCollisionsInOneUpdate() {
        Particle p1 = new Particle(0, 0, 1, 0, 1);
        Particle p2 = new Particle(1.5, 0, -1, 0, 1);
        Particle p3 = new Particle(0, 1.5, 0, -1, 1);

        simulationService.addParticle(p1);
        simulationService.addParticle(p2);
        simulationService.addParticle(p3);

        simulationService.updateSimulation();

        Particle updatedP1 = simulationService.getParticles().get(0);
        Particle updatedP2 = simulationService.getParticles().get(1);
        Particle updatedP3 = simulationService.getParticles().get(2);

        assertNotEquals(1, updatedP1.getVx());
        assertNotEquals(-1, updatedP2.getVx());
        assertNotEquals(-1, updatedP3.getVy());
    }

    @Test
    void testPlayPauseFunctionality() {
        Particle p = new Particle(0, 0, 1, 1, 1);
        simulationService.addParticle(p);

        simulationService.togglePlayPause();
        simulationService.updateSimulation();

        List<Particle> particles = simulationService.getParticles();
        assertEquals(0, particles.get(0).getX(), 0.001);
        assertEquals(0, particles.get(0).getY(), 0.001);
    }

    @Test
    void testResetSimulation() {
        Particle p = new Particle(0, 0, 1, 1, 1);
        simulationService.addParticle(p);

        simulationService.resetSimulation();
        assertEquals(0, simulationService.getParticles().size());
    }

    @Test
    void testLargeNumberOfParticles() {
        for (int i = 0; i < 1000; i++) {
            simulationService.addParticle(new Particle(i, i, 1, 1, 1));
        }

        simulationService.updateSimulation();
        assertEquals(1000, simulationService.getParticles().size());
    }
}
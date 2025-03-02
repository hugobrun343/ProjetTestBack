package org.acme.service;

import org.acme.model.Particule;
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
        Particule p = new Particule(0, 0, 1, 1, 1);
        simulationService.addParticule(p);
        assertEquals(1, simulationService.getParticules().size());
    }

    @Test
    void testParticleMovement() {
        Particule p = new Particule(0, 0, 2, -1, 1);
        simulationService.addParticule(p);
        simulationService.updateSimulation();
        List<Particule> particles = simulationService.getParticules();

        assertEquals(0.02, particles.get(0).getX(), 0.001);
        assertEquals(-0.01, particles.get(0).getY(), 0.001);
    }

    @Test
    void testGravityEffectCloseToBlackHole() {
        Particule p = new Particule(1, 1, 0, 0, 1);
        simulationService.addParticule(p);

        simulationService.updateSimulation();
        Particule updated = simulationService.getParticules().get(0);

        assertTrue(updated.getX() < 1);
        assertTrue(updated.getY() < 1);
    }

    @Test
    void testGravityEffectFarFromBlackHole() {
        Particule p = new Particule(100, 100, 0, 0, 1);
        simulationService.addParticule(p);

        simulationService.updateSimulation();
        Particule updated = simulationService.getParticules().get(0);

        assertTrue(updated.getX() < 100);
        assertTrue(updated.getY() < 100);
    }

    @Test
    void testElasticCollisionBetweenTwoParticles() {
        Particule p1 = new Particule(0, 0, 1, 0, 1);
        Particule p2 = new Particule(1.0, 0, -1, 0, 1);

        simulationService.addParticule(p1);
        simulationService.addParticule(p2);

        for (int i = 0; i < 10; i++) {
            simulationService.updateSimulation();
        }

        Particule updatedP1 = simulationService.getParticules().get(0);
        Particule updatedP2 = simulationService.getParticules().get(1);

        assertTrue(updatedP1.getVx() < 0);
        assertTrue(updatedP2.getVx() > 0);
    }

    @Test
    void testMultipleCollisionsInOneUpdate() {
        Particule p1 = new Particule(0, 0, 1, 0, 1);
        Particule p2 = new Particule(1.5, 0, -1, 0, 1);
        Particule p3 = new Particule(0, 1.5, 0, -1, 1);

        simulationService.addParticule(p1);
        simulationService.addParticule(p2);
        simulationService.addParticule(p3);

        simulationService.updateSimulation();

        Particule updatedP1 = simulationService.getParticules().get(0);
        Particule updatedP2 = simulationService.getParticules().get(1);
        Particule updatedP3 = simulationService.getParticules().get(2);

        assertNotEquals(1, updatedP1.getVx());
        assertNotEquals(-1, updatedP2.getVx());
        assertNotEquals(-1, updatedP3.getVy());
    }

    @Test
    void testPlayPauseFunctionality() {
        Particule p = new Particule(0, 0, 1, 1, 1);
        simulationService.addParticule(p);

        simulationService.togglePlayPause();
        simulationService.updateSimulation();

        List<Particule> particles = simulationService.getParticules();
        assertEquals(0, particles.get(0).getX(), 0.001);
        assertEquals(0, particles.get(0).getY(), 0.001);
    }

    @Test
    void testResetSimulation() {
        Particule p = new Particule(0, 0, 1, 1, 1);
        simulationService.addParticule(p);

        simulationService.resetSimulation();
        assertEquals(0, simulationService.getParticules().size());
    }

    @Test
    void testLargeNumberOfParticles() {
        for (int i = 0; i < 1000; i++) {
            simulationService.addParticule(new Particule(i, i, 1, 1, 1));
        }

        simulationService.updateSimulation();
        assertEquals(1000, simulationService.getParticules().size());
    }
}
package org.acme.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Particule class.
 */
public class ParticleTest {

    @Test
    void testParticleInitialization() {
        Particle p = new Particle(10, 20, 3, -2, 5);
        assertEquals(10, p.getX());
        assertEquals(20, p.getY());
        assertEquals(3, p.getVx());
        assertEquals(-2, p.getVy());
        assertEquals(5, p.getMass());
    }

    @Test
    void testParticleMovement() {
        Particle p = new Particle(0, 0, 2, -1, 1);
        p.update(1); // 1 second step
        assertEquals(2, p.getX(), 0.001);
        assertEquals(-1, p.getY(), 0.001);
    }

    @Test
    void testParticleMovementWithSmallTimeStep() {
        Particle p = new Particle(0, 0, 3, 4, 1);
        p.update(0.1); // Small step
        assertEquals(0.3, p.getX(), 0.001);
        assertEquals(0.4, p.getY(), 0.001);
    }
}
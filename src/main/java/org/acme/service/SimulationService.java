package org.acme.service;

import org.acme.model.Particle;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Simulation service that handles the N-body particle system.
 * Manages particle updates, gravitational effects, collisions, play/pause, and reset functionality.
 */
@ApplicationScoped
public class SimulationService {

    @Getter
    private final List<Particle> particles = new ArrayList<>();
    private final double dt = 0.01;

    @Getter
    private boolean running = true;

    private static final double G = 10;
    private static final double BLACK_HOLE_X = 0;
    private static final double BLACK_HOLE_Y = 0;
    private static final double BLACK_HOLE_MASS = 1000;
    private static final double PARTICLE_RADIUS = 1.0;

    /**
     * Adds a new particle to the simulation.
     *
     * @param p the particle to add
     */
    public void addParticle(Particle p) {
        particles.add(p);
    }

    /**
     * Retrieves a particle by its index in the list.
     *
     * @param index the index of the particle
     * @return the particle at the given index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Particle getParticle(int index) {
        if (index < 0 || index >= particles.size()) {
            throw new IndexOutOfBoundsException("Invalid particle index: " + index);
        }
        return particles.get(index);
    }

    /**
     * Removes a particle from the simulation by index.
     *
     * @param index the index of the particle to remove
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public void removeParticle(int index) {
        if (index < 0 || index >= particles.size()) {
            throw new IndexOutOfBoundsException("Invalid particle index: " + index);
        }
        particles.remove(index);
    }

    /**
     * Updates the simulation by applying gravity, detecting collisions, and computing new positions.
     *
     * @return the list of particles after the update
     */
    public List<Particle> updateSimulation() {
        if (running) {
            for (Particle p : particles) {
                applyGravity(p);
                p.update(dt);
            }
            detectAndResolveCollisions();
        }
        return particles;
    }

    /**
     * Applies the gravitational force of the black hole to a given particle.
     *
     * @param p the particle to be affected by gravity
     */
    private void applyGravity(Particle p) {
        double dx = BLACK_HOLE_X - p.getX();
        double dy = BLACK_HOLE_Y - p.getY();
        double distanceSquared = dx * dx + dy * dy;
        double distance = Math.sqrt(distanceSquared);

        // Prevents numerical explosion if a particle is too close to the black hole
        if (distance < 1) return;

        // Gravitational force: F = G * (m1 * m2) / r²
        double force = (G * BLACK_HOLE_MASS * p.getMass()) / distanceSquared;
        double ax = force * (dx / distance) / p.getMass();
        double ay = force * (dy / distance) / p.getMass();

        p.setVx(p.getVx() + ax * dt);
        p.setVy(p.getVy() + ay * dt);
    }

    /**
     * Detects and resolves collisions between particles using an elastic collision model.
     */
    private void detectAndResolveCollisions() {
        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p1 = particles.get(i);
                Particle p2 = particles.get(j);

                double dx = p1.getX() - p2.getX();
                double dy = p1.getY() - p2.getY();
                double distanceSquared = dx * dx + dy * dy;
                double distance = Math.sqrt(distanceSquared);

                if (distance < PARTICLE_RADIUS * 2) {
                    resolveCollision(p1, p2);
                }
            }
        }
    }

    /**
     * Resolves a collision between two particles using elastic collision physics.
     *
     * @param p1 the first particle
     * @param p2 the second particle
     */
    private void resolveCollision(Particle p1, Particle p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) return; // Avoid division by zero

        double nx = dx / distance;
        double ny = dy / distance;
        double vxRelative = p2.getVx() - p1.getVx();
        double vyRelative = p2.getVy() - p1.getVy();

        double velocityAlongNormal = vxRelative * nx + vyRelative * ny;

        // If particles are moving apart, no need to resolve collision
        if (velocityAlongNormal > 0) return;

        double restitution = 1.0;
        double impulse = (-(1 + restitution) * velocityAlongNormal) /
                (1 / p1.getMass() + 1 / p2.getMass());

        double impulseX = impulse * nx;
        double impulseY = impulse * ny;

        p1.setVx(p1.getVx() - (impulseX / p1.getMass()));
        p1.setVy(p1.getVy() - (impulseY / p1.getMass()));

        p2.setVx(p2.getVx() + (impulseX / p2.getMass()));
        p2.setVy(p2.getVy() + (impulseY / p2.getMass()));
    }

    /**
     * Toggles the simulation between play and pause states.
     */
    public void togglePlayPause() {
        running = !running;
    }

    /**
     * Resets the simulation by removing all particles.
     */
    public void resetSimulation() {
        particles.clear();
    }
}
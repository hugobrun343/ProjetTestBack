package org.acme.service;

import org.acme.model.Particle;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Simulation service managing an N-body particle system.
 */
@ApplicationScoped
public class SimulationService {

    @Getter
    private final List<Particle> particles = new ArrayList<>();
    private final double dt = 0.01;

    @Getter
    private boolean running = true;

    private static final double G = 0.007;
    private static final double BLACK_HOLE_X = 0;
    private static final double BLACK_HOLE_Y = 0;
    private static final double BLACK_HOLE_MASS = 1000;
    private static final double PARTICLE_RADIUS = 1.0;
    private static final double MAX_FORCE = 500;
    private static final double MIN_REPULSION_DISTANCE = 5;
    private static final double REPULSION_FORCE = 18;
    private static final double SPEED_DAMPING = 1;

    /**
     * Adds a particle to the simulation.
     *
     * @param p the particle to add
     */
    public void addParticle(Particle p) {
        particles.add(p);
    }

    /**
     * Removes a particle from the simulation by index.
     *
     * @param index the index of the particle to remove
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public void removeParticle(int index) {
        if (index < 0 || index >= particles.size()) {
            throw new IndexOutOfBoundsException("Invalid particle index: " + index);
        }
        particles.remove(index);
    }

    /**
     * Retrieves a particle by its index.
     *
     * @param index the index of the particle
     * @return the particle at the given index
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public Particle getParticle(int index) {
        if (index < 0 || index >= particles.size()) {
            throw new IndexOutOfBoundsException("Invalid particle index: " + index);
        }
        return particles.get(index);
    }

    /**
     * Updates the simulation state.
     *
     * @return the updated list of particles
     */
    public List<Particle> updateSimulation() {
        if (running) {
            for (Particle p : particles) {
                applyInverseGravity(p);
                dampenSpeed(p);
                p.update(dt);
            }
            detectAndResolveCollisions();
        }
        return particles;
    }

    /**
     * Applies gravity increasing with distance, with a repulsion effect when too close.
     *
     * @param p the particle affected
     */
    private void applyInverseGravity(Particle p) {
        double dx = BLACK_HOLE_X - p.getX();
        double dy = BLACK_HOLE_Y - p.getY();
        double distanceSquared = dx * dx + dy * dy;
        double distance = Math.sqrt(distanceSquared);

        if (distance < 1) return;

        double force = G * BLACK_HOLE_MASS * p.getMass() * distance;
        force = Math.min(force, MAX_FORCE);

        if (distance < MIN_REPULSION_DISTANCE) {
            force -= REPULSION_FORCE;
        }

        double ax = (force * dx / distance) / p.getMass();
        double ay = (force * dy / distance) / p.getMass();

        p.setVx(p.getVx() + ax * dt);
        p.setVy(p.getVy() + ay * dt);
    }

    /**
     * Reduces velocity slightly to maintain orbital motion.
     *
     * @param p the particle to adjust
     */
    private void dampenSpeed(Particle p) {
        p.setVx(p.getVx() * SPEED_DAMPING);
        p.setVy(p.getVy() * SPEED_DAMPING);
    }

    /**
     * Detects and resolves collisions between particles.
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
     * Resolves a collision between two particles using elastic physics.
     *
     * @param p1 the first particle
     * @param p2 the second particle
     */
    private void resolveCollision(Particle p1, Particle p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) return;

        double nx = dx / distance;
        double ny = dy / distance;
        double vxRelative = p2.getVx() - p1.getVx();
        double vyRelative = p2.getVy() - p1.getVy();

        double velocityAlongNormal = vxRelative * nx + vyRelative * ny;

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
     * Resets the simulation by clearing all particles.
     */
    public void resetSimulation() {
        particles.clear();
    }
}
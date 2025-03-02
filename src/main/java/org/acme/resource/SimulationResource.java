package org.acme.resource;

import org.acme.model.Particle;
import org.acme.service.SimulationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * REST API for managing the simulation (Adding/Removing particles, Play/Pause, Start/Reset).
 * The actual simulation updates will be handled by WebSockets.
 */
@Path("/simulation")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class SimulationResource {

    @Inject
    SimulationService simulationService;

    /**
     * Adds a new particle to the simulation.
     *
     * @param p the particle to add
     * @return A success message
     */
    @POST
    @Path("/add")
    public String addParticle(Particle p) {
        simulationService.addParticle(p);
        return "Particle added successfully";
    }

    /**
     * Removes a particle from the simulation by its index.
     *
     * @param index the index of the particle to remove
     * @return A success message, or an error message if the index is invalid
     */
    @DELETE
    @Path("/remove/{index}")
    public String removeParticle(@PathParam("index") int index) {
        try {
            simulationService.removeParticle(index);
            return "Particle removed successfully";
        } catch (IndexOutOfBoundsException e) {
            return "Invalid particle index: " + index;
        }
    }

    /**
     * Starts the simulation by adding a given number of randomly generated particles.
     *
     * @param numParticles Number of particles to add.
     * @return A success message
     */
    @POST
    @Path("/start/{numParticles}")
    public String startSimulation(@PathParam("numParticles") int numParticles) {
        if (numParticles <= 0) {
            return "Invalid number of particles: " + numParticles;
        }

        simulationService.resetSimulation();
        for (int i = 0; i < numParticles; i++) {
            double x = Math.random() * 100 - 50;
            double y = Math.random() * 100 - 50;
            double vx = Math.random() * 2 - 1;
            double vy = Math.random() * 2 - 1;
            double mass = Math.random() * 10 + 1;

            simulationService.addParticle(new Particle(x, y, vx, vy, mass));
        }

        return "Simulation started with " + numParticles + " particles.";
    }

    /**
     * Toggles the simulation between play and pause states.
     *
     * @return The new simulation state
     */
    @POST
    @Path("/toggle")
    public String togglePlayPause() {
        simulationService.togglePlayPause();
        return "Simulation is now: " + (simulationService.isRunning() ? "Running" : "Paused");
    }

    /**
     * Resets the simulation by removing all particles.
     *
     * @return A success message
     */
    @POST
    @Path("/reset")
    public String resetSimulation() {
        simulationService.resetSimulation();
        return "Simulation reset successfully";
    }

    @GET
    @Path("/state")
    public List<Particle> getSimulationState() {
        return simulationService.getParticles();
    }
}
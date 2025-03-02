package org.acme.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.model.Particle;
import org.acme.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class SimulationResourceTest {

    @Inject
    SimulationService simulationService;

    @BeforeEach
    void setUp() {
        simulationService.resetSimulation();
    }

    @Test
    void testAddSingleParticle() {
        Particle p = new Particle(5, 5, 1, -1, 1);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .body(p)
                .when()
                .post("/simulation/add")
                .then()
                .statusCode(200)
                .body(equalTo("Particle added successfully"));
    }

    @Test
    void testAddMultipleParticles() {
        for (int i = 0; i < 5; i++) {
            Particle p = new Particle(i, i, i * 0.5, i * -0.5, 1);
            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.TEXT)
                    .body(p)
                    .when()
                    .post("/simulation/add")
                    .then()
                    .statusCode(200)
                    .body(equalTo("Particle added successfully"));
        }
    }

    @Test
    void testRemoveValidParticle() {
        simulationService.addParticle(new Particle(10, 10, 2, 2, 1));

        given()
                .when()
                .delete("/simulation/remove/0")
                .then()
                .statusCode(200)
                .body(equalTo("Particle removed successfully"));
    }

    @Test
    void testRemoveParticleFromEmptySimulation() {
        given()
                .when()
                .delete("/simulation/remove/0")
                .then()
                .statusCode(200) // Modification : plus de `Response.Status.BAD_REQUEST`
                .body(equalTo("Invalid particle index: 0"));
    }

    @Test
    void testRemoveNegativeIndex() {
        given()
                .when()
                .delete("/simulation/remove/-1")
                .then()
                .statusCode(200)
                .body(equalTo("Invalid particle index: -1"));
    }

    @Test
    void testRemoveOutOfBoundsIndex() {
        simulationService.addParticle(new Particle(0, 0, 1, 1, 1));

        given()
                .when()
                .delete("/simulation/remove/5")
                .then()
                .statusCode(200)
                .body(equalTo("Invalid particle index: 5"));
    }

    /**
     * Tests starting the simulation with a valid number of particles.
     */
    @Test
    void testStartSimulationValid() {
        int numParticles = 10;

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .post("/simulation/start/" + numParticles)
                .then()
                .statusCode(200)
                .body(equalTo("Simulation started with " + numParticles + " particles."));

        String response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .get("/simulation/state")
                .then()
                .statusCode(200)
                .extract().asString(); // ✅ On extrait la réponse en texte

        int particleCount = response.split(",").length; // ✅ Compter les particules retournées
        assert particleCount == numParticles : "Expected " + numParticles + " particles but got " + particleCount;
    }

    /**
     * Tests starting the simulation with zero particles (should return an error).
     */
    @Test
    void testStartSimulationZeroParticles() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .post("/simulation/start/0")
                .then()
                .statusCode(200)
                .body(equalTo("Invalid number of particles: 0"));

        String response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .get("/simulation/state")
                .then()
                .statusCode(200)
                .extract().asString();

        assert response.isEmpty() || response.equals("[]") : "Expected an empty simulation state but got: " + response;
    }

    /**
     * Tests starting the simulation with a negative number of particles.
     */
    @Test
    void testStartSimulationNegativeParticles() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .post("/simulation/start/-5")
                .then()
                .statusCode(200)
                .body(equalTo("Invalid number of particles: -5"));

        String response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .get("/simulation/state")
                .then()
                .statusCode(200)
                .extract().asString();

        assert response.isEmpty() || response.equals("[]") : "Expected an empty simulation state but got: " + response;
    }

    @Test
    void testTogglePlayPause() {
        boolean initialState = simulationService.isRunning();

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .post("/simulation/toggle")
                .then()
                .statusCode(200)
                .body(equalTo("Simulation is now: " + (initialState ? "Paused" : "Running")));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .post("/simulation/toggle")
                .then()
                .statusCode(200)
                .body(equalTo("Simulation is now: " + (initialState ? "Running" : "Paused")));
    }

    @Test
    void testResetSimulation() {
        simulationService.addParticle(new Particle(5, 5, 1, 1, 1));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.TEXT)
                .when()
                .post("/simulation/reset")
                .then()
                .statusCode(200)
                .body(equalTo("Simulation reset successfully"));
    }
}
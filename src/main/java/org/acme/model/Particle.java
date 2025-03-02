package org.acme.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Particle {
    private double x, y;
    private double vx, vy;
    private double mass;

    public Particle(double x, double y, double vx, double vy, double mass) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.mass = mass;
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
    }
}
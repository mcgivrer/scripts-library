package com.snapgames.demo.oneoone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTest {

    @Test
    public void testIsIntersect_overlap() {
        App.Entity<?> a = new App.Entity<>();
        a.setPosition(0, 0);
        a.setSize(10, 10);

        App.Entity<?> b = new App.Entity<>();
        b.setPosition(5, 5);
        b.setSize(10, 10);

        assertTrue(a.isIntersect(b), "Expected A and B to intersect (overlap)");
        assertTrue(b.isIntersect(a), "Intersection should be symmetric");
    }

    @Test
    public void testIsIntersect_noOverlap() {
        App.Entity<?> a = new App.Entity<>();
        a.setPosition(0, 0);
        a.setSize(10, 10);

        App.Entity<?> b = new App.Entity<>();
        b.setPosition(20, 20);
        b.setSize(5, 5);

        assertFalse(a.isIntersect(b), "Expected A and B to not intersect (far apart)");
    }

    @Test
    public void testIsIntersect_touchingEdges() {
        App.Entity<?> a = new App.Entity<>();
        a.setPosition(0, 0);
        a.setSize(10, 10);

        App.Entity<?> b = new App.Entity<>();
        b.setPosition(10, 0);
        b.setSize(5, 5); // touching on the right edge

        // With the current AABB implementation, touching edges should NOT count as intersection
        assertFalse(a.isIntersect(b), "Expected touching edges NOT to count as intersection");
    }

    @Test
    public void testUpdate_positionChangedByVelocity() {
        App.Entity<?> e = new App.Entity<>();
        e.setPosition(0, 0);
        e.setVelocity(100f, 50f); // pixels per second

        // simulate 500 ms elapsed
        e.update(500);

        // x should be 100 * 0.5 = 50, y should be 50 * 0.5 = 25
        assertEquals(50f, e.x, 0.0001f, "X position should have moved by vx * elapsed/1000");
        assertEquals(25f, e.y, 0.0001f, "Y position should have moved by vy * elapsed/1000");
    }
}

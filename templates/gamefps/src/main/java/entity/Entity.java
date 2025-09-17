package {{BASE_PACKAGE}}.entity;

import {{BASE_PACKAGE}}.behaviors.Behavior;
import {{BASE_PACKAGE}}.utils.Node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple entity class for game objects.
 */
public class Entity extends Node<Entity> {

    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;

    public float dx = 0.0f;
    public float dy = 0.0f;
    public float dz = 0.0f;

    // Angles d'orientation (en radians)
    protected float rotX = 0.0f;
    protected float rotY = 0.0f;
    protected float rotZ = 0.0f;

    public float friction = 1.0f;
    public float elasticity = 1.0f;
    public float mass = 1.0f;

    public int width = 1;
    public int height = 1;
    public int depth = 1;
    public Color color;
    public Color fillColor;

    public BufferedImage sprite;

    public List<Behavior<Entity>> behaviors = new LinkedList<>();

    /**
     * Creates a new entity with the specified name.
     *
     * @param name
     */
    public Entity(String name) {
        super(name);
    }

    /**
     * Creates a new entity with the specified parameters.
     *
     * @param name
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Entity(String name, int x, int y, int width, int height) {
        this(name);
        this.x = x;
        this.y = y;
        this.z = 0;
        this.width = width;
        this.height = height;
        this.depth = 1;
        this.color = Color.WHITE;
        this.fillColor = Color.BLUE;
    }

    /**
     * Creates a new entity with the specified parameters.
     *
     * @param name
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Entity(String name, int x, int y, int z, int width, int height, int depth) {
        this(name);
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.color = Color.WHITE;
        this.fillColor = Color.BLUE;
    }

    /**
     * Test id 2 entities intersects.
     *
     * @param other the other entity to test against.
     * @return true if the entities intersect, false otherwise.
     */
    public boolean intersects(Entity other) {
        return this.x < other.x + other.width && this.x + this.width > other.x &&
                this.y < other.y + other.height && this.y + this.height > other.y &&
                this.z < other.z + other.depth && this.z + this.depth > other.z;
    }

    /**
     * Draw the entity using the provided Graphics2D context.
     *
     * @param g the Graphics2D context to draw on.
     */
    public void draw(Graphics2D g) {
        // Application de la rotation autour du centre de l'entité (X, Y, Z)
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + width / 2.0, y + height / 2.0);
        // Rotation Z (autour de l'écran, 2D)
        g2.rotate(rotZ);
        // Pour X et Y, il faudrait une vraie transformation 3D (non supportée nativement par Graphics2D)
        // On peut simuler un effet 3D basique par une rotation sur X/Y via une transformation affine
        // mais cela ne donne pas un rendu 3D réaliste. Ici, on applique une simple inclinaison.
        if (rotX != 0.0f || rotY != 0.0f) {
            double shearX = Math.tan(rotY);
            double shearY = Math.tan(rotX);
            g2.shear(shearX, shearY);
        }
        g2.translate(-width / 2.0, -height / 2.0);
        if (sprite != null) {
            g2.drawImage(sprite, 0, 0, width, height, null);
        } else {
            g2.setColor(fillColor);
            g2.fillRect(0, 0, width, height);
            g2.setColor(color);
            g2.drawRect(0, 0, width, height);
        }
        g2.dispose();
    }

    /**
     * Update the entity's position based on its velocity.
     */
    public void update(long elapsed) {
        for (Behavior<Entity> b : behaviors) {
            b.apply(this, elapsed);
        }
        x += dx * elapsed;
        y += dy * elapsed;
        z += dz * elapsed;
    }

    /**
     * Set the sprite for the entity and update its width and height accordingly.
     *
     * @param sprite the BufferedImage sprite to set.
     * @return the updated entity.
     */
    public Entity setSprite(BufferedImage sprite) {
        this.sprite = sprite;
        this.width = sprite.getWidth();
        this.height = sprite.getHeight();
        // La profondeur n'est pas définie par le sprite, on laisse depth inchangé
        return this;
    }

    /**
     * Set the velocity of the entity.
     *
     * @param dx the change in x position
     * @param dy the change in y position
     * @return the updated entity
     */
    public Entity setVelocity(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
        this.dz = 0;
        return this;

    }

    /**
     * Définit la vélocité sur les 3 axes.
     */
    public Entity setVelocity(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        return this;
    }

    /**
     * Set the position of the entity.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the updated entity
     */
    public Entity setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.z = 0;
        return this;

    }

    /**
     * Définit la position sur les 3 axes.
     */
    public Entity setPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Set the color of the entity.
     *
     * @param color the color to set
     * @return the updated entity
     */
    public Entity setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * Set the fill color of the entity.
     *
     * @param color the fill color to set
     * @return the updated entity
     */
    public Entity setFillColor(Color color) {
        this.fillColor = color;
        return this;
    }

    public Entity addBehavior(Behavior<Entity> behavior) {
        this.behaviors.add(behavior);
        return this;
    }

    /**
     * set Entity size.
     *
     * @param width  Entity width
     * @param height Entity height
     * @return
     */
    public Entity setSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.depth = 1;
        return this;

    }

    /**
     * Définit la taille sur les 3 axes.
     */
    public Entity setSize(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        return this;
    }

    /**
     * Set the Elasticity physic factor.
     *
     * @param e the elasticity factor (from 0.0f to 1.0f)
     * @return the updated Entity.
     */
    public Entity setElasticity(float e) {
        this.elasticity = e;
        return this;
    }

    /**
     * Set the Friction physic factor.
     *
     * @param f the friction factor (from 0.0f to 1.0f)
     * @return the updated Entity.
     */
    public Entity setFriction(float f) {
        this.friction = f;
        return this;
    }

    /**
     * Set the Entity mass.
     *
     * @param m the mass factor (1.0f to ...)
     * @return the updated Entity.
     */
    public Entity setMass(float m) {
        this.mass = m;
        return this;
    }

    /**
     * Définit l'orientation de l'entité (en radians).
     */
    public Entity setRotation(float rotX, float rotY, float rotZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        return this;
    }

    public float getRotX() {
        return rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public float getRotZ() {
        return rotZ;
    }
}

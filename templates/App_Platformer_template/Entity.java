package ${PROJECT_PACKAGE_NAME};

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * A simple entity class for game objects.
 */
public class Entity {
    public static long index = 0;
    public long id = index++;
    public String name = "noname_%d".formatted(this.id);
    public float x;
    public float y;

    public float dx;
    public float dy;

    public int width;
    public int height;
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
        this.name = name;
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
        this.width = width;
        this.height = height;
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
                this.y < other.y + other.height && this.y + this.height > other.y;
    }

    /**
     * Draw the entity using the provided Graphics2D context.
     * 
     * @param g the Graphics2D context to draw on.
     */
    public void draw(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, (int) x, (int) y, width, height, null);
        } else {
            g.setColor(fillColor);
            g.fillRect((int) x, (int) y, width, height);
            g.setColor(color);
            g.drawRect((int) x, (int) y, width, height);
        }
    }

    /**
     * Update the entity's position based on its velocity.
     */
    public void update() {
        for (Behavior<Entity> b : behaviors) {
            b.apply(this);
        }
        x += dx;
        y += dy;
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
        return this;
    }

}
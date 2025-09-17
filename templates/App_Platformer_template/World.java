package ${PROJECT_PACKAGE_NAME};

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A simple world class representing the game world boundaries.
 */
public class World extends Entity {
    public float gravity = 0.981f;
    private Color colorGround = Color.GREEN.darker().darker();

    /**
     * Creates a new world with the specified width and height.
     * 
     * @param width  width of the World
     * @param height heoght of the world
     */
    public World(int width, int height) {
        super("world", 0, 0, width, height);
        this.color = Color.DARK_GRAY;
        this.fillColor = Color.CYAN;
    }

    /**
     * Check if the entity is within the world boundaries.
     * 
     * @param e the entity to check
     * @return true if the entity is within the world, false otherwise
     */
    public boolean contains(Entity e) {
        return e.x >= this.x && e.x + e.width <= this.x + this.width &&
                e.y >= this.y && e.y + e.height <= this.y + this.height;
    }

    /**
     * Clamp the entity's position within the world boundaries.
     * 
     * @param e the entity to clamp
     */
    public void clamp(Entity e) {
        if (e.x < this.x) {
            e.x = this.x;
        } else if (e.x + e.width > this.x + this.width) {
            e.x = this.x + this.width - e.width;
        }
        if (e.y < this.y) {
            e.y = this.y;
        } else if (e.y + e.height > this.y + this.height) {
            e.y = this.y + this.height - e.height;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(colorGround);
        g.fillRect(-this.width, this.height, this.width * 3, this.height);
    }

}
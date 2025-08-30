package ${PROJECT_PACKAGE_NAME};

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFrame;

/**
 * ${PROJECT_MAIN_CLASS_NAME} serves as a basic demonstration of how to build a graphical application
 * using Java AWT and manage a custom rendering loop with double buffering.
 * This application displays the elapsed time in a `Frame` window with support for
 * basic keyboard interaction to terminate the program.
 * <p>
 * The class also provides mechanisms to manage window events, custom rendering to a
 * buffered image, and drawing text with anti-aliasing enabled.
 * <p>
 * Implements the `KeyListener` interface to handle keyboard events for user interaction.
 *
 * @author ${GIT_AUTHOR_NAME} <${GIT_AUTHOR_EMAIL}>
 * @version ${PROJECT_APP_VERSION}
 */
public class ${PROJECT_MAIN_CLASS_NAME} implements KeyListener {

  /**
   * The ${PROJECT_MAIN_CLASS_NAME} modes.
   */
  public enum AppMode {
    /**
     * Development mode.
     * Used for debugging and development purposes.
     */
    public interface Behavior<Entity> {
        default void init(${PROJECT_MAIN_CLASS_NAME} app, Entity e) {
        }

        default void update(${PROJECT_MAIN_CLASS_NAME} app, long elapsed, Entity e) {
        }

        default void draw(${PROJECT_MAIN_CLASS_NAME} app, Graphics2D g, Entity e) {
        }

        default void dispose(${PROJECT_MAIN_CLASS_NAME} app, Entity e) {
        }
    }

    /**
     * Production mode.
     * Used for running the ${PROJECT_MAIN_CLASS_NAME} in a production environment.
     */
    PRODUCTION
  }

  /**
   * Text alignment options.
   */
  public enum TextAlign {
    LEFT, CENTER, RIGHT
  }

  /**
   * A simple circular queue implementation using LinkedList.
   */
  public class CircularQueue<E> extends LinkedList<E> {
    private final int capacity;

        public boolean jump;
        public boolean onGround;

        public Color color = Color.BLACK, fillColor = Color.RED;
        public BufferedImage image;

        private Material material = Material.DEFAULT;

        private Entity parent;
        private List<Entity> children = new ArrayList<>();
        private int contact = 0;
        private List<Point2D> forces = new ArrayList<>();

        public Map<String, Object> properties = new ConcurrentHashMap<>();

        public Entity(double x, double y, double width, double height) {
            super(x, y, width, height);
        }

        public Entity(String name) {
            this(0, 0, 0, 0);
            this.name = name;
        }

        // ... autres méthodes existantes ...

        public Entity setMass(double mass) {
            this.mass = Math.max(0.1, mass); // Minimum mass to avoid division by zero
            return this;
        }

        public double getMass() {
            return mass;
        }


        public Entity add(Entity child) {
            child.parent = this;
            children.add(child);
            return this;
        }

        public Entity apply(Point2D f) {
            forces.add(f);
            return this;
        }

        public Entity add(Behavior<Entity> b) {
            behaviors.add(b);
            return this;
        }

        /**
         * Gets the WaveBehavior if present on this entity.
         */
        public Behavior findBehavior(Class<?> fb) {
            return behaviors.stream()
                    .filter(b -> b.getClass().equals(fb))
                    .map(b -> b)
                    .findFirst()
                    .orElse(null);
        }

        public Entity setPosition(double x, double y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Entity setSize(double width, double height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Entity setColor(Color color) {
            this.color = color;
            return this;
        }

        public Entity setFillColor(Color color) {
            this.fillColor = color;
            return this;
        }

        public Entity setActive(boolean active) {
            this.active = active;
            return this;
        }

        public Entity setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public Entity setPhysicType(PhysicType physicType) {
            this.physicType = physicType;
            return this;
        }

        public Entity setProperty(String name, Object value) {
            properties.put(name, value);
            return this;
        }

        public Object getProperty(String name) {
            return properties.get(name);
        }

        /**
         * Draws the current entity onto the provided graphics context.
         * This method renders the entity with its current position, rotation, colors, and image.
         * If an image is associated with the entity, it will be drawn at the specified position.
         * Otherwise, the entity will be rendered as a filled and outlined shape.
         *
         * @param g the graphics context on which the entity will be drawn
         */
        public void draw(Graphics2D g) {
            g.rotate(r, x, y);
            if (image != null) {
                g.drawImage(image, (int) x, (int) y, null);
            } else {
                if (this.fillColor != null) {
                    g.setColor(this.fillColor);
                    g.fill(this);
                }
                if (this.color != null) {
                    g.setColor(this.color);
                    g.draw(this);
                }
            }
        }

        public String[] getDebugInfo() {
            return new String[]{
                    "id: " + id,
                    "name: " + name,
                    "gnd:" + onGround,
                    "cnt:" + contact,
                    "jmp:" + jump,
                    "s: %3.2fx%3.2f".formatted(width, height),
                    "p: %3.2f,%3.2f".formatted(x, y),
                    "r: %3.2f".formatted(r),
                    "v: %3.2f,%3.2f".formatted(dx, dy),
                    "dr: %3.2f".formatted(r),
            };
        }

        public void drawDebug(Graphics2D g) {
            g.setColor(Color.ORANGE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 8.5f));
            if (${PROJECT_MAIN_CLASS_NAME}.debug > 0) {
                int ix = 0;
                for (String s : getDebugInfo()) {
                    if (${PROJECT_MAIN_CLASS_NAME}.debug > ix) {
                        g.drawString(s,
                                (int) (x + width + 4),
                                (int) (y + height - (g.getFontMetrics().getHeight() * ix++)));
                    }
                }
                g.setColor(Color.YELLOW);
                // draw the resulting velocity
                g.drawLine(
                        (int) (x + width / 2), (int) (y + height / 2),
                        (int) ((x + width / 2) + dx * 0.5), (int) ((y + height / 2) + dy * 0.5));
                g.setColor(Color.GREEN);
                // draw all applied forces
                getForces().forEach(f -> {
                    g.drawLine(
                            (int) (x + width / 2), (int) (y + height / 2),
                            (int) ((x + width / 2) + f.getX() * 0.5), (int) ((y + height / 2) + f.getY() * 0.5));
                });
            }
        }

        public void update(long elapsed) {

        }

        public boolean isActive() {
            return active;
        }

        public Material getMaterial() {
            return material;
        }

        public int getContact() {
            return contact;
        }

        public void addContact(int i) {
            contact += i;
        }

        public void resetContact() {
            contact = 0;
        }

        public void resetForces() {
            forces.clear();
        }

        public PhysicType getPhysicType() {
            return physicType;
        }

        public Collection<Point2D> getForces() {
            return forces;
        }

        public void removeProperty(String name) {
            properties.remove(name);
        }
    }

    @Override
    public boolean add(E e) {
      if (size() >= capacity)
        removeFirst();
      return super.add(e);
    }
  }

  /**
   * A simple entity class for game objects.
   */
  public static class Entity {
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
    public static class WaveBehavior implements Behavior<Entity> {
        private double[][] heightMap;
        private double[][] velocityMap;
        private double[][] prevHeightMap; // Pour améliorer la stabilité
        private int width, height;
        private double cellSize;
        private double time = 0;

        // Wave parameters - ajustés pour de meilleurs résultats
        private double waveAmplitude = 3.0;
        private double waveFrequency = 0.8;
        private double waveSpeed = 100.0;
        private double damping = 0.998; // Moins d'amortissement
        private double tension = 0.25; // Plus de tension pour la restauration
        private double restoreForce = 0.02; // Force de restauration vers la surface

        // Surface points for rendering
        private List<Point2D> surfacePoints;
        private double baseWaterLevel; // Niveau de référence de l'eau

        public WaveBehavior(double cellSize) {
            this.cellSize = cellSize;
            this.surfacePoints = new ArrayList<>();
        }

        @Override
        public void init(${PROJECT_MAIN_CLASS_NAME} app, Entity entity) {
            if (!entity.getMaterial().isFluid()) {
                throw new IllegalArgumentException("WaveBehavior can only be applied to fluid entities");
            }

            this.width = (int) (entity.width / cellSize) + 1;
            this.height = (int) (entity.height / cellSize) + 1;
            this.baseWaterLevel = entity.y; // Niveau de référence

            this.heightMap = new double[width][height];
            this.velocityMap = new double[width][height];
            this.prevHeightMap = new double[width][height];

            initializeWaves();
        }

        private void initializeWaves() {
            // Initialize with small random perturbations around zero
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    heightMap[x][y] = (Math.random() - 0.5) * 0.5;
                    velocityMap[x][y] = 0;
                    prevHeightMap[x][y] = heightMap[x][y];
                }
            }
        }

        @Override
        public void update(${PROJECT_MAIN_CLASS_NAME} app, long elapsed, Entity entity) {
            time += elapsed * 0.001; // Convert to seconds

            // Update wave equation
            updateWaveEquation(elapsed);

            // Generate continuous waves
            generateContinuousWaves();

            // Apply restoration force
            applyRestorationForce();

            // Update surface points for rendering
            updateSurfacePoints(entity);

            // Check for interactions with other entities
            checkEntityInteractions(app, entity);
        }

        private void updateWaveEquation(long elapsed) {
            double dt = elapsed * 0.05; // Convert to seconds
            //dt = Math.min(dt, 0.016); // Limiter dt pour la stabilité

            // Sauvegarder l'état précédent
            for (int x = 0; x < width; x++) {
                System.arraycopy(heightMap[x], 0, prevHeightMap[x], 0, height);
            }

            double[][] newHeightMap = new double[width][height];
            double[][] newVelocityMap = new double[width][height];

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    // Calculate Laplacian (second derivative) - équation des vagues 2D
                    double laplacian = (heightMap[x - 1][y] + heightMap[x + 1][y] +
                            heightMap[x][y - 1] + heightMap[x][y + 1] -
                            4 * heightMap[x][y]) / (cellSize * cellSize);

                    // Force de restauration vers le niveau de référence (0)
                    double restorationForce = -heightMap[x][y] * restoreForce;

                    // Équation des vagues avec force de restauration
                    double acceleration = tension * laplacian + restorationForce;

                    // Update velocity avec amortissement
                    newVelocityMap[x][y] = velocityMap[x][y] * damping + acceleration * dt;

                    // Update height
                    newHeightMap[x][y] = heightMap[x][y] + newVelocityMap[x][y] * dt;

                    // Limiter l'amplitude pour éviter les oscillations excessives
                    newHeightMap[x][y] = Math.max(-waveAmplitude * 2,
                            Math.min(waveAmplitude * 2, newHeightMap[x][y]));
                }
            }

            // Boundary conditions - bords fixes
            for (int x = 0; x < width; x++) {
                newHeightMap[x][0] = 0;
                newHeightMap[x][height - 1] = 0;
                newVelocityMap[x][0] = 0;
                newVelocityMap[x][height - 1] = 0;
            }
            for (int y = 0; y < height; y++) {
                newHeightMap[0][y] = 0;
                newHeightMap[width - 1][y] = 0;
                newVelocityMap[0][y] = 0;
                newVelocityMap[width - 1][y] = 0;
            }

            heightMap = newHeightMap;
            velocityMap = newVelocityMap;
        }

        private void applyRestorationForce() {
            // Force de restauration globale pour ramener les vagues vers le niveau de référence
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    // Force qui ramène progressivement vers 0
                    double restoration = -heightMap[x][y] * 0.001;
                    velocityMap[x][y] += restoration;
                }
            }
        }

        private void generateContinuousWaves() {
            // Generate waves from the left side with better amplitude control
            for (int y = 1; y < height - 1; y++) {
                double waveHeight = waveAmplitude * Math.sin(waveFrequency * time + y * 0.2);

                // Injection plus douce des vagues
                heightMap[1][y] += waveHeight * 0.05;
                velocityMap[1][y] += waveHeight * 0.02;
            }

            // Add some random disturbances - réduites
            if (Math.random() < 0.005) { // Moins fréquent
                int x = (int) (Math.random() * (width - 2)) + 1;
                int y = (int) (Math.random() * (height - 2)) + 1;
                heightMap[x][y] += (Math.random() - 0.5) * 0.5; // Amplitude réduite
            }
        }

        private void updateSurfacePoints(Entity entity) {
            surfacePoints.clear();

            // Generate surface points based on height map
            for (int x = 0; x < width; x++) {
                double worldX = entity.x + x * cellSize;

                // Prendre seulement la hauteur de la surface (y=0 dans notre grille)
                double surfaceHeight = 0;
                if (height > 0) {
                    // Moyenne des quelques premières lignes pour la surface
                    int surfaceRows = Math.min(3, height);
                    for (int y = 0; y < surfaceRows; y++) {
                        surfaceHeight += heightMap[x][y];
                    }
                    surfaceHeight /= surfaceRows;
                }

                // Position mondiale avec décalage de vague
                double worldY = baseWaterLevel + surfaceHeight;
                surfacePoints.add(new Point2D.Double(worldX, worldY));
            }
        }

        private void checkEntityInteractions(${PROJECT_MAIN_CLASS_NAME} app, Entity fluidEntity) {
            // Check interactions with other dynamic entities
            app.entities.stream()
                    .filter(e -> e != fluidEntity &&
                            e.isActive() &&
                            e.getPhysicType() == PhysicType.DYNAMIC &&
                            e.intersects(fluidEntity))
                    .forEach(entity -> {
                        // Calculate impact intensity based on velocity
                        double velocityMagnitude = Math.sqrt(entity.dx * entity.dx + entity.dy * entity.dy);
                        double impactIntensity = Math.min(velocityMagnitude * 0.2, waveAmplitude); // Limiter l'intensité

                        // Add wave disturbance at entity's position
                        if (impactIntensity > 0.1) {
                            addDisturbance(entity.getCenterX(), entity.getCenterY(), impactIntensity, fluidEntity);
                        }
                    });
        }

        @Override
        public void draw(${PROJECT_MAIN_CLASS_NAME} app, Graphics2D g, Entity entity) {
            if (surfacePoints.isEmpty()) return;

            // Draw wave surface
            g.setColor(new Color(0, 100, 255, 180));

            // Create wave path
            int[] xPoints = new int[surfacePoints.size() + 2];
            int[] yPoints = new int[surfacePoints.size() + 2];

            // Add surface points
            for (int i = 0; i < surfacePoints.size(); i++) {
                Point2D point = surfacePoints.get(i);
                xPoints[i] = (int) point.getX();
                yPoints[i] = (int) point.getY();
            }

            // Close the polygon at the bottom
            xPoints[surfacePoints.size()] = (int) (entity.x + entity.width);
            yPoints[surfacePoints.size()] = (int) (entity.y + entity.height);
            xPoints[surfacePoints.size() + 1] = (int) entity.x;
            yPoints[surfacePoints.size() + 1] = (int) (entity.y + entity.height);

            g.fillPolygon(xPoints, yPoints, xPoints.length);

            // Draw wave crests with varying intensity
            g.setColor(Color.WHITE);
            for (int i = 0; i < surfacePoints.size() - 1; i++) {
                Point2D p1 = surfacePoints.get(i);
                Point2D p2 = surfacePoints.get(i + 1);

                // Varier l'intensité selon la hauteur de la vague
                double waveHeight = Math.abs(p1.getY() - baseWaterLevel);
                int alpha = (int) Math.min(255, waveHeight * 50 + 100);
                g.setColor(new Color(255, 255, 255, alpha));

                g.drawLine((int) p1.getX(), (int) p1.getY(),
                        (int) p2.getX(), (int) p2.getY());
            }
        }

        @Override
        public void dispose(${PROJECT_MAIN_CLASS_NAME} app, Entity entity) {
            // Clean up resources
            heightMap = null;
            velocityMap = null;
            prevHeightMap = null;
            surfacePoints.clear();
        }

        /**
         * Adds a disturbance to the wave system at the specified position.
         */
        public void addDisturbance(double x, double y, double intensity, Entity fluidEntity) {
            // Convert world coordinates to grid coordinates
            int gridX = (int) ((x - fluidEntity.x) / cellSize);
            int gridY = (int) ((y - fluidEntity.y) / cellSize);

            // Add disturbance in a circular pattern
            int radius = 2; // Rayon réduit pour éviter les perturbations trop importantes
            intensity = Math.min(intensity, waveAmplitude); // Limiter l'intensité

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int nx = gridX + dx;
                    int ny = gridY + dy;

                    if (nx >= 1 && nx < width - 1 && ny >= 1 && ny < height - 1) {
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        if (distance <= radius) {
                            double factor = (radius - distance) / radius;
                            double disturbance = intensity * factor;

                            // Ajouter à la fois hauteur et vitesse pour un effet plus réaliste
                            heightMap[nx][ny] += disturbance;
                            velocityMap[nx][ny] += disturbance * 0.5;
                        }
                    }
                }
            }
        }

        /**
         * Gets the wave height at a specific world position.
         */
        public double getWaveHeightAt(double x, double y, Entity fluidEntity) {
            // Convert world coordinates to grid coordinates
            int gridX = (int) ((x - fluidEntity.x) / cellSize);
            int gridY = (int) ((y - fluidEntity.y) / cellSize);

            if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
                return heightMap[gridX][gridY];
            }
            return 0;
        }

        // Builder pattern methods for configuration
        public WaveBehavior setWaveAmplitude(double amplitude) {
            this.waveAmplitude = amplitude;
            return this;
        }

        public WaveBehavior setWaveFrequency(double frequency) {
            this.waveFrequency = frequency;
            return this;
        }

        public WaveBehavior setWaveSpeed(double speed) {
            this.waveSpeed = speed;
            return this;
        }

        public WaveBehavior setDamping(double damping) {
            this.damping = Math.max(0.9, Math.min(0.999, damping)); // Limiter l'amortissement
            return this;
        }

        public WaveBehavior setTension(double tension) {
            this.tension = Math.max(0.1, tension); // Minimum de tension
            return this;
        }

        public WaveBehavior setRestoreForce(double restoreForce) {
            this.restoreForce = restoreForce;
            return this;
        }

        // Getters
        public List<Point2D> getSurfacePoints() {
            return new ArrayList<>(surfacePoints);
        }

        public double getCellSize() {
            return cellSize;
        }

        public double getBaseWaterLevel() {
            return baseWaterLevel;
        }
    }

    /**
     * Initializes a new instance of the ${PROJECT_MAIN_CLASS_NAME} class and starts the application.
     * This constructor serves as the entry point for initializing basic setup and logging
     * the application's start.
     */
    public ${PROJECT_MAIN_CLASS_NAME}() {
        System.out.println("Start test Application...");
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

    private void initializeScene() {
        world = (World) new World("earth")
                .setGravity(new Point2D.Double(0, 98.1 * 3))
                .setSize(32 * 20, 20 * 20)
                .setColor(Color.GRAY)
                .setFillColor(Color.DARK_GRAY)
                .setMaterial(Material.AIR);
        add(world);

        // Ajouter des plateformes statiques
        Entity platform1 = new Entity("platform1")
                .setSize(128, 32)
                .setPosition(200, 400)
                .setFillColor(Color.LIGHT_GRAY)
                .setColor(Color.DARK_GRAY)
                .setPhysicType(PhysicType.STATIC)
                .setMaterial(Material.STONE);
        add(platform1);

        Entity platform2 = new Entity("platform2")
                .setSize(96, 32)
                .setPosition(400, 300)
                .setFillColor(Color.LIGHT_GRAY)
                .setColor(Color.DARK_GRAY)
                .setPhysicType(PhysicType.STATIC)
                .setMaterial(Material.STONE);
        add(platform2);


        player = new Entity("player")
                .setSize(24, 32)
                .setPosition(world.getWidth() * 0.5, world.getHeight() * 0.75)
                .setMaterial(new Material("men", 0.70, 0.88))
                .setPhysicType(PhysicType.DYNAMIC)
                .setMass(70.0)
                .add(new Behavior<Entity>() {
                    @Override
                    public void update(${PROJECT_MAIN_CLASS_NAME} app, long elapsed, Entity e) {
                        double forceStrength = 5000.0; // Force en Newtons
                        if (isKeyPressed(KeyEvent.VK_LEFT)) {
                            e.apply(new Point2D.Double(-forceStrength * 10, 0));
                        }
                        if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                            e.apply(new Point2D.Double(forceStrength * 10, 0));
                        }
                        if (isKeyPressed(KeyEvent.VK_UP) && !e.jump) {
                            e.apply(new Point2D.Double(0, -forceStrength * 300)); // Force de saut plus importante
                            e.jump = true;
                            e.onGround = false;
                        }
                        if (isKeyPressed(KeyEvent.VK_UP) && e.jump) {
                            e.apply(new Point2D.Double(0, -forceStrength * 10)); // Force de saut plus importante
                        }
                        if (isKeyPressed(KeyEvent.VK_DOWN)) {
                            e.apply(new Point2D.Double(0, forceStrength * 10)); // Force de saut plus importante
                        }
                    }

                });
        add(player);


// Bois (plus léger que l'eau - doit flotter)
        Entity woodBox = new Entity("woodBox")
                .setSize(50, 50)
                .setPosition(100, 100)
                .setFillColor(new Color(139, 69, 19))
                .setColor(Color.BLACK)
                .setMass(60.0)
                .setPhysicType(PhysicType.DYNAMIC)
                .setMaterial(new Material("wood", 0.8, 0.3, false, 600.0, 0.0)); // Densité 600 kg/m³
        add(woodBox);
// Métal (plus lourd que l'eau - doit couler)
        Entity heavyBox = new Entity("heavyBox")
                .setSize(30, 30)
                .setPosition(200, 100)
                .setFillColor(Color.GRAY)
                .setMass(200.0)
                .setColor(Color.BLACK)
                .setPhysicType(PhysicType.DYNAMIC)
                .setMaterial(new Material("metal", 0.9, 0.1, false, 2700.0, 0.0)); // Densité 2700 kg/m³
        add(heavyBox);
        // Zone d'eau
        // Dans la méthode initialize(), assurons-nous que l'eau est bien définie comme fluide :
        Entity water = new Entity("water")
                .setSize(world.getWidth(), world.getHeight() * 0.15)
                .setPosition(0, world.getHeight() * 0.85)
                .setFillColor(new Color(0, 100, 200, 100))
                .setColor(new Color(0, 130, 240, 100))
                .setPhysicType(PhysicType.STATIC)
                .setMaterial(new Material("water", 0.89, 0.0, true, 1000.0, 0.8))
                .add(new WaveBehavior(3.0)
                        .setWaveAmplitude(4.0)
                        .setWaveFrequency(0.56)
                        .setDamping(0.998)
                        .setTension(0.05));

        add(water);
        camera = (Camera) new Camera("cam01").setTarget(player).setSize(buffer.getWidth(), buffer.getHeight());
    }


    private void add(Entity e) {
        entities.add(e);
        e.behaviors.forEach(b -> b.init(this, e));
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

  /**
   * A simple world class representing the game world boundaries.
   */
  public static class World extends Entity {
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

  /**
   * Define a Behavior to be applied to any object T.
   */
  public interface Behavior<T> {
    /**
     * Apply the Behavior to the T object.
     * 
     * @param o T object instance to apply Behavior to.
     */
    void apply(T o);
  }

  /**
   * The resource bundle for internationalization.
   */
  public static ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");
  /**
   * The ${PROJECT_MAIN_CLASS_NAME} configuration properties.
   */
  public static Properties config = new Properties();
  /**
   * The debug level for the ${PROJECT_MAIN_CLASS_NAME}.
   * 0 = no debug, 1 = basic debug, 2 = detailed debug
   */
  public static int debug = 0;
  /**
   * The current ${PROJECT_MAIN_CLASS_NAME} mode.
   * Default is PRODUCTION.
   */
  public static AppMode mode = AppMode.PRODUCTION;

  /**
   * Flag to indicate if the ${PROJECT_MAIN_CLASS_NAME} should exit.
   */
  public boolean exit = false;

  /**
   * A circular queue to store recent key events.
   */
  public CircularQueue<KeyEvent> keyEvents = new CircularQueue<>(100);

  /**
   * An array to track the state of keys (pressed or not).
   */
  public boolean keys[] = new boolean[1024];

  /**
   * The main ${PROJECT_MAIN_CLASS_NAME} window.
   */
  public JFrame window;

  public World world = new World(800, 600);
  /**
   * The list of entities in the game.
   */
  public List<Entity> entities = new LinkedList<>();

  public int score = 0, life = 3;

  /**
   * Creates a new instance of the ${PROJECT_MAIN_CLASS_NAME}.
   */
  public ${PROJECT_MAIN_CLASS_NAME}() {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "Create ${PROJECT_MAIN_CLASS_NAME} '%s'", messages.getString("app.name"));
  }

  /**
   * Runs the ${PROJECT_MAIN_CLASS_NAME}.
   *
   * @param args the command-line arguments
   */
  public void run(String[] args) {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "${PROJECT_MAIN_CLASS_NAME} '%s' is running..", messages.getString("app.name"));
    for (String arg : args) {
      info(${PROJECT_MAIN_CLASS_NAME}.class, "", arg);
    }
    init(args);
    loop();
    dispose();
  }

  public void loop() {
    initialize();
    load();
    create();
    do {
      update();
      render();
    } while (!exit);
    dispose();
  }

  public void initialize() {
    if (window != null) {
      window.dispose();
    }
    window = new JFrame(messages.getString("app.name").formatted(messages.getString("app.version")));
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setPreferredSize(new Dimension(800, 600));
    window.addKeyListener(this);
    window.pack();
    window.setVisible(true);
    window.createBufferStrategy(3);
    world.setSize(window.getWidth(), window.getHeight());
  }

  private void load() {
    // load every resource you may need.
  }

  private void create() {
    entities.add(
        world
            .setSize((int) (window.getWidth() * 0.75), (int) (window.getHeight() * 0.75))
            .setPosition((int) (window.getWidth() * 0.125), (int) (window.getHeight() * 0.125)));
    // Create contextual scene.
    entities.add(
        new Entity("player", window.getWidth() / 2, window.getHeight() / 2, 24, 32)
            .setColor(Color.WHITE)
            .setFillColor(Color.GREEN)
            .addBehavior(e -> {
              float speed = 0.3f;
              float friction = 0.98f;

              if (isKeyPressed(KeyEvent.VK_LEFT)) {
                e.dx = -speed;
              } else if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                e.dx = speed;
              } else {
                e.dx = e.dx * 0.98f;
              }
              if (isKeyPressed(KeyEvent.VK_UP)) {
                e.dy = -speed;
              } else if (isKeyPressed(KeyEvent.VK_DOWN)) {
                e.dy = speed;
              } else {
                e.dy = e.dy * friction;
              }
            })
            .addBehavior(e -> {
              e.dy += world.gravity * .005f;
            }));
  }

  public void update() {
    for (Entity e : entities) {
      e.update();
      if (!world.contains(e)) {
        world.clamp(e);
      }
    }
  }

  public void render() {
    // prepare drawing graphics API
    BufferStrategy bf = window.getBufferStrategy();
    Graphics2D g = (Graphics2D) bf.getDrawGraphics();
    // set drawing configuration.
    g.setRenderingHints(
        Map.of(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
    // clear window
    g.setBackground(Color.BLACK);
    g.clearRect(0, 0, window.getWidth(), window.getHeight());

    // draw entities
    for (Entity e : entities) {
      e.draw(g);
    }

    // do you drawings
    drawText(g, "%05d".formatted(score),
        (int) (window.getWidth() - 20), 60,
        TextAlign.RIGHT,
        28.0f, Color.WHITE);
    drawText(g, "%02d".formatted(life),
        60, 60,
        TextAlign.RIGHT,
        28.0f, Color.WHITE);

    // switch buffer
    g.dispose();
    bf.show();

  }

  /**
   * Initializes the ${PROJECT_MAIN_CLASS_NAME}.
   *
   * @param args the command-line arguments
   */
  private void init(String[] args) {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "${PROJECT_MAIN_CLASS_NAME} '%s' is initializing.", messages.getString("app.name"));
    // default values
    config.put("app.config.file", "/config.properties");
    config.put("app.debug", 0);
    config.put("app.mode", AppMode.DEVELOPMENT);
    // parsing arguments
    for (String arg : args) {
      String[] kv = arg.split("=", 2);
      if (kv.length == 2) {
        config.put(kv[0], kv[1]);
        info(${PROJECT_MAIN_CLASS_NAME}.class, "Set config from argument %s = %s", kv[0], kv[1]);
      } else {
        warn(${PROJECT_MAIN_CLASS_NAME}.class, "Invalid config argument: %s", arg);
      }
      info(${PROJECT_MAIN_CLASS_NAME}.class, "", arg);
    }
    // load configuration file
    try {
      config.load(${PROJECT_MAIN_CLASS_NAME}.class.getResourceAsStream(config.getProperty("app.config.file")));
    } catch (Exception e) {
      error(${PROJECT_MAIN_CLASS_NAME}.class, "Failed to load config file: %s", e.getMessage());
    }
    // extract configuration values
    parseConfiguration(config);
  }

  /**
   * Parses the ${PROJECT_MAIN_CLASS_NAME} configuration.
   *
   * @param config the configuration properties
   */
  private void parseConfiguration(Properties config) {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "Parsing configuration.");
    for (String key : config.stringPropertyNames()) {
      String value = config.getProperty(key);
      switch (key) {
        case "app.debug":
          debug = Integer.parseInt(value);
          info(${PROJECT_MAIN_CLASS_NAME}.class, "read config '%s' = '%s'", key, value);
          break;
        case "app.mode":
          mode = AppMode.valueOf(value.toUpperCase());
          info(${PROJECT_MAIN_CLASS_NAME}.class, "read config '%s' = '%s'", key, value);
          break;
        default:
          warn(${PROJECT_MAIN_CLASS_NAME}.class, "Unknown config key: %s", key);
      }
      info(${PROJECT_MAIN_CLASS_NAME}.class, "Config '%s' = '%s'", key, value);
    }
  }

  /**
   * Disposes the ${PROJECT_MAIN_CLASS_NAME} resources.
   */
  private void dispose() {
    if (Optional.ofNullable(window).isPresent()) {
      window.dispose();
    }
    info(${PROJECT_MAIN_CLASS_NAME}.class, "${PROJECT_MAIN_CLASS_NAME} '%s' is ending.", messages.getString("app.name"));

  }

  /**
   * The main entry point for the ${PROJECT_MAIN_CLASS_NAME}.
   */
  public static void main(String[] args) {
    ${PROJECT_MAIN_CLASS_NAME} app = new ${PROJECT_MAIN_CLASS_NAME}();
    app.run(args);
  }

  /**
   * Logs a message with the specified log level.
   *
   * @param cls     the class logging the message
   * @param level   the log level
   * @param message the log message
   * @param args    optional arguments for the message
   */
  private static void log(Class<?> cls, String level, String message, Object... args) {
    System.out.printf("%s;%s;%s;%s%n", ZonedDateTime.now(), cls.getCanonicalName(), level, message.formatted(args));
  }

  /**
   * Logs an informational message.
   *
   * @param cls     the class logging the message
   * @param message the log message
   * @param args    optional arguments for the message
   */
  public static void info(Class<?> cls, String message, Object... args) {
    log(cls, "INFO", message, args);
  }

  /**
   * Logs a warning message.
   *
   * @param cls     the class logging the message
   * @param message the log message
   * @param args    optional arguments for the message
   */
  public static void warn(Class<?> cls, String message, Object... args) {
    log(cls, "WARN", message, args);
  }

  /**
   * Logs a debug message.
   *
   * @param cls        the class logging the message
   * @param debugLevel the debug level
   * @param message    the log message
   * @param args       optional arguments for the message
   */
  public static void debug(Class<?> cls, int debugLevel, String message, Object... args) {
    if (isDebugGreaterThan(debugLevel)) {
      log(cls, "DEBUG", message, args);
    }
  }

  /**
   * Logs an error message.
   *
   * @param cls     the class logging the message
   * @param message the log message
   * @param args    optional arguments for the message
   */
  public static void error(Class<?> cls, String message, Object... args) {
    log(cls, "ERROR", message, args);
  }

  /**
   * Checks if the current debug level is greater than the specified level.
   *
   * @param level the debug level to compare against
   * @return true if the current debug level is greater, false otherwise
   */
  public static boolean isDebugGreaterThan(int level) {
    return debug < level;
  }

  public void keyTyped(KeyEvent ke) {
    keyEvents.add(ke);
  }

  public void keyPressed(KeyEvent ke) {
    keys[ke.getKeyCode()] = true;
  }

  public void keyReleased(KeyEvent ke) {
    keys[ke.getKeyCode()] = false;
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE -> {
        exit = true;
      }
      case KeyEvent.VK_D -> {
        if (ke.isControlDown()) {
          debug = debug + 1 < 10 ? debug + 1 : 0;
        }
      }
      case KeyEvent.VK_Z -> {
        if (ke.isControlDown()) {
          initialize();
        }
      }
      case KeyEvent.VK_R -> {
        if (ke.isControlDown()) {
          load();
        }
      }

      default -> {


    public static void main(String[] args) {
        ${PROJECT_MAIN_CLASS_NAME} app = new ${PROJECT_MAIN_CLASS_NAME}();
        app.run(args);
    }
  }

  /**
   * Return keyCode status
   * 
   * @param keyCode KeyEvent code
   * @return true if the Key corresponding to the keyCode is pressed.
   */
  public boolean isKeyPressed(int keyCode) {
    return keys[keyCode];
  }

  public void resetKeyEventsStack() {
    keyEvents.clear();
  }

  public KeyEvent pollLastKeyEvent() {
    return keyEvents.pollLast();
  }

  /**
   * Draw text on the screen with specified alignment and font size.
   * 
   * @param g        the Graphics2D context
   * @param text     the text to draw
   * @param x        the x coordinate
   * @param y        the y coordinate
   * @param align    the text alignment (LEFT, CENTER, RIGHT)
   * @param fontSize the font size
   * @param c        the text color
   */
  public static void drawText(Graphics2D g, String text, int x, int y, TextAlign align, float fontSize, Color c) {
    g.setFont(g.getFont().deriveFont(Font.BOLD, fontSize));
    g.setColor(c);
    int offsetX = align.equals(TextAlign.CENTER)
        ? (int) -(g.getFontMetrics().stringWidth(text) * 0.5)
        : align.equals(TextAlign.RIGHT)
            ? -g.getFontMetrics().stringWidth(text)
            : 0;
    g.drawString(text, x + offsetX, y);
  }

}

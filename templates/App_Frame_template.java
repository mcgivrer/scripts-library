package ${PACKAGE_NAME};

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * ${MAIN_CLASS_NAME} serves as a basic demonstration of how to build a graphical application
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
 * @version ${APP_VERSION}
 */
public class ${MAIN_CLASS_NAME} implements KeyListener {

    /**
     * Represents a generic behavior that can be applied to an entity in the application.
     * This interface provides a mechanism for defining and updating the state and appearance
     * of an entity during its lifecycle, such as handling interactions, animations,
     * or custom behaviors. Both the update and draw methods can be overridden to define
     * specific behavior logic.
     *
     * @param <Entity> the type of entity to which this behavior is applied
     */
    public interface Behavior<Entity> {
        default void init(${MAIN_CLASS_NAME} app, Entity e) {
        }

        default void update(${MAIN_CLASS_NAME} app, long elapsed, Entity e) {
        }

        default void draw(${MAIN_CLASS_NAME} app, Graphics2D g, Entity e) {
        }

        default void dispose(${MAIN_CLASS_NAME} app, Entity e) {
        }
    }

    /**
     * Represents types of physical behavior that can be assigned to entities
     * within the application. This enumeration defines how an entity interacts
     * with the simulated physics system.
     * <p>
     * Types:
     * - DYNAMIC: Indicates that the entity is affected by forces and can move or
     * be influenced by the physics system. Entities marked as DYNAMIC
     * participate in collisions and respond to forces like gravity and impulses.
     * <p>
     * - STATIC: Specifies that the entity does not move and acts as an immovable
     * object within the simulation. STATIC entities can be used as barriers,
     * platforms, or environmental objects which other entities may interact with
     * but that do not change their position.
     * <p>
     * - NONE: Denotes that the entity has no physical properties, meaning it is not
     * affected by forces, collisions, or other physics-based interactions. This
     * type can be used for decorative or logical entities that do not require
     * physical simulation.
     */
    public enum PhysicType {
        DYNAMIC, STATIC, NONE;
    }

    /**
     * Represents a movable and drawable entity in a graphical application.
     * This class extends Rectangle2D.Double, providing functionality
     * to define the entity's position, size, rotation, colors, and image.
     * It also includes methods for updating the entity's state and rendering it.
     */
    public static class Entity extends Rectangle2D.Double {
        private static long index = 0;
        private long id = index++;
        public String name = "entity_%d".formatted(id);
        public List<Behavior> behaviors = new ArrayList<>();
        public double dx, dy;
        public double r, dr;
        public boolean active = true;

        public PhysicType physicType = PhysicType.DYNAMIC;
        public double mass = 1.0; // Mass of the entity (in kg)

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
            if (${MAIN_CLASS_NAME}.debug > 0) {
                int ix = 0;
                for (String s : getDebugInfo()) {
                    if (${MAIN_CLASS_NAME}.debug > ix) {
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

    /**
     * Represents a physical world or environment within which entities exist and interact.
     * This class extends the {@link Entity} class, inheriting its properties while introducing
     * specific functionality such as defining gravity in the environment.
     */
    public static class World extends Entity {
        private Point2D gravity = new Point2D.Double(0, 0.981);

        public World(double x, double y, double width, double height) {
            super(x, y, width, height);
        }

        public World(String name) {
            super(name);
            setMaterial(Material.DEFAULT);
        }

        public World setGravity(Point2D gravity) {
            this.gravity = gravity;
            return this;
        }

        @Override
        public void draw(Graphics2D g) {
            g.setColor(fillColor);
            for (double ix = x; ix < x + width; ix += 32) {
                for (double iy = y; iy < y + height; iy += 32) {
                    g.setColor(fillColor.darker());
                    g.fill(new Ellipse2D.Double(ix + 4, iy + 4, 24, 24));
                    g.setColor(fillColor);
                    g.draw(new Ellipse2D.Double(ix, iy, 32, 32));
                }
            }
            g.setColor(color);
            g.draw(this);
        }

        public Point2D getGravity() {
            return gravity;
        }
    }

    /**
     * Represents a camera entity that can follow a target entity with a smooth transition.
     * The camera adjusts its position based on the target's position, ensuring a smooth
     * movement by applying a tweening factor.
     */
    public static class Camera extends Entity {
        private Entity target;
        private double tweenFactor = 0.005;

        public Camera(String name) {
            super(name);
        }

        public Camera setTarget(Entity target) {
            this.target = target;
            return this;
        }

        public Camera setTweenFactor(double tweenFactor) {
            this.tweenFactor = tweenFactor;
            return this;
        }

        public void update(long elapsed) {
            if (target != null) {
                this.x += (target.x - this.x - (width + target.width) / 2) * tweenFactor * elapsed;
                this.y += (target.y - this.y - (height + target.height) / 2) * tweenFactor * elapsed;
            }
        }
    }

    /**
     * Represents a material with physical properties such as friction and elasticity.
     * This class is used to define how entities interact with each other and the world.
     */
    public static class Material {
        public String name;
        public double friction;
        public double elasticity;
        public boolean isFluid = false;
        public double density = 1.0; // Density in kg/m³ (or equivalent units)
        public double viscosity = 1.0; // Viscosity factor (1.0 = no viscosity, lower = more viscous)

        public static final Material DEFAULT = new Material("default", 0.98, 0.85);
        public static final Material ICE = new Material("ice", 0.02, 0.95);
        public static final Material WOOD = new Material("wood", 0.75, 0.45);
        public static final Material STONE = new Material("stone", 0.85, 0.15);
        public static final Material AIR = new Material("air", 0.999, 0.01);
        public static final Material WATER = new Material("water", 0.95, 0.3, true, 1000.0, 0.1);

        public Material(String name, double friction, double elasticity) {
            this.name = name;
            this.friction = friction;
            this.elasticity = elasticity;
        }

        public Material(String name, double friction, double elasticity, boolean isFluid, double density, double viscosity) {
            this.name = name;
            this.friction = friction;
            this.elasticity = elasticity;
            this.isFluid = isFluid;
            this.density = density;
            this.viscosity = viscosity;
        }

        public boolean isFluid() {
            return isFluid;
        }

        public double getDensity() {
            return density;
        }

        public double getViscosity() {
            return viscosity;
        }

        @Override
        public String toString() {
            return "Material{name='%s', friction=%.2f, elasticity=%.2f, isFluid=%s, density=%.1f, viscosity=%.2f}"
                    .formatted(name, friction, elasticity, isFluid, density, viscosity);
        }

        public double getFriction() {
            return friction;
        }
    }

    /**
     * Wave behavior for fluid entities.
     * This behavior manages wave generation, propagation, and surface deformation.
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
        public void init(${MAIN_CLASS_NAME} app, Entity entity) {
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
        public void update(${MAIN_CLASS_NAME} app, long elapsed, Entity entity) {
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

        private void checkEntityInteractions(${MAIN_CLASS_NAME} app, Entity fluidEntity) {
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
        public void draw(${MAIN_CLASS_NAME} app, Graphics2D g, Entity entity) {
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
        public void dispose(${MAIN_CLASS_NAME} app, Entity entity) {
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

    private static final long FPS = 60;
    private static final ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");
    private static final Properties config = new Properties();

    private static int debug = 0;
    private boolean pause = false;

    private Frame window;
    private boolean exit = false;
    private BufferedImage buffer;

    private World world;

    private final List<Entity> entities = new CopyOnWriteArrayList<>();
    private Camera camera;
    private Entity player;

    private final boolean[] keys = new boolean[1024];

    /**
     * Initializes a new instance of the ${MAIN_CLASS_NAME} class and starts the application.
     * This constructor serves as the entry point for initializing basic setup and logging
     * the application's start.
     */
    public ${MAIN_CLASS_NAME}() {
        System.out.println("Start test Application...");
    }

    /**
     * Starts the main application loop, initializing configurations, creating the window
     * and buffer, and handling the main rendering and event loop. Releases resources upon completion.
     *
     * @param args command-line arguments that can be used to override default configurations.
     */
    public void run(String[] args) {
        loadAndParseConfig("/config.properties", args);
        createWindow();
        createBuffer();
        initializeScene();
        loop();
        dispose();
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
                    public void update(${MAIN_CLASS_NAME} app, long elapsed, Entity e) {
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
     * Loads a configuration file and updates its properties based on the provided arguments.
     * The method attempts to load a configuration file from the given path and overrides
     * specific properties using the provided key-value pairs in the arguments array.
     *
     * @param s    the relative path to the configuration file to be loaded
     * @param args an array of strings representing key-value pairs (in the format key=value)
     *             to override default configurations from the file
     */
    private void loadAndParseConfig(String s, String[] args) {
        try {
            config.load(getClass().getResourceAsStream(s));
            if (args.length > 0) {
                for (String arg : args) {
                    if (arg.contains("=")) {
                        String[] keyValue = arg.split("=");
                        config.setProperty(keyValue[0], keyValue[1]);
                    }
                }
            }
            debug = Integer.parseInt((String) config.getOrDefault("app.debug", "0"));
        } catch (Exception e) {
            System.err.println("Unable to load config file");
        }
    }

    /**
     * Creates and initializes the application's main window.
     * <p>
     * The method retrieves configuration settings for the window size and applies
     * them during the initialization. It sets up a listener for window closing
     * events, ensuring the application exits gracefully when the window is closed.
     * It also adds a key listener for handling keyboard input and creates a
     * triple-buffered drawing strategy for improved rendering performance.
     * <p>
     * Behavior:
     * - Retrieves the window size from the provided configuration, defaulting
     * to 640x480 if not specified.
     * - Initializes the application window with the specified size and title.
     * - Assigns a window-closing event listener to set the application state as
     * terminated.
     * - Adds a key listener for user input handling.
     * - Makes the window visible and enables triple buffering.
     */
    private void createWindow() {
        String winSize = (String) config.getOrDefault("app.window.size", "640x480");

        window = new Frame(messages.getString("app.name"));
        String[] size = winSize.split("x");
        window.setSize(Integer.parseInt(size[0]), Integer.parseInt(size[1]));

        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit = true;
            }
        });
        window.addKeyListener(this);

        window.setVisible(true);
        window.createBufferStrategy(3);
    }


    /**
     * Creates and initializes the rendering buffer for the application.
     * <p>
     * The method retrieves the buffer resolution from the configuration file or
     * falls back to a default value of 640x480 if not specified. It then parses
     * the resolution to determine the width and height of the buffer and creates
     * a new BufferedImage object with the specified dimensions and an alpha-enabled
     * pixel format (ARGB). This buffer is used for off-screen rendering and is
     * essential for the application's graphical rendering process.
     * <p>
     * Behavior:
     * - Reads the buffer resolution configuration key (`app.renderer.buffer.resolution`).
     * - Defaults to "640x480" if no configuration is provided.
     * - Splits the resolution string into width and height.
     * - Initializes a BufferedImage object with the specified resolution and ARGB type.
     */
    private void createBuffer() {
        String bufferReso = (String) config.getOrDefault("app.renderer.buffer.resolution", "640x480");

        String[] bufferSize = bufferReso.split("x");
        buffer = new BufferedImage(Integer.parseInt(bufferSize[0]), Integer.parseInt(bufferSize[1]), BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Handles the main application rendering and event loop.
     * <p>
     * This method contains the core logic for the rendering process and time tracking.
     * It continuously executes in a loop until the application state is set to exit,
     * rendering updates to the screen, calculating elapsed time, and maintaining a
     * smooth execution cycle by delaying the loop execution using thread sleep. The
     * method also manages resources efficiently by properly disposing of graphics
     * objects after use.
     * <p>
     * Behavior:
     * - Tracks elapsed time and displays it in the console and the window.
     * - Continuously renders a graphical representation of the elapsed time on a
     * double-buffered display.
     * - Clears the rendering buffer with a black background.
     * - Draws formatted elapsed time as text using antialiasing for better visuals.
     * - Ensures proper disposal of rendering resources to maintain performance.
     * - Uses triple buffering to render the updated frame onto the screen.
     * - Sleeps for 100 ms after each iteration to maintain a consistent update cycle.
     * <p>
     * The loop terminates when the `exit` field is set to true, ensuring a smooth
     * application shutdown.
     */
    private void loop() {
        long startTime = 0, endTime = System.currentTimeMillis(), elapsed = 0, elapsedTime = 0;
        while (!exit) {
            startTime = endTime;
            elapsedTime += elapsed;
            System.out.printf("loop %s\r", getFormatedTime(elapsedTime));
            // update game logic
            if (!pause) {
                update(elapsed);
            }
            // render everything.
            render(elapsedTime);
            // reset Entity's applied forces
            postUpdate(elapsed);
            // wait next frame
            waitUntilNextFrame(elapsed);
            endTime = System.currentTimeMillis();
            elapsed = endTime - startTime;
        }
        System.out.printf("loop %s%n", getFormatedTime(elapsedTime));
    }

    /**
     * Delays execution to synchronize the application loop with a target frame rate.
     * This method calculates the remaining time until the next frame should begin
     * based on the target frames per second (FPS) and the elapsed time of the
     * current frame. It ensures a smooth and consistent frame rate by pausing
     * execution for the calculated duration.
     *
     * @param elapsed the time, in milliseconds, that has elapsed since the start
     *                of the current frame
     */
    private static void waitUntilNextFrame(long elapsed) {
        try {
            Thread.sleep((1000 / FPS) - elapsed > 0 ? (1000 / FPS) - elapsed : 1);
        } catch (Exception e) {
            System.err.printf("Unable to wait for %d ms%n", elapsed);
        }
    }

    /**
     * Updates the state of all active entities in the game, except for the {@code World},
     * based on the provided elapsed time. Additionally, updates the camera if present.
     * <p>
     * This method performs the following actions:
     * - Filters the entities to exclude the {@code World} and only includes entities
     * marked as active.
     * - Updates each active entity by invoking {@code updateEntity}, performing entity-specific
     * updates, and ensuring the entity remains within the world boundaries.
     * - Updates the camera, if it is not null, based on the elapsed time.
     *
     * @param elapsed the time, in milliseconds, that has elapsed since the last update
     */
    private void update(long elapsed) {
        entities.stream()
                .filter(e -> !(e instanceof World) && e.isActive())
                .forEach(e -> {
                    updateEntity(e, elapsed);
                    e.update(elapsed);
                    keepEntityIntoWorld(e, world);
                });
        if (camera != null) {
            camera.update(elapsed);
        }
    }

    /**
     * Performs post-update actions on all active entities in the system, excluding instances of {@code World}.
     * This method resets the forces for each active entity.
     *
     * @param elapsed the time elapsed since the last update, in milliseconds
     */
    private void postUpdate(long elapsed) {
        entities.stream()
                .filter(e -> !(e instanceof World) && e.isActive())
                .forEach(Entity::resetForces);
    }


    /**
     * Updates the position and rotation of the entity based on the elapsed time.
     * Version nettoyée sans debug.
     */
    public void updateEntity(Entity e, long elapsed) {
        // apply logic update from behaviors
        e.behaviors.forEach(b -> {
            b.update(this, elapsed, e);
        });

        Material material = e.getMaterial();
        if (e.getPhysicType().equals(PhysicType.DYNAMIC)) {
            // Reset ground status at the beginning of each update
            e.onGround = false;

            // Convert elapsed time from milliseconds to seconds
            double elapsedSeconds = elapsed / 1000.0;

            // Check for fluid collisions (separate from solid collisions)
            List<Entity> fluidEntities = checkCollisionWithFluidEntities(e);

            // Apply world gravity
            Point2D gravity = world.getGravity();
            e.dx += gravity.getX() * elapsedSeconds;
            e.dy += gravity.getY() * elapsedSeconds;

            // Apply buoyancy and drag forces if entity is in fluid
            for (Entity fluidEntity : fluidEntities) {
                // Calculate and apply buoyancy force
                Point2D buoyancyForce = calculateBuoyancyForceWithWaves(e, fluidEntity);
                e.apply(buoyancyForce);

                // Calculate and apply drag force
                Point2D dragForce = calculateDragForce(e, fluidEntity);
                e.apply(dragForce);

                // Calculate and apply surface damping to prevent oscillations
                Point2D dampingForce = calculateSurfaceDamping(e, fluidEntity);
                e.apply(dampingForce);
            }

            // compute the resulting acceleration according to applied forces
            for (Point2D f : e.getForces()) {
                e.dx += (f.getX() / e.mass) * elapsedSeconds;
                e.dy += (f.getY() / e.mass) * elapsedSeconds;
            }

            // Limiter la vitesse pour éviter les comportements erratiques
            double maxSpeed = 200.0;
            double currentSpeed = Math.sqrt(e.dx * e.dx + e.dy * e.dy);
            if (currentSpeed > maxSpeed) {
                double speedRatio = maxSpeed / currentSpeed;
                e.dx *= speedRatio;
                e.dy *= speedRatio;
            }

            // Store previous position for collision resolution
            double prevX = e.x;
            double prevY = e.y;

            // compute new position
            e.x += e.dx * elapsedSeconds;
            e.y += e.dy * elapsedSeconds;

            // Check for collisions with SOLID static entities only
            List<Material> contactMaterials = checkCollisionWithStaticEntities(e, prevX, prevY);

            // Add fluid materials to contact materials for friction calculation ONLY
            List<Material> fluidMaterials = checkFluidContact(e);
            contactMaterials.addAll(fluidMaterials);

            // compute new rotation
            e.r += e.dr * elapsedSeconds;

            // compute friction according to possible contact(s)
            if (e.onGround || !contactMaterials.isEmpty()) {
                // Calculate combined friction from all contact materials
                double combinedFriction = calculateCombinedFriction(contactMaterials);
                e.dx *= combinedFriction;
                e.dy *= combinedFriction;
                e.dr *= combinedFriction;
            } else {
                // no contact, only apply the World atmosphere's material friction.
                e.dx *= world.getMaterial().friction;
                e.dy *= world.getMaterial().friction;
                e.dr *= world.getMaterial().friction;
            }
        }
    }


    /**
     * Calculates the intersection area between two entities.
     * This is used to determine how much of an entity is submerged in fluid.
     *
     * @param entity1 the first entity
     * @param entity2 the second entity
     * @return the intersection area
     */
    private double calculateIntersectionArea(Entity entity1, Entity entity2) {
        // Calculate the intersection rectangle
        double x1 = Math.max(entity1.x, entity2.x);
        double y1 = Math.max(entity1.y, entity2.y);
        double x2 = Math.min(entity1.x + entity1.width, entity2.x + entity2.width);
        double y2 = Math.min(entity1.y + entity1.height, entity2.y + entity2.height);

        // If there's no intersection, return 0
        if (x2 <= x1 || y2 <= y1) {
            return 0.0;
        }

        // Return the area of intersection
        return (x2 - x1) * (y2 - y1);
    }

    /**
     * Calculates the buoyancy force (Archimedes' principle) for an entity submerged in fluid.
     * VERSION AVEC SUIVI DU FACTEUR D'IMMERSION MAXIMUM (sans debug).
     *
     * @param entity      the entity experiencing buoyancy
     * @param fluidEntity the fluid entity (like water)
     * @return the buoyancy force as a Point2D (toujours vers le haut)
     */
    private Point2D calculateBuoyancyForceOld(Entity entity, Entity fluidEntity) {
        // Calculate intersection area (approximation of submerged volume)
        double submergedArea = calculateIntersectionArea(entity, fluidEntity);

        if (submergedArea <= 0) {
            return new Point2D.Double(0, 0);
        }

        // Calcul du facteur d'immersion actuel
        double entityArea = entity.getWidth() * entity.getHeight();
        double currentSubmersionRatio = Math.min(submergedArea / entityArea, 1.0);

        // Mise à jour du facteur d'immersion maximum
        updateMaxSubmersionRatio(entity, currentSubmersionRatio);

        // Estimation du volume 3D
        double estimatedThickness = 10.0;
        double submergedVolume = submergedArea * estimatedThickness;

        // Get fluid density
        double fluidDensity = fluidEntity.getMaterial().getDensity();

        // Calculate buoyancy force using Archimedes' principle
        Point2D gravity = world.getGravity();

        double volumeToForceScale = 0.00001;

        // Buoyancy force magnitude (always upward, opposing gravity)
        double buoyancyForceMagnitude = fluidDensity * submergedVolume * Math.abs(gravity.getY()) * volumeToForceScale;

        // Limiter la force maximale pour éviter les explosions
        double maxBuoyancyForce = Math.abs(gravity.getY()) * entity.mass * 1.5;
        buoyancyForceMagnitude = Math.min(buoyancyForceMagnitude, maxBuoyancyForce);

        // La poussée d'Archimède agit toujours vers le haut (oppose la gravité)
        return new Point2D.Double(0, -buoyancyForceMagnitude);
    }

    /**
     * Calculates the buoyancy force with wave effects, using the same approach as the original
     * calculateBuoyancyForce method but with wave height adjustments.
     */
    private Point2D calculateBuoyancyForceWithWaves(Entity entity, Entity fluidEntity) {
        // Calculate intersection area (approximation of submerged volume)
        double submergedArea = calculateIntersectionArea(entity, fluidEntity);

        // Adjust submerged area based on wave height
        if (fluidEntity.findBehavior(WaveBehavior.class) != null) {
            submergedArea = calculateWaveAdjustedIntersectionArea(entity, fluidEntity);
        }

        if (submergedArea <= 0) {
            return new Point2D.Double(0, 0);
        }

        // Calcul du facteur d'immersion actuel
        double entityArea = entity.getWidth() * entity.getHeight();
        double currentSubmersionRatio = Math.min(submergedArea / entityArea, 1.0);

        // Mise à jour du facteur d'immersion maximum
        updateMaxSubmersionRatio(entity, currentSubmersionRatio);

        // Estimation du volume 3D
        double estimatedThickness = 10.0;
        double submergedVolume = submergedArea * estimatedThickness;

        // Get fluid density
        double fluidDensity = fluidEntity.getMaterial().getDensity();

        // Calculate buoyancy force using Archimedes' principle
        Point2D gravity = world.getGravity();

        double volumeToForceScale = 0.00001;

        // Buoyancy force magnitude (always upward, opposing gravity)
        double buoyancyForceMagnitude = fluidDensity * submergedVolume * Math.abs(gravity.getY()) * volumeToForceScale;

        // Limiter la force maximale pour éviter les explosions
        double maxBuoyancyForce = Math.abs(gravity.getY()) * entity.mass * 1.5;
        buoyancyForceMagnitude = Math.min(buoyancyForceMagnitude, maxBuoyancyForce);

        // La poussée d'Archimède agit toujours vers le haut (oppose la gravité)
        return new Point2D.Double(0, -buoyancyForceMagnitude);
    }

    /**
     * Calculates the intersection area between an entity and a fluid entity,
     * taking into account wave deformation of the fluid surface.
     */
    private double calculateWaveAdjustedIntersectionArea(Entity entity, Entity fluidEntity) {
        WaveBehavior waveBehavior = (WaveBehavior) fluidEntity.findBehavior(WaveBehavior.class);
        if (waveBehavior == null) {
            return calculateIntersectionArea(entity, fluidEntity);
        }

        // Get wave surface points
        List<Point2D> surfacePoints = waveBehavior.getSurfacePoints();
        if (surfacePoints.isEmpty()) {
            return calculateIntersectionArea(entity, fluidEntity);
        }

        // Calculate intersection with wave-deformed surface
        double totalArea = 0;
        double cellSize = waveBehavior.getCellSize();

        // Iterate through the entity's horizontal extent
        double entityLeft = entity.x;
        double entityRight = entity.x + entity.width;
        double entityTop = entity.y;
        double entityBottom = entity.y + entity.height;

        // Sample the wave surface at regular intervals
        int samples = (int) Math.ceil(entity.width / cellSize);
        samples = Math.max(samples, 10); // Minimum samples for accuracy

        for (int i = 0; i < samples; i++) {
            double x = entityLeft + (i * entity.width / samples);

            // Get wave height at this x position
            double waveHeight = waveBehavior.getWaveHeightAt(x, fluidEntity.y, fluidEntity);
            double adjustedFluidTop = waveBehavior.getBaseWaterLevel() + waveHeight;

            // Calculate intersection height at this x position
            double intersectionTop = Math.max(entityTop, adjustedFluidTop);
            double intersectionBottom = Math.min(entityBottom, fluidEntity.y + fluidEntity.height);

            if (intersectionBottom > intersectionTop) {
                // Calculate the area of this vertical slice
                double sliceWidth = entity.width / samples;
                double sliceHeight = intersectionBottom - intersectionTop;
                totalArea += sliceWidth * sliceHeight;
            }
        }

        return totalArea;
    }

    /**
     * Enhanced method to get wave behavior from entity, with better error handling.
     */
    private WaveBehavior getWaveBehavior(Entity entity) {
        return entity.behaviors.stream()
                .filter(b -> b instanceof WaveBehavior)
                .map(b -> (WaveBehavior) b)
                .findFirst()
                .orElse(null);
    }


    /**
     * Met à jour le facteur d'immersion maximum pour une entité donnée.
     * Détecte aussi les phases de remontée et d'émersion.
     *
     * @param entity       l'entité dont on suit l'immersion
     * @param currentRatio le ratio d'immersion actuel (0.0 à 1.0)
     */
    private void updateMaxSubmersionRatio(Entity entity, double currentRatio) {
        // Utiliser les propriétés de l'entité pour stocker les données
        if (entity.getProperty("maxSubmersionRatio") == null) {
            entity.setProperty("maxSubmersionRatio", currentRatio);
            entity.setProperty("wasFullySubmerged", false);
            entity.setProperty("isRising", false);
            entity.setProperty("previousRatio", currentRatio);
            entity.setProperty("riseStartTime", System.currentTimeMillis());
        }

        double maxRatio = (Double) entity.getProperty("maxSubmersionRatio");
        boolean wasFullySubmerged = (Boolean) entity.getProperty("wasFullySubmerged");
        boolean isRising = (Boolean) entity.getProperty("isRising");
        double previousRatio = (Double) entity.getProperty("previousRatio");
        long riseStartTime = (Long) entity.getProperty("riseStartTime");

        // Mise à jour du maximum
        if (currentRatio > maxRatio) {
            entity.setProperty("maxSubmersionRatio", currentRatio);
        }

        // Détecter si l'objet a été complètement submergé
        if (currentRatio >= 0.99) {
            entity.setProperty("wasFullySubmerged", true);
        }

        // Détecter le début de la remontée (vitesse vers le haut + était submergé)
        if (!isRising && entity.dy < -5 && wasFullySubmerged) {
            entity.setProperty("isRising", true);
            entity.setProperty("riseStartTime", System.currentTimeMillis());
        }

        // Détecter la fin de la remontée (objet sort de l'eau)
        if (isRising && currentRatio <= 0.01) {
            long remonteeTime = System.currentTimeMillis() - riseStartTime;
            entity.setProperty("isRising", false);
            entity.setProperty("emergenceTime", remonteeTime);
        }

        entity.setProperty("previousRatio", currentRatio);
    }

    /**
     * Calculates a stabilized drag force for an entity moving through a fluid.
     * Cette traînée aide à stabiliser les objets dans le fluide.
     *
     * @param entity      the entity experiencing drag
     * @param fluidEntity the fluid entity
     * @return the drag force as a Point2D
     */
    private Point2D calculateDragForce(Entity entity, Entity fluidEntity) {
        // Calculate intersection area to determine how much of the entity is in fluid
        double submergedArea = calculateIntersectionArea(entity, fluidEntity);

        if (submergedArea <= 0) {
            return new Point2D.Double(0, 0);
        }

        double entityArea = entity.getWidth() * entity.getHeight();
        double submersionRatio = Math.min(submergedArea / entityArea, 1.0);

        double viscosity = fluidEntity.getMaterial().getViscosity();
        double speed = Math.sqrt(entity.dx * entity.dx + entity.dy * entity.dy);

        if (speed < 0.001) return new Point2D.Double(0, 0);

        // Drag force plus important pour stabiliser
        double dragCoefficient = 2.0 * (1.0 - viscosity) * submersionRatio;
        double dragMagnitude = dragCoefficient * speed;

        // Direction opposite to velocity
        double dragX = -entity.dx * dragMagnitude / speed;
        double dragY = -entity.dy * dragMagnitude / speed;

        return new Point2D.Double(dragX, dragY);
    }

    /**
     * Calculates a damping force to prevent oscillations at the fluid surface.
     * Cette force aide à stabiliser les objets qui flottent à la surface.
     *
     * @param entity      the entity
     * @param fluidEntity the fluid entity
     * @return the damping force as a Point2D
     */
    private Point2D calculateSurfaceDamping(Entity entity, Entity fluidEntity) {
        double submergedArea = calculateIntersectionArea(entity, fluidEntity);
        double entityArea = entity.getWidth() * entity.getHeight();
        double submersionRatio = submergedArea / entityArea;

        // Damping plus fort près de la surface (entre 0.1 et 0.9 de submersion)
        if (submersionRatio > 0.1 && submersionRatio < 0.9) {
            double dampingFactor = 0.5; // Facteur d'amortissement
            return new Point2D.Double(
                    -entity.dx * dampingFactor,
                    -entity.dy * dampingFactor
            );
        }

        return new Point2D.Double(0, 0);
    }

    /**
     * Checks for collisions between an entity and fluid entities (like water).
     * Returns a list of fluid entities that the given entity is currently intersecting with.
     * Les fluides ne bloquent JAMAIS le mouvement, ils appliquent seulement des forces.
     *
     * @param entity the entity to check for fluid collisions
     * @return a list of fluid entities that the entity is colliding with
     */
    private List<Entity> checkCollisionWithFluidEntities(Entity entity) {
        return entities.stream()
                .filter(e -> e != entity &&
                        e.isActive() &&
                        e.getMaterial().isFluid() &&
                        entity.intersects(e))
                .collect(Collectors.toList());
    }

    /**
     * Checks for collisions between an entity and static entities.
     * La vérification fluide/solide se fait maintenant dans resolveCollision().
     * Returns a list of materials that the entity is in contact with.
     *
     * @param entity the entity to check for collisions
     * @param prevX  the entity's previous x position
     * @param prevY  the entity's previous y position
     * @return a list of materials that the entity is in contact with
     */
    private List<Material> checkCollisionWithStaticEntities(Entity entity, double prevX, double prevY) {
        List<Material> contactMaterials = new ArrayList<>();

        entities.stream()
                .filter(e -> e != entity &&
                        e.getPhysicType().equals(PhysicType.STATIC) &&
                        e.isActive())
                .forEach(staticEntity -> {
                    if (entity.intersects(staticEntity)) {
                        // Ajouter le matériau seulement si ce n'est PAS un fluide
                        if (!staticEntity.getMaterial().isFluid()) {
                            contactMaterials.add(staticEntity.getMaterial());
                        }
                        // Appeler la résolution - elle se chargera de vérifier si c'est un fluide
                        resolveCollision(entity, staticEntity, prevX, prevY);
                    }
                });

        return contactMaterials;
    }


    /**
     * Vérifie le contact avec les fluides pour le calcul des frottements.
     * Cette méthode ne résout AUCUNE collision, elle retourne juste les matériaux.
     *
     * @param entity the entity to check
     * @return list of fluid materials in contact
     */
    private List<Material> checkFluidContact(Entity entity) {
        return entities.stream()
                .filter(e -> e != entity &&
                        e.isActive() &&
                        e.getMaterial().isFluid() &&
                        entity.intersects(e))
                .map(Entity::getMaterial)
                .collect(Collectors.toList());
    }


    /**
     * Calculates the combined friction from multiple contact materials.
     * Uses the minimum friction value (most slippery surface dominates).
     *
     * @param contactMaterials list of materials from contacted surfaces
     * @return combined friction coefficient
     */
    private double calculateCombinedFriction(List<Material> contactMaterials) {
        if (contactMaterials.isEmpty()) {
            return world.getMaterial().friction;
        }

        // Use the minimum friction (most slippery surface dominates)
        return contactMaterials.stream()
                .mapToDouble(Material::getFriction)
                .min()
                .orElse(world.getMaterial().friction);
    }

    /**
     * Méthode principale de résolution de collision qui vérifie TOUJOURS si l'entité statique est un fluide.
     * Si c'est un fluide, la collision est IGNORÉE (pas de repositionnement).
     * Cette méthode remplace toutes les autres méthodes de résolution de collision.
     */
    private void resolveCollision(Entity dynamicEntity, Entity staticEntity, double prevX, double prevY) {
        // VÉRIFICATION ABSOLUE : Les fluides ne bloquent JAMAIS les objets
        if (staticEntity.getMaterial().isFluid()) {
            return; // SORTIE IMMÉDIATE - aucun repositionnement
        }
        // Calculate overlap on both axes
        double overlapX = Math.min(dynamicEntity.x + dynamicEntity.width - staticEntity.x,
                staticEntity.x + staticEntity.width - dynamicEntity.x);
        double overlapY = Math.min(dynamicEntity.y + dynamicEntity.height - staticEntity.y,
                staticEntity.y + staticEntity.height - dynamicEntity.y);

        // Calculate combined elasticity (average of both materials)
        double combinedElasticity = (dynamicEntity.getMaterial().elasticity + staticEntity.getMaterial().elasticity) / 2.0;

        // Mass factor for collision response (heavier objects are less affected by collisions)
        double massImpactFactor = 1.0 / Math.sqrt(dynamicEntity.mass);

        // Determine collision direction based on smaller overlap
        if (overlapX < overlapY) {
            // Horizontal collision
            if (dynamicEntity.x < staticEntity.x) {
                // Collision from left
                dynamicEntity.x = staticEntity.x - dynamicEntity.width;
                dynamicEntity.dx = -Math.abs(dynamicEntity.dx) * combinedElasticity * massImpactFactor;
            } else {
                // Collision from right
                dynamicEntity.x = staticEntity.x + staticEntity.width;
                dynamicEntity.dx = Math.abs(dynamicEntity.dx) * combinedElasticity * massImpactFactor;
            }
            dynamicEntity.addContact(1); // Horizontal contact
        } else {
            // Vertical collision
            if (dynamicEntity.y < staticEntity.y) {
                // Collision from top (entity hits static object from above)
                dynamicEntity.y = staticEntity.y - dynamicEntity.height;
                dynamicEntity.dy = -Math.abs(dynamicEntity.dy) * combinedElasticity * massImpactFactor;
            } else {
                // Collision from bottom (entity lands on static object)
                dynamicEntity.y = staticEntity.y + staticEntity.height;
                dynamicEntity.dy = Math.abs(dynamicEntity.dy) * combinedElasticity * massImpactFactor;
            }
            dynamicEntity.addContact(2); // Vertical contact
        }

        // Check if entity is standing on the static object (grounded)
        if (isEntityGroundedOn(dynamicEntity, staticEntity)) {
            dynamicEntity.onGround = true;
            dynamicEntity.jump = false;
        }
    }

    /**
     * Determines if a dynamic entity is grounded on a static entity.
     * An entity is considered grounded if it's resting on top of a static object.
     *
     * @param dynamicEntity the dynamic entity to check
     * @param staticEntity  the static entity to check against
     * @return true if the dynamic entity is grounded on the static entity
     */
    private boolean isEntityGroundedOn(Entity dynamicEntity, Entity staticEntity) {
        // Check if the dynamic entity is resting on top of the static entity
        double tolerance = 2.0; // Small tolerance for floating point precision

        // Entity's bottom should be close to or touching the static entity's top
        boolean touchingTop = Math.abs((dynamicEntity.y + dynamicEntity.height) - staticEntity.y) <= tolerance;

        // Entity should be horizontally overlapping with the static entity
        boolean horizontalOverlap = !(dynamicEntity.x + dynamicEntity.width <= staticEntity.x ||
                dynamicEntity.x >= staticEntity.x + staticEntity.width);

        // Entity should have minimal or downward vertical velocity
        boolean stableVertically = dynamicEntity.dy >= -0.1; // Allow small upward velocity due to elasticity

        return touchingTop && horizontalOverlap && stableVertically;
    }


    /**
     * Ensures that the specified entity remains within the bounds of the given world.
     * If the entity moves outside the world boundaries, its position and velocity are adjusted
     * to simulate collisions with the edges of the world using the world's material properties.
     *
     * @param e     the entity to be kept within the world bounds
     * @param world the world defining the boundaries within which the entity must remain
     */
    private void keepEntityIntoWorld(Entity e, World world) {
        e.resetContact();
        if (!world.contains(e)) {
            // Calculate combined elasticity with world material
            double combinedElasticity = (e.getMaterial().elasticity + world.getMaterial().elasticity) / 2.0;

            if (e.x < world.x) {
                e.x = world.x;
                e.dx = -e.dx * combinedElasticity;
                e.addContact(1);
            }
            if (e.y < world.y) {
                e.y = world.y;
                e.dy = -e.dy * combinedElasticity;
                e.jump = false;
                e.addContact(2);
            }
            if (e.x + e.width > world.x + world.width) {
                e.x = world.x + world.width - e.width;
                e.dx = -e.dx * combinedElasticity;
                e.addContact(4);
            }
            if (e.y + e.height > world.y + world.height) {
                e.y = world.y + world.height - e.height;
                e.dy = -e.dy * combinedElasticity;
                e.jump = false;
                e.onGround = true; // Entity is grounded when touching the bottom of the world
                e.addContact(8);
            }
        }
    }


    /**
     * Renders the game frame using double buffering and displays it on the screen.
     * This method handles the rendering of game entities, camera transformations, and
     * optional debug information based on the current game state and elapsed time.
     *
     * @param elapsedTime the time elapsed since the last frame was rendered, in milliseconds
     */
    private void render(long elapsedTime) {
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHints(
                Map.of(
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
                        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                ));
        // clear buffer
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
        // draw everything!
        entities.stream()
                .filter(Entity::isActive)
                .forEach(e -> {
                    if (camera != null) g.translate(-camera.x, -camera.y);
                    e.draw(g);
                    e.behaviors.forEach(b -> b.draw(this, g, e));
                    e.drawDebug(g);
                    if (camera != null) g.translate(camera.x, camera.y);
                });


        // dispose API
        g.dispose();
        // copy on screen
        BufferStrategy bs = window.getBufferStrategy();
        Graphics2D gs = (Graphics2D) bs.getDrawGraphics();
        gs.drawImage(buffer, 0, 0, window.getWidth(), window.getHeight(), 0, 0, buffer.getWidth(), buffer.getHeight(), null);
        // draw time on buffer
        if (debug > 0) {
            gs.setColor(Color.ORANGE);
            gs.setFont(g.getFont().deriveFont(Font.BOLD, 10.0f));
            gs.drawString("[ time:%s | debug %d | entity:%d | pause:%s ]".formatted(
                            getFormatedTime(elapsedTime),
                            debug,
                            entities.size(),
                            pause),
                    10, window.getHeight() - 12);
        }
        // free graphics API
        gs.dispose();
        // switch buffer
        bs.show();
    }

    /**
     * Releases resources and performs necessary cleanup operations for the application.
     * <p>
     * This method disposes of the main application window and prints a log message to indicate
     * the termination of the application. It is typically called at the end of the application's
     * lifecycle to ensure a proper shutdown.
     * <p>
     * Behavior:
     * - Disposes of the main window resource, releasing associated memory and system resources.
     * - Logs a message to the standard output to signal the end of the application.
     */
    private void dispose() {
        window.dispose();
        System.out.println("End test Application.");
    }


    public static void main(String[] args) {
        ${MAIN_CLASS_NAME} app = new ${MAIN_CLASS_NAME}();
        app.run(args);
    }

    /**
     * Formats the given time duration in milliseconds into a human-readable string representation.
     * The output format is "HH:mm:ss.SSS", where HH represents hours, mm represents minutes,
     * ss represents seconds, and SSS represents milliseconds.
     *
     * @param time the time duration in milliseconds to be formatted
     * @return a formatted string representing the time duration in "HH:mm:ss.SSS" format
     */
    public static String getFormatedTime(long time) {
        return "%02d:%02d:%02d.%03d".formatted(
                ((time / 1000) * 3600) % 24,
                ((time / 1000) / 60) % 60,
                ((time / 1000) % 60),
                time % 1000);
    }

    /*------------------ Manage key listener ---------------*/

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
        switch (e.getKeyCode()) {
            // quit the demo
            case KeyEvent.VK_ESCAPE -> {
                exit = true;
            }
            // switch level of the debug display /log
            case KeyEvent.VK_D -> {
                if (e.isControlDown()) {
                    debug = (debug + 1) % 10;
                }
            }
            // reverse gravity
            case KeyEvent.VK_G -> {
                if (e.isControlDown()) {
                    world.gravity = new Point2D.Double(-world.gravity.getX(), -world.gravity.getY());
                }
            }
            case KeyEvent.VK_P, KeyEvent.VK_PAUSE -> {
                pause = !pause;
            }
            case KeyEvent.VK_Z -> {
                entities.clear();
                initializeScene();
            }
            // others cases, do nothing
            default -> {
                // do nothing
            }
        }
    }

    /**
     * Checks if the specified key is currently pressed.
     *
     * @param keyCode the key code representing the key whose state is to be checked
     *                (corresponds to the constants in {@link KeyEvent}).
     * @return true if the specified key is pressed; false otherwise.
     */
    public boolean isKeyPressed(int keyCode) {
        return keys[keyCode];
    }

}

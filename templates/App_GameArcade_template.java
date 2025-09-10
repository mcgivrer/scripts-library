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
 * The main ${PROJECT_MAIN_CLASS_NAME} class for project demo004.
 *
 * @author Frederic Delorme <frederic.delorme@gmail.com>
 * @version 1.0.0
 */
public class ${PROJECT_MAIN_CLASS_NAME} implements KeyListener{

  /**
   * The ${PROJECT_MAIN_CLASS_NAME} modes.
   */
  public enum AppMode {
    /**
     * Development mode.
     * Used for debugging and development purposes.
     */
    DEVELOPMENT,
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

    /**
     * Creates a new CircularQueue with the specified capacity.
     */
    public CircularQueue(int capacity) {
      this.capacity = capacity;
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

    public float friction = 1.0f;
    public float elasticity = 1.0f;
    public float mass = 1.0f;

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
        e.dx = -e.dx * e.elasticity;
      } else if (e.x + e.width > this.x + this.width) {
        e.x = this.x + this.width - e.width;
        e.dx = -e.dx * e.elasticity;
      }
      if (e.y < this.y) {
        e.y = this.y;
        e.dy = -e.dy * e.elasticity;
      } else if (e.y + e.height > this.y + this.height) {
        e.y = this.y + this.height - e.height;
        e.dy = -e.dy * e.elasticity;

      }
    }

    /**
     * Specific drawing process for the World
     * 
     * @param g the Graphics2D API instance.
     */
    @Override
    public void draw(Graphics2D g) {
      g.setColor(fillColor);
      g.fillRect((int) -(this.width + x), (int) (-this.height + y), this.width * 3, this.height * 2);
      g.setColor(colorGround);
      g.fillRect((int) -(this.width + x), (int) (this.height + y), this.width * 3, this.height);
      g.setColor(color);
      g.drawRect((int) x, (int) (y), this.width, this.height);
    }

    /**
     * Set world Gravity factor.
     * 
     * @param g
     * @return
     */
    public World setGravity(float g) {
      this.gravity = g;
      return this;
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
   * Runs the ${PROJECT_MAIN_CLASS_NAME} with the specified command-line arguments.
   * This method initializes the application, enters the main loop, and
   * disposes of resources when exiting.
   * 
   * @param args the command-line arguments to pass to the ${PROJECT_MAIN_CLASS_NAME}
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

  /**
   * The main application loop.
   * This method initializes the application, loads resources, creates entities,
   * and enters the update-render loop until the exit flag is set.
   */
  public void loop() {
    initialize();
    load();
    create();
    do {
      update();
      render();
      try {
        Thread.sleep(60 / 1000);
      } catch (InterruptedException e) {
        error(${PROJECT_MAIN_CLASS_NAME}.class, "Unable to wait for %d ms: %s", 60 / 1000, e.getMessage());
      }
    } while (!exit);
    dispose();
  }

  /**
   * Initializes the ${PROJECT_MAIN_CLASS_NAME} window and sets up the game world.
   * If a window already exists, it is disposed of before creating a new one.
   * The window is configured with a title, size, and key listener, and a buffer
   * strategy is created for smooth rendering.
   * The game world size is set to match the window dimensions.
   */
  public void initialize() {
    if (window != null) {
      window.dispose();
    }
    window = new JFrame(messages.getString("app.name").formatted(messages.getString("app.version")));
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setPreferredSize(new Dimension(800, 600));
    window.addKeyListener(this);
    window.setLocationByPlatform(true);
    window.pack();
    window.setVisible(true);
    window.createBufferStrategy(3);
    world.setSize(window.getWidth(), window.getHeight());
  }

  /**
   * Loads the resources needed for the ${PROJECT_MAIN_CLASS_NAME}.
   * This method is a placeholder for loading images, sounds, and other assets.
   */
  private void load() {
    // load every resource you may need.
  }

  /**
   * Creates the initial entities and game objects for the ${PROJECT_MAIN_CLASS_NAME}.
   * This method sets up the game world and adds a player entity with basic
   * behaviors for movement and gravity.
   */
  private void create() {
    entities.add(
        world
            .setGravity(0.981f)
            .setSize((int) (window.getWidth() * 0.75), (int) (window.getHeight() * 0.75))
            .setPosition((int) (window.getWidth() * 0.125), (int) (window.getHeight() * 0.125)));
    // Create contextual scene.
    entities.add(
        new Entity("player", window.getWidth() / 2, window.getHeight() / 2, 24, 32)
            .setColor(Color.BLACK)
            .setFillColor(Color.BLUE)
            .setFriction(0.98f)
            .setElasticity(0.95f)
            .setMass(20.0f)
            .addBehavior(e -> {
              float speed = 0.3f;

              if (isKeyPressed(KeyEvent.VK_LEFT)) {
                e.dx = -speed;
              } else if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                e.dx = speed;
              } else {
                e.dx = e.dx * e.friction;
              }
              if (isKeyPressed(KeyEvent.VK_UP)) {
                e.dy = -speed;
              } else if (isKeyPressed(KeyEvent.VK_DOWN)) {
                e.dy = speed;
              } else {
                e.dy = e.dy * e.friction;
              }
            })
            .addBehavior(e -> {
              e.dy += world.gravity / e.mass;
            }));
  }

  /**
   * Updates the state of all entities in the game.
   * Each entity's update method is called, and if an entity goes out of the
   * world bounds, it is clamped back within the world.
   * This method is called in the main application loop to ensure that the game
   * state is consistently updated.
   */
  public void update() {
    for (Entity e : entities) {
      e.update();
      if (!world.contains(e)) {
        world.clamp(e);
      }
    }
  }

  /**
   * Renders the current state of the game to the window.
   * This method prepares the drawing context, clears the window,
   * draws all entities, and displays the current score and life count.
   * Finally, it switches the buffer to show the rendered frame.
   */
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
   * Initializes the ${PROJECT_MAIN_CLASS_NAME} configuration.
   * This method sets default configuration values, parses command-line
   * arguments, and loads additional configuration from a properties file.
   * 
   * @param args the command-line arguments to parse for configuration settings
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
   * Parses the configuration properties and sets the corresponding fields.
   * This method reads the configuration keys and updates the debug level and
   * application mode accordingly.
   * Unknown configuration keys are logged as warnings.
   * 
   * @param config the Properties object containing configuration key-value pairs
   * @see #debug
   * @see #mode
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
   * Disposes of the ${PROJECT_MAIN_CLASS_NAME} resources and closes the window.
   * This method is called when the application is exiting to ensure that
   * resources are properly released and the window is closed.
   */
  private void dispose() {
    if (Optional.ofNullable(window).isPresent()) {
      window.dispose();
    }
    info(${PROJECT_MAIN_CLASS_NAME}.class, "${PROJECT_MAIN_CLASS_NAME} '%s' is ending.", messages.getString("app.name"));

  }

  /**
   * The main entry point for the ${PROJECT_MAIN_CLASS_NAME}.
   * Creates an instance of ${PROJECT_MAIN_CLASS_NAME} and runs it with the provided command-line
   * arguments.
   * 
   * @param args the command-line arguments to pass to the ${PROJECT_MAIN_CLASS_NAME}
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

  /**
   * KeyListener implementation to handle key events.
   * Key events are added to a circular queue for later processing.
   */
  @Override
  public void keyTyped(KeyEvent ke) {
    keyEvents.add(ke);
  }

  /**
   * Key pressed event handler.
   * Sets the corresponding key state to true in the keys array.
   * 
   * @param ke the KeyEvent object representing the key press event
   */
  @Override
  public void keyPressed(KeyEvent ke) {
    keys[ke.getKeyCode()] = true;
  }

  /**
   * Key released event handler.
   * Sets the corresponding key state to false in the keys array.
   * Handles specific key releases for actions like exiting the app,
   * toggling debug levels, and reinitializing or reloading the app.
   * 
   * @param ke the KeyEvent object representing the key release event
   */
  @Override
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

      }
    }
  }

  /**
   * Check if a specific key is currently pressed.
   * 
   * @param keyCode the key code to check (e.g., KeyEvent.VK_A)
   * @return true if the key is pressed, false otherwise
   */
  public boolean isKeyPressed(int keyCode) {
    return keys[keyCode];
  }

  /**
   * Reset the key events stack.
   * This method clears all stored key events from the circular queue.
   */
  public void resetKeyEventsStack() {
    keyEvents.clear();
  }

  /**
   * Poll the last key event from the circular queue.
   * This method retrieves and removes the most recent key event,
   * or returns null if the queue is empty.
   * 
   * @return the last KeyEvent, or null if the queue is empty
   */
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
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
public class ${PROJECT_MAIN_CLASS_NAME} implements KeyListener {

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
    public void update(long elapsed) {
      for (Behavior<Entity> b : behaviors) {
        b.apply(this, elapsed);
      }
      x += dx * elapsed;
      y += dy * elapsed;
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
      g.setColor(fillColor);
      g.fillRect((int) (-this.width + x), (int) (-this.height + y), this.width * 3, 2 * this.height);
      g.setColor(colorGround);
      g.fillRect((int) (-this.width + x), (int) (this.height + y), this.width * 3, this.height);
      g.setColor(color);
      g.drawRect((int) (x), (int) (y), this.width, this.height);

    }

  }

  /**
   * Define a Behavior to be applied to any object T.
   */
  public interface Behavior<T> {
    /**
     * Apply the Behavior to the T object.
     * 
     * @param o       T object instance to apply Behavior to.
     * @param elapsed time elapsed since last call in ms.
     */
    void apply(T o, long elapsed);
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
    info(${PROJECT_MAIN_CLASS_NAME}.class, "Create ${PROJECT_MAIN_CLASS_NAME} '%s'", messages.getString("${PROJECT_MAIN_CLASS_NAME}.name"));
  }

  /**
   * Runs the ${PROJECT_MAIN_CLASS_NAME}.
   *
   * @param args the command-line arguments
   */
  public void run(String[] args) {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "${PROJECT_MAIN_CLASS_NAME} '%s' is running..", messages.getString("${PROJECT_MAIN_CLASS_NAME}.name"));
    for (String arg : args) {
      info(${PROJECT_MAIN_CLASS_NAME}.class, "", arg);
    }
    init(args);
    loop();
    dispose();
  }

  public void loop() {
    long startTime = System.currentTimeMillis();
    long elapsed = 0;
    long endTime = 0;
    initialize();
    load();
    create();
    do {
      startTime = endTime;
      elapsed = startTime - endTime;
      update(elapsed);
      render();
      endTime = System.currentTimeMillis();
    } while (!exit);
    dispose();
  }

  public void initialize() {
    if (window != null) {
      window.dispose();
    }
    window = new JFrame(messages.getString("${PROJECT_MAIN_CLASS_NAME}.name").formatted(messages.getString("${PROJECT_MAIN_CLASS_NAME}.version")));
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setPreferredSize(new Dimension(800, 600));
    window.addKeyListener(this);
    window.setLocationByPlatform(false);
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
            .addBehavior((e, elapsed) -> {
              float speed = 0.6f;
              float friction = 0.98f;

              if (isKeyPressed(KeyEvent.VK_LEFT)) {
                e.dx = -speed;
              } else if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                e.dx = speed;
              } else {
                e.dx = e.dx * 0.98f;
              }
              if (isKeyPressed(KeyEvent.VK_UP)) {
                e.dy = -(world.gravity + speed*5);
              } else if (isKeyPressed(KeyEvent.VK_DOWN)) {
                e.dy = speed;
              } else {
                e.dy = e.dy * friction;
              }
            })
            .addBehavior((e, elapsed) -> {
              e.dy += world.gravity * .01f * elapsed;
            }));
  }

  public void update(long elapsed) {
    for (Entity e : entities) {
      e.update(elapsed);
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
    info(${PROJECT_MAIN_CLASS_NAME}.class, "${PROJECT_MAIN_CLASS_NAME} '%s' is initializing.", messages.getString("${PROJECT_MAIN_CLASS_NAME}.name"));
    // default values
    config.put("${PROJECT_MAIN_CLASS_NAME}.config.file", "/config.properties");
    config.put("${PROJECT_MAIN_CLASS_NAME}.debug", 0);
    config.put("${PROJECT_MAIN_CLASS_NAME}.mode", AppMode.DEVELOPMENT);
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
      config.load(${PROJECT_MAIN_CLASS_NAME}.class.getResourceAsStream(config.getProperty("${PROJECT_MAIN_CLASS_NAME}.config.file")));
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
        case "${PROJECT_MAIN_CLASS_NAME}.debug":
          debug = Integer.parseInt(value);
          info(${PROJECT_MAIN_CLASS_NAME}.class, "read config '%s' = '%s'", key, value);
          break;
        case "${PROJECT_MAIN_CLASS_NAME}.mode":
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
    info(${PROJECT_MAIN_CLASS_NAME}.class, "${PROJECT_MAIN_CLASS_NAME} '%s' is ending.", messages.getString("${PROJECT_MAIN_CLASS_NAME}.name"));

  }

  /**
   * The main entry point for the ${PROJECT_MAIN_CLASS_NAME}.
   */
  public static void main(String[] args) {
    ${PROJECT_MAIN_CLASS_NAME} ${PROJECT_MAIN_CLASS_NAME} = new ${PROJECT_MAIN_CLASS_NAME}();
    ${PROJECT_MAIN_CLASS_NAME}.run(args);
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

      }
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

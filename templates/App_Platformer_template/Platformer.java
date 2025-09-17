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
 * The main Platformer class for project demo004.
 *
 * @author Frederic Delorme <frederic.delorme@gmail.com>
 * @version 1.0.0
 */
public class Platformer implements KeyListener {

  /**
   * The resource bundle for internationalization.
   */
  public static ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");
  /**
   * The Platformer configuration properties.
   */
  public static Properties config = new Properties();
  /**
   * The debug level for the Platformer.
   * 0 = no debug, 1 = basic debug, 2 = detailed debug
   */
  public static int debug = 0;
  /**
   * The current Platformer mode.
   * Default is PRODUCTION.
   */
  public static AppMode mode = AppMode.PRODUCTION;

  /**
   * Flag to indicate if the Platformer should exit.
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
   * The main Platformer window.
   */
  public JFrame window;

  public World world = new World(800, 600);
  /**
   * The list of entities in the game.
   */
  public List<Entity> entities = new LinkedList<>();

  public int score = 0, life = 3;

  /**
   * Creates a new instance of the Platformer.
   */
  public Platformer() {
    info(Platformer.class, "Create Platformer '%s'", messages.getString("app.name"));
  }

  /**
   * Runs the Platformer.
   *
   * @param args the command-line arguments
   */
  public void run(String[] args) {
    info(Platformer.class, "Platformer '%s' is running..", messages.getString("app.name"));
    for (String arg : args) {
      info(Platformer.class, "", arg);
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
   * Initializes the Platformer.
   *
   * @param args the command-line arguments
   */
  private void init(String[] args) {
    info(Platformer.class, "Platformer '%s' is initializing.", messages.getString("app.name"));
    // default values
    config.put("app.config.file", "/config.properties");
    config.put("app.debug", 0);
    config.put("app.mode", AppMode.DEVELOPMENT);
    // parsing arguments
    for (String arg : args) {
      String[] kv = arg.split("=", 2);
      if (kv.length == 2) {
        config.put(kv[0], kv[1]);
        info(Platformer.class, "Set config from argument %s = %s", kv[0], kv[1]);
      } else {
        warn(Platformer.class, "Invalid config argument: %s", arg);
      }
      info(Platformer.class, "", arg);
    }
    // load configuration file
    try {
      config.load(Platformer.class.getResourceAsStream(config.getProperty("app.config.file")));
    } catch (Exception e) {
      error(Platformer.class, "Failed to load config file: %s", e.getMessage());
    }
    // extract configuration values
    parseConfiguration(config);
  }

  /**
   * Parses the Platformer configuration.
   *
   * @param config the configuration properties
   */
  private void parseConfiguration(Properties config) {
    info(Platformer.class, "Parsing configuration.");
    for (String key : config.stringPropertyNames()) {
      String value = config.getProperty(key);
      switch (key) {
        case "app.debug":
          debug = Integer.parseInt(value);
          info(Platformer.class, "read config '%s' = '%s'", key, value);
          break;
        case "app.mode":
          mode = AppMode.valueOf(value.toUpperCase());
          info(Platformer.class, "read config '%s' = '%s'", key, value);
          break;
        default:
          warn(Platformer.class, "Unknown config key: %s", key);
      }
      info(Platformer.class, "Config '%s' = '%s'", key, value);
    }
  }

  /**
   * Disposes the Platformer resources.
   */
  private void dispose() {
    if (Optional.ofNullable(window).isPresent()) {
      window.dispose();
    }
    info(Platformer.class, "Platformer '%s' is ending.", messages.getString("app.name"));

  }

  /**
   * The main entry point for the Platformer.
   */
  public static void main(String[] args) {
    Platformer app = new Platformer();
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

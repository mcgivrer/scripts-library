package com.snapgames.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFrame;

/**
 * The main application class.
 *
 * @author Default Author <default@example.com>
 * @version 0.0.1
 */
public class Application implements KeyListener {

    /**
     * The application modes.
     */
    public enum AppMode {
        /**
         * Development mode.
         * Used for debugging and development purposes.
         */
        DEVELOPMENT,
        /**
         * Production mode.
         * Used for running the application in a production environment.
         */
        PRODUCTION
    }

    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }

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
     * The resource bundle for internationalization.
     */
    public static ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");
    /**
     * The application configuration properties.
     */
    public static Properties config = new Properties();
    /**
     * The debug level for the application.
     * 0 = no debug, 1 = basic debug, 2 = detailed debug
     */
    public static int debug = 0;
    /**
     * The current application mode.
     * Default is PRODUCTION.
     */
    public static AppMode mode = AppMode.PRODUCTION;

    public boolean exit = false;

    public CircularQueue<KeyEvent> keyEvents = new CircularQueue<>(100);
    public boolean keys[] = new boolean[1024];

    public JFrame window;

    /**
     * Creates a new instance of the application.
     */
    public Application() {
        info(Application.class, "Create application '%s'", messages.getString("app.name"));
    }

    /**
     * Runs the application.
     *
     * @param args the command-line arguments
     */
    public void run(String[] args) {
        info(Application.class, "Application '%s' is running..", messages.getString("app.name"));
        for (String arg : args) {
            info(Application.class, "", arg);
        }
        init(args);
        loop();
        dispose();
    }

    public void loop() {
        initialize();
        do {
            update();
            render();
        } while (!exit);
        dispose();
    }

    public void initialize() {
        window = new JFrame(messages.getString("app.name").formatted(messages.getString("app.version")));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setPreferredSize(new Dimension(800, 600));
        window.addKeyListener(this);
        window.pack();
        window.setVisible(true);
        window.createBufferStrategy(3);
    }

    public void update() {
        // do you stuff !
    }

    public void render() {
        // prepare drawing graphics API
        BufferStrategy bf = window.getBufferStrategy();
        Graphics2D g = (Graphics2D) bf.getDrawGraphics();
        // set drawing configuration.
        g.setRenderingHints(
                Map.of(
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
                        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                ));
        // clear window
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, window.getWidth(), window.getHeight());

        // do you drawings
        drawText(g, messages.getString("app.message.welcome"),
                (int) (window.getWidth() * 0.5), (int) (window.getHeight() * 0.5),
                TextAlign.CENTER,
                28.0f, Color.WHITE);
        drawText(g, messages.getString("app.message.exit"),
                (int) (window.getWidth() * 0.5), (int) (window.getHeight() * 0.5) + 30,
                TextAlign.CENTER,
                12.0f, Color.GRAY);

        //switch buffer
        g.dispose();
        bf.show();

    }


    /**
     * Initializes the application.
     *
     * @param args the command-line arguments
     */
    private void init(String[] args) {
        info(Application.class, "Application '%s' is initializing.", messages.getString("app.name"));
        // default values
        config.put("app.config.file", "/config.properties");
        config.put("app.debug", 0);
        config.put("app.mode", AppMode.DEVELOPMENT);
        // parsing arguments
        for (String arg : args) {
            String[] kv = arg.split("=", 2);
            if (kv.length == 2) {
                config.put(kv[0], kv[1]);
                info(Application.class, "Set config from argument %s = %s", kv[0], kv[1]);
            } else {
                warn(Application.class, "Invalid config argument: %s", arg);
            }
            info(Application.class, "", arg);
        }
        // load configuration file
        try {
            config.load(Application.class.getResourceAsStream(config.getProperty("app.config.file")));
        } catch (Exception e) {
            error(Application.class, "Failed to load config file: %s", e.getMessage());
        }
        // extract configuration values
        parseConfiguration(config);
    }

    /**
     * Parses the application configuration.
     *
     * @param config the configuration properties
     */
    private void parseConfiguration(Properties config) {
        info(Application.class, "Parsing configuration.");
        for (String key : config.stringPropertyNames()) {
            String value = config.getProperty(key);
            switch (key) {
                case "app.debug":
                    debug = Integer.parseInt(value);
                    info(Application.class, "read config '%s' = '%s'", key, value);
                    break;
                case "app.mode":
                    mode = AppMode.valueOf(value.toUpperCase());
                    info(Application.class, "read config '%s' = '%s'", key, value);
                    break;
                default:
                    warn(Application.class, "Unknown config key: %s", key);
            }
            info(Application.class, "Config '%s' = '%s'", key, value);
        }
    }

    /**
     * Disposes the application resources.
     */
    private void dispose() {
        if (Optional.ofNullable(window).isPresent()) {
            window.dispose();
        }
        info(Application.class, "Application '%s' is ending.", messages.getString("app.name"));

    }

    /**
     * The main entry point for the application.
     */
    public static void main(String[] args) {
        Application app = new Application();
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
            default -> {

            }
        }
    }

    public void resetKeyEventsStack() {
        keyEvents.clear();
    }

    public KeyEvent pollLastKeyEvent() {
        return keyEvents.pollLast();
    }

    // Graphics drawing utilities

    public void drawText(Graphics2D g, String text, int x, int y, TextAlign align, float fontSize, Color c) {
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

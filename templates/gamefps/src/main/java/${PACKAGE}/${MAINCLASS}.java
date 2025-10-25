package ${PACKAGE};

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
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

import ${PACKAGE}.utils.AppMode;
import ${PACKAGE}.utils.TextAlign;
import ${PACKAGE}.utils.CircularQueue;

/**
 * The main ${MAINCLASS} class for project demo008.
 *
 * @author ${AUTHOR_NAME} <${AUTHOR_EMAIL}>
 * @version ${PROJECT_VERSION}
 * @since ${PROJECT_YEAR}
 */
public class ${MAINCLASS} implements KeyListener {


    /**
     * The resource bundle for internationalization.
     */
    public static ResourceBundle i18Text = ResourceBundle.getBundle("i18n/messages");
    /**
     * The ${MAINCLASS} configuration properties.
     */
    public static Properties config = new Properties();
    /**
     * The debug level for the ${MAINCLASS}.
     * 0 = no debug, 1 = basic debug, 2> = detailed debug
     */
    public static int debug = 0;
    /**
     * The current ${MAINCLASS} mode.
     * Default is PRODUCTION.
     */
    public static AppMode mode = AppMode.DEVELOPMENT;

    public boolean exit = false;

    public CircularQueue<KeyEvent> keyEvents = new CircularQueue<>(100);
    public boolean keys[] = new boolean[1024];

    public JFrame window;

    /**
     * Creates a new instance of the ${MAINCLASS}.
     */
    public ${MAINCLASS}() {
        info(${MAINCLASS}.class, "Create ${MAINCLASS} '%s'", getI18n("app.name", "Application"));
    }

    /**
     * Runs the ${MAINCLASS}.
     *
     * @param args the command-line arguments
     */
    public void run(String[] args) {
        info(${MAINCLASS}.class, "${MAINCLASS} '%s' is running..", getI18n("app.name", "1.0.0"));
        for (String arg : args) {
            info(${MAINCLASS}.class, "- %s", arg);
        }
        init(args);
        loop();
        dispose();
    }

    /**
     * Initializes the ${MAINCLASS}.
     *
     * @param args the command-line arguments
     */
    private void init(String[] args) {
        info(${MAINCLASS}.class, "${MAINCLASS} '%s' is initializing.", getI18n("app.name", "Application"));
        // default values
        config.put("app.config.file", "/config.properties");
        config.put("app.debug", 0);
        config.put("app.mode", AppMode.DEVELOPMENT);
        // parsing arguments
        for (String arg : args) {
            String[] kv = arg.split("=", 2);
            if (kv.length == 2) {
                config.put(kv[0], kv[1]);
                info(${MAINCLASS}.class, "Set config from argument %s = %s", kv[0], kv[1]);
            } else {
                warn(${MAINCLASS}.class, "Invalid config argument: %s", arg);
            }
            info(${MAINCLASS}.class, "", arg);
        }
        // load configuration file
        try {
            config.load(${MAINCLASS}.class.getResourceAsStream(config.getProperty("app.config.file")));
        } catch (Exception e) {
            error(${MAINCLASS}.class, "Failed to load config file: %s", e.getMessage());
        }
        // extract configuration values
        parseConfiguration(config);
    }

    /**
     * Parses the ${MAINCLASS} configuration.
     *
     * @param config the configuration properties
     */
    private void parseConfiguration(Properties config) {
        info(${MAINCLASS}.class, "Parsing configuration.");
        for (String key : config.stringPropertyNames()) {
            String value = config.getProperty(key);
            switch (key) {
                case "app.debug":
                    debug = Integer.parseInt(value);
                    info(${MAINCLASS}.class, "read config '%s' = '%s'", key, value);
                    break;
                case "app.mode":
                    mode = AppMode.valueOf(value.toUpperCase());
                    info(${MAINCLASS}.class, "read config '%s' = '%s'", key, value);
                    break;
                default:
                    warn(${MAINCLASS}.class, "Unknown config key: %s", key);
            }
            info(${MAINCLASS}.class, "Config '%s' = '%s'", key, value);
        }
    }

    public void loop() {
        long startTime = 0, endTime = 0, elapsed = 0;
        long fps = 0, fpsFrame = 0, fpsTimeFrame = 0;
        Map<String, Object> stats = new ConcurrentHashMap<>();
        initialize();
        do {
            startTime = endTime;

            update(stats, elapsed);
            render(stats, elapsed);

            sleep((1000 / 60) - elapsed > 0 ? (1000 / 60) - elapsed : 1);
            fpsTimeFrame += elapsed;
            fpsFrame++;
            if (fpsTimeFrame > 1000) {
                fps = fpsFrame;
                fpsTimeFrame = 0;
                fpsFrame = 0;
            }
            endTime = System.currentTimeMillis();
            elapsed = endTime - startTime;

            // store statistic metrics.
            stats.put("startTime", startTime);
            stats.put("endTime", endTime);
            stats.put("elapsed", elapsed);
            stats.put("fps", fps);
        } while (!exit);
        dispose();
    }

    private void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            fatal(this.getClass(), -1, "Unable to wait for %d ms : %s", l, e.getMessage());
        }
    }

    public void initialize() {
        window = new JFrame(getI18n("app.name", "Application").formatted(getI18n("app.version", "1.0.0")));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setPreferredSize(new Dimension(800, 600));
        window.addKeyListener(this);
        window.pack();
        window.setVisible(true);
        window.createBufferStrategy(3);
    }

    public void update(Map<String, Object> stats, long elapsed) {
        // do you stuff !
    }

    public void render(Map<String, Object> stats, long elapsed) {
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

        // do you drawings
        drawText(g, getI18n("app.message.welcome", "Welcome into this demo"),
                (int) (window.getWidth() * 0.5), (int) (window.getHeight() * 0.5),
                TextAlign.CENTER,
                28.0f, Color.WHITE,
                Font.BOLD);
        drawText(g, getI18n("app.message.exit", "press ESCAPE to exit"),
                (int) (window.getWidth() * 0.5), (int) (window.getHeight() * 0.5) + 30,
                TextAlign.CENTER,
                12.0f, Color.GRAY,
                Font.BOLD);

        if (debug > 0) {
            drawText(g,
                    "[ dbg:%d | elapsed:%02d | FPS:%03d ]".formatted(debug, stats.get("elapsed"), stats.get("fps")),
                    30, window.getHeight() - 20,
                    TextAlign.LEFT,
                    11.0f,
                    Color.ORANGE,
                    Font.PLAIN);
        }
        // switch buffer
        g.dispose();
        bf.show();

    }

    /**
     * Disposes the ${MAINCLASS} resources.
     */
    private void dispose() {
        if (Optional.ofNullable(window).isPresent()) {
            window.dispose();
        }
        info(${MAINCLASS}.class, "${MAINCLASS} '%s' is ending.", i18Text.getString("app.name"));

    }

    /**
     * The main entry point for the ${MAINCLASS}.
     */
    public static void main(String[] args) {
        ${MAINCLASS} ${MAINCLASS} = new ${MAINCLASS}();
        ${MAINCLASS}.run(args);
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
        if (isDebugGreaterThan(debugLevel) && mode == AppMode.DEVELOPMENT) {
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
     * Logs a fatal message and exit the application with -1 status.
     *
     * @param cls        the class logging the message
     * @param exitStatus the status value return on execution stop.
     * @param message    the log message
     * @param args       optional arguments for the message
     */
    public static void fatal(Class<?> cls, int exitStatus, String message, Object... args) {
        log(cls, "FATAL", message, args);
        System.exit(exitStatus);
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

    public void drawText(Graphics2D g, String text, int x, int y, TextAlign align, float fontSize, Color c,
            int fontStyle) {
        g.setFont(g.getFont().deriveFont(fontStyle, fontSize));
        g.setColor(c);
        int offsetX = align.equals(TextAlign.CENTER)
                ? (int) -(g.getFontMetrics().stringWidth(text) * 0.5)
                : align.equals(TextAlign.RIGHT)
                        ? -g.getFontMetrics().stringWidth(text)
                        : 0;
        g.drawString(text, x + offsetX, y);
    }

    public static String getI18n(String key, String defaultText) {
        String msg = i18Text.getString(key);
        if (msg == null)
            return defaultText;
        return msg;

    }

}

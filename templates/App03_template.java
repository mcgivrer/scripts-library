package ${PROJECT_PACKAGE_NAME};

import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * The main application class.
 *
 * @author ${GIT_AUTHOR_NAME} <${GIT_AUTHOR_EMAIL}>
 * @version ${APP_VERSION}
 */
public class ${PROJECT_MAIN_CLASS_NAME}{

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
  PRODUCTION}

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

  /**
   * Creates a new instance of the application.
   */
  public ${PROJECT_MAIN_CLASS_NAME}() {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "Create application '%s'", messages.getString("app.name"));
  }

  /**
   * Runs the application.
   *
   * @param args the command-line arguments
   */
  public void run(String[] args) {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "Application '%s' is running..", messages.getString("app.name"));
    for (String arg : args) {
      info(${PROJECT_MAIN_CLASS_NAME}.class, "", arg);
    }
    init(args);
    dispose();
  }

  /**
   * Initializes the application.
   *
   * @param args the command-line arguments
   */
  private void init(String[] args) {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "Application '%s' is initializing.", messages.getString("app.name"));
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
   * Parses the application configuration.
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
   * Disposes the application resources.
   */
  private void dispose() {
    info(${PROJECT_MAIN_CLASS_NAME}.class, "Application '%s' is ending.", messages.getString("app.name"));
  }

  /**
   * The main entry point for the application.
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
}

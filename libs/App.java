package core;

import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.ResourceBundle;

public class App{
    public enum AppMode{
        DEVELOPMENT,
        PRODUCTION
    }

    public static int debug = 0;
    public static ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages");
    public static Properties config = new Properties();

    public App(){
      info(App.class, "Create application");
    }

    public void run(String[] args) {
      info(App.class, "Application is running..");
      for(String arg : args){
        info(App.class, "", arg);
      }
      init(args);
      dispose();
    }

    private void init(String[] args) {
      info(App.class, "Application is initializing.");
      // default values
      config.put("app.config.file", "/config.properties");
      config.put("app.debug",0);
      config.put("app.mode",AppMode.DEVELOPMENT);

      for(String arg : args){
        String[] kv = arg.split("=",2);
        if(kv.length == 2){
          config.put(kv[0], kv[1]);
          info(App.class, "Set config from argument %s = %s", kv[0], kv[1]);
        }else{
          warn(App.class, "Invalid config argument: %s", arg);
        }
        info(App.class, "", arg);
      }
      config.load(App.class.getResourceAsStream(config.getProperty("app.config.file")));
      parseConfiguration(config);
    }

    private void parseConfiguration(Properties config) {
      info(App.class, "Parsing configuration.");
      for (String key : config.stringPropertyNames()) {
        String value = config.getProperty(key);
        switch(key){
            case "app.debug":
                debug = Integer.parseInt(value);
                info(App.class, "read config '%s' = '%s'", key, value);
                break;
            case "app.mode":
                mode = AppMode.valueOf(value.toUpperCase());
                info(App.class, "read config '%s' = '%s'", key, value);
                break;
            default:
                warn(App.class, "Unknown config key: %s", key);
        }
        info(App.class, "Config '%s' = '%s'", key, value);
      }
    }

    private void dispose(){
      info(App.class, "Application is ending.");
    }

    public static void main(String[] args){
      App app = new App();
      app.run(args);
    }

    public static void log(Class<?> cls, String level, String message, Object... args) {
        System.out.printf("%s;%s;%s;%s%n", ZonedDateTime.now(), cls.getCanonicalName(), level, message.formatted(args));
    }

    public static void info(Class<?> cls, String message, Object... args) {
        log(cls, "INFO", message, args);
    }

    public static void warn(Class<?> cls, String message, Object... args) {
        log(cls, "WARN", message, args);
    }

    public static void debug(Class<?> cls, int debugLevel, String message, Object... args) {
        if (App.isDebugGreaterThan(debugLevel)) {
            log(cls, "DEBUG", message, args);
        }
    }

    public static void error(Class<?> cls, String message, Object... args) {
        log(cls, "ERROR", message, args);
    }



    public static boolean isDebugGreaterThan(int level) {
        return debug < level;
    }
}
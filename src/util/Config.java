package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Simple configuration loader. Reads {@code config.properties} from the classpath.
 * Values are loaded once at class initialization.
 */
public final class Config {
    public static final int SLOT_BUFFER_MINUTES;
    public static final double GST_RATE;
    public static final String DATETIME_FORMAT;

    static {
        Properties prop = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException e) {
            // ignore – defaults will be used
        }
        SLOT_BUFFER_MINUTES = Integer.parseInt(prop.getProperty("slot.buffer.minutes", "30"));
        GST_RATE = Double.parseDouble(prop.getProperty("gst.rate", "0.18"));
        DATETIME_FORMAT = prop.getProperty("datetime.format", "dd-MM-yyyy HH:mm");
    }

    private Config() { }
}


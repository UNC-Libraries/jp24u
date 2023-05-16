package JP2ImageConverter.util;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

/**
 * Constants common to CLIs
 * @author krwong
 */
public class CLIConstants {
    private CLIConstants() {
    }

    public static final String OUTPUT_LOGGER_NAME = "output";
    public static final Logger outputLogger = getLogger(OUTPUT_LOGGER_NAME);
}

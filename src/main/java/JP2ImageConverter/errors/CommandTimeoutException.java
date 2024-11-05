package JP2ImageConverter.errors;

import java.util.List;

/**
 * @author bbpennel
 */
public class CommandTimeoutException extends CommandException {
    private static final long serialVersionUID = 1L;

    public CommandTimeoutException(String message, List<String> command, String output) {
        super(message, command, output, -1);
    }
}

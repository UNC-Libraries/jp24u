package JP2ImageConverter;

import picocli.CommandLine;

/**
 * Main class for the CLI utils
 * @author krwong
 */
@CommandLine.Command(subcommands = {
        JP2ImageConverterCommand.class
})
public class CLIMain {

    protected CLIMain() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CLIMain()).execute(args);
        System.exit(exitCode);
    }

    public static int runCommand(String[] args) {
        int exitCode = new CommandLine(new CLIMain()).execute(args);
        return exitCode;
    }
}

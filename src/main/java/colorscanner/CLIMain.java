package colorscanner;

import picocli.CommandLine;

/**
 * Main class for the CLI utils
 * @author krwong
 */
@CommandLine.Command(subcommands = {
        ColorScannerCommand.class
})
public class CLIMain {

    protected CLIMain() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CLIMain()).execute(args);
        System.exit(exitCode);
    }
}

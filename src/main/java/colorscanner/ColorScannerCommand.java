package colorscanner;

import colorscanner.options.ColorScannerOptions;
import colorscanner.services.ColorFieldsService;
import colorscanner.services.KakaduService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static colorscanner.util.CLIConstants.outputLogger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author krwong
 */
@Command(name = "colorscanner",
        description = "")
public class ColorScannerCommand {
    private static final Logger log = getLogger(ColorScannerCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private ColorFieldsService colorFieldsService = new ColorFieldsService();
    private KakaduService kakaduService = new KakaduService();

    @Command(name = "list",
            description = "Retrieve image color fields and attributes for an image file.")
    public int list(@Mixin ColorScannerOptions options) throws Exception {
        try {
            colorFieldsService.listFields(options.getFileName());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to list color fields", e);
            return 1;
        }
    }

    @Command(name = "list all",
            description = "Retrieve image color fields and attributes for a list of files.")
    public int listAll(@Mixin ColorScannerOptions options) throws Exception {
        try {
            colorFieldsService.fileListAllFields(options.getFileName());
            return 0;
        } catch (Exception e) {
            outputLogger.info("FAIL: {}", e.getMessage());
            log.error("Failed to list color fields. Not processing file list further.", e);
            return 1;
        }
    }

    @Command(name = "kdu_compress",
            description = "Run kakadu kdu_compress on an image file.")
    public int kduCompress(@Mixin ColorScannerOptions options) throws Exception {
        try {
            kakaduService.setColorFieldsService(colorFieldsService);
            kakaduService.kduCompress(options.getFileName());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to generate jp2 file", e);
            return 1;
        }
    }

    @Command(name = "kdu_compress all",
            description = "Run kakadu kdu_compress on a list of image files.")
    public int kduCompressAll(@Mixin ColorScannerOptions options) throws Exception {
        try {
            kakaduService.setColorFieldsService(colorFieldsService);
            kakaduService.fileListKduCompress(options.getFileName());
            return 0;
        } catch (Exception e) {
            outputLogger.info("FAIL: {}", e.getMessage());
            log.error("Failed to generate jp2 file. Not processing file list further.", e);
            return 1;
        }
    }

}

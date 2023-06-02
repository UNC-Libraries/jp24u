package JP2ImageConverter;

import JP2ImageConverter.options.JP2ImageConverterOptions;
import JP2ImageConverter.services.ColorFieldsService;
import JP2ImageConverter.services.KakaduService;
import JP2ImageConverter.services.ImagePreproccessingService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static JP2ImageConverter.util.CLIConstants.outputLogger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author krwong
 */
@Command(name = "jp24u",
        description = "")
public class JP2ImageConverterCommand {
    private static final Logger log = getLogger(JP2ImageConverterCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private ColorFieldsService colorFieldsService = new ColorFieldsService();
    private ImagePreproccessingService imagePreproccessingService = new ImagePreproccessingService();
    private KakaduService kakaduService = new KakaduService();

    @Command(name = "list",
            description = "Retrieve image color fields and attributes for an image file.")
    public int list(@Mixin JP2ImageConverterOptions options) throws Exception {
        try {
            colorFieldsService.listFields(options.getFileName());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to list color fields", e);
            return 1;
        }
    }

    @Command(name = "list_all",
            description = "Retrieve image color fields and attributes for a list of files.")
    public int listAll(@Mixin JP2ImageConverterOptions options) throws Exception {
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
    public int kduCompress(@Mixin JP2ImageConverterOptions options) throws Exception {
        try {
            kakaduService.setColorFieldsService(colorFieldsService);
            kakaduService.setImagePreproccessingService(imagePreproccessingService);
            kakaduService.kduCompress(options.getFileName(), options.getOutputPath(), options.getSourceFormat());
            imagePreproccessingService.deleteTmpImageFilesDir();
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to generate jp2 file", e);
            return 1;
        }
    }

    @Command(name = "kdu_compress_all",
            description = "Run kakadu kdu_compress on a list of image files.")
    public int kduCompressAll(@Mixin JP2ImageConverterOptions options) throws Exception {
        try {
            kakaduService.setColorFieldsService(colorFieldsService);
            kakaduService.setImagePreproccessingService(imagePreproccessingService);
            kakaduService.fileListKduCompress(options.getFileName(), options.getOutputPath(), options.getSourceFormat());
            imagePreproccessingService.deleteTmpImageFilesDir();
            return 0;
        } catch (Exception e) {
            outputLogger.info("FAIL: {}", e.getMessage());
            log.error("Failed to generate jp2 file. Not processing file list further.", e);
            return 1;
        }
    }
}

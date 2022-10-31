package colorscanner.services;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for kakadu
 * @author krwong
 */
public class KakaduService {
    private static final Logger log = getLogger(KakaduService.class);

    private ColorFieldsService colorFieldsService;

    /**
     * Get ColorSpace from color fields service
     */
    public String getColorSpace(String fileName) throws Exception {
        List<String> fields = colorFieldsService.colorFields(fileName);
        String colorSpace = fields.get(2).split(":")[1];
        return colorSpace;
    }

    /**
     * Run kdu_compress and convert tif to jp2
     * @param fileName
     */
    public void kduCompress(String fileName) throws Exception {
        String kduCompress = "kdu_compress";
        String input = "-i";
        String output = "-o";
        String outputFile = fileName.split("\\.")[0] + ".jp2";
        String clevels = "Clevels=6";
        String clayers = "Clayers=6";
        String cprecincts = "\"Cprecincts={256,256},{256,256},{128,128}\"";
        String stiles = "\"Stiles={512,512}\"";
        String corder = "Corder=RPCL";
        String orggenplt = "ORGgen_plt=yes";
        String orgtparts = "ORGtparts=R";
        String cblk = "\"Cblk={64,64}\"";
        String cusesop = "Cuse_sop=yes";
        String cuseeph = "Cuse_eph=yes";
        String flushPeriod = "-flush_period";
        String flushPeriodOptions = "1024";
        String rate = "-rate";
        String rateOptions = "3";
        String jp2Space = "";
        String jp2SpaceOptions = "";

        // get color space from colorFields
        String colorSpace = getColorSpace(fileName);
        // for grayscale images: add jp2Space to command
        if (colorSpace.toLowerCase().contains("gray")) {
            jp2Space = "-jp2_space";
            jp2SpaceOptions = "sLUM";
        }

        String[] command = {kduCompress, input, fileName, output, outputFile, clevels, clayers, cprecincts, stiles,
                corder, orggenplt, orgtparts, cblk, cusesop, cuseeph, flushPeriod, flushPeriodOptions,
                rate, rateOptions, jp2Space, jp2SpaceOptions};

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            String cmdOutput = "";
            while ((line = br.readLine()) != null) {
                cmdOutput = cmdOutput + line;
            }
            System.out.println(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate jp2 file.", e);
        }

    }
    /**
     * Iterate through list of image files and run kdu_compress to convert all tifs to jp2s
     * @param fileName
     */
    public void fileListKduCompress(String fileName) throws Exception {
        List<String> listOfFiles = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);

        Iterator<String> itr = listOfFiles.iterator();
        while (itr.hasNext()) {
            String imageFileName = itr.next();
            if (Files.exists(Paths.get(imageFileName))) {
                kduCompress(imageFileName);
            } else {
                throw new Exception(imageFileName + " does not exist. Not processing file list further.");
            }
        }
    }

    public void setColorFieldsService(ColorFieldsService colorFieldsService) {
        this.colorFieldsService = colorFieldsService;
    }
}

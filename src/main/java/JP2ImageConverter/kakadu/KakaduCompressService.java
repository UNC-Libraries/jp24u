package JP2ImageConverter.kakadu;

import kdu_jni.Jp2_channels;
import kdu_jni.Jp2_colour;
import kdu_jni.Jp2_dimensions;
import kdu_jni.Jp2_family_tgt;
import kdu_jni.Jp2_palette;
import kdu_jni.Jp2_resolution;
import kdu_jni.Jp2_target;
import kdu_jni.Jpx_layer_target;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_compressed_target;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_cplex_bkgnd_store;
import kdu_jni.Kdu_dims;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_membroker;
import kdu_jni.Kdu_message_formatter;
import kdu_jni.Kdu_params;
import kdu_jni.Kdu_platform_file_target;
import kdu_jni.Kdu_push_pull_params;
import kdu_jni.Kdu_roi_image;
import kdu_jni.Kdu_thread_entity_affinity;
import kdu_jni.Siz_params;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

import static kdu_jni.Kdu_global.KDU_AUTO_COMMENT_VERSION;
import static kdu_jni.Kdu_global.KDU_SLOPE_HINT_DEFAULT;
import static kdu_jni.Kdu_global.KDU_SLOPE_HINT_ESTIMATE;
import static kdu_jni.Kdu_global.Mcomponents;
import static kdu_jni.Kdu_global.Nprecision;
import static kdu_jni.Kdu_global.Nsigned;
import static kdu_jni.Kdu_global.Scomponents;
import static kdu_jni.Kdu_global.Sdims;
import static org.slf4j.LoggerFactory.getLogger;

public class KakaduCompressService {
    private static final Logger log = getLogger(KakaduCompressService.class);

    private void parse_simple_args(List<String> args, String ofname, Kdu_membroker membroker,
                                   boolean transpose, boolean vflip, boolean hflip,
                                   int flush_period,
                                   double rate_tolerance, boolean trim_to_rate,
                                   boolean allow_slope_prediction,
                                   boolean allow_periodic_trimming,
                                   boolean allow_shorts, boolean no_info, boolean no_version,
                                   boolean no_weights, boolean grey_weights, int chroma_weights,
                                   boolean rgb_to_420, boolean no_palette, int num_jpx_layers,
                                   int num_threads, int num_xform_threads,
                                   int num_coding_threads, int pref_split_width,
                                   int max_split_depth, int max_split_frags,
                                   int double_buffering_height, int progress_interval,
                                   int cpu_iterations, boolean bc_noopt, boolean mem,
                                   boolean quiet) {
        int rotate;
        kdc_file_binding * files, *last_file, *new_file;

        files = last_file = null;
        ofname = null;
        membroker = null;
        rotate = 0;
        hflip = false;
        vflip = false;
        transpose = false;
        flush_period = 1024;
        rate_tolerance = 0.02;
        trim_to_rate = false;
        allow_slope_prediction = true;
        allow_periodic_trimming = true;
        allow_shorts = true;
        no_info = false;
        no_version = false;
        no_weights = false;
        grey_weights = false;
        chroma_weights = 0; // Option enabled only if value is 1, 2 or 3
        rgb_to_420 = false;
        no_palette = false;
        num_jpx_layers = 1;
        num_threads = 0; // This is not actually the default -- see below.
        num_xform_threads = num_coding_threads = 0;
        pref_split_width = -1; // Negative values mean that the codestream's default
        max_split_depth = -1;  // splitting policy will not be overridden.
        max_split_frags = -1;
        double_buffering_height = 0; // no double buffering
        progress_interval = 0; // Don't provide any progress indication at all
        cpu_iterations = -1;
        bc_noopt = false;
        mem = false;
        quiet = false;

        if (args.find("-i") != null) {
            char inputFile;
            String cp;
            String name_end;
            int len;

            if ((inputFile = args.advance()) == null) {
            // no java equivalent for kdu_error
                kdu_error e;
                log.error("\"-i\" argument requires a file name!");
            }
            while ((len = (int) strlen(inputFile)) > 0) {
                cp = strchr(inputFile, ',');
                if (cp == null)
                    cp = inputFile + len;
                for (name_end = inputFile; name_end < cp; name_end++)
                    if (*name_end == '*')
                break;
                int num_copies = 1, copy_size = 0;
                if (name_end < cp) {
                    if ((sscanf(name_end, "*%d@%d", & num_copies,&copy_size) !=2) ||
                    (num_copies < 1) || (copy_size < 1))
                    // no java equivalent for kdu_error
                    {
                        kdu_error e;
                        log.error("Malformed copy replicator suffix found "
                            + "within file name in the comma-separated list supplied "
                            + "with the \"-i\" argument.  Copy replicator suffices "
                            + "must have the form \"*<copies>@<copy size>\".");
                    }
                }
                kdu_long copy_offset = 0;
                for (; num_copies > 0; num_copies--, copy_offset += copy_size) {
                    // kdc_file_binding in /apps/kdu_compress/compress_local.h
                    new_file = new kdc_file_binding(string, (int) (name_end - string),
                            copy_offset);
                    if (last_file == null)
                        files = last_file = new_file;
                    else
                        last_file = last_file -> next = new_file;
                }
                if (*cp == ',')cp++;
                string = cp;
            }
            args.advance();
        }

        if (args.find("-o") != null) {
            char *string = args.advance();
            if (string == null) {
            // no java equivalent for kdu_error
                kdu_error e;
                log.error("\"-o\" argument requires a file name!");
            }
            ofname = new char[strlen(string) + 1];
            strcpy(ofname, string);
            args.advance();
        }

        if (args.find("-flush_period") != null) {
            char *string = args.advance();
            if ((string == null) || (sscanf(string, "%d", & flush_period) != 1) ||
            (flush_period < 1)) {
                // no java equivalent for kdu_error
                kdu_error e;
                log.error("\"-flush_period\" argument requires a positive integer parameter.");
            }
            args.advance();
        }


        if (args.find("-no_weights") != null) {
            no_weights = true;
            args.advance();
        }

        if (args.find("-no_palette") != null) {
            no_palette = true;
            args.advance();
        }

        if (files == null) {
            //throw new KduException()
            // no java equivalent for kdu_error
            kdu_error e;
            log.error("Must provide one or more input files!");
        }
    }

    public void set_jp2_attributes(Jp2_dimensions dims, Jp2_palette pclr,
                       Jp2_resolution res, Jp2_channels channels,
                       Jp2_colour colr, Siz_params siz,
                       kdu_rgb8_palette &palette,
                       int num_components, kdu_args &args,
                       Jpx_layer_target jpx_layer,
                       kdu_image_dims idims, boolean transpose,
                       boolean doing_rgb_to_420_conversion)
  /* The return value, if non-null, represents the comma-separated list of
     extra JP2 box files obtained from a `-jp2_box' argument.
        If `jpx_layer.exists()' returns true, the JP2 attributes are being
     prepared for a JPX file.  In this case, additional colour space
     information may be supplied via a "-jpx_space" command-line argument.
       The `transpose' is true, we must transpose any resolution informtion
     found in `idims' -- this is the only reason for supplying the argument. */
    {
        char *extra_box_files = null;
        // Set dimensional information (all redundant with the SIZ marker segment)
        dims.init(siz);

        // Set resolution information (optional)
        if (args.find("-jp2_aspect") != null) {
            float aspect_ratio=0.0F;
            char *string = args.advance();
            if ((string == null) ||
                    (sscanf(string,"%f",&aspect_ratio) != 1) ||
            (aspect_ratio <= 0.0F))
            { kdu_error e; e << "Missing or illegal aspect ratio "
                "parameter supplied with the `-jp2_aspect' argument!"; }
            args.advance();
            res.init(aspect_ratio);
        } else {
            // See if `idims' contains any resolution information
            boolean units_known;
            double xpels_per_metre, ypels_per_metre;
            if (idims.get_resolution(0,units_known,xpels_per_metre,ypels_per_metre)) {
                if (transpose) {
                    double tmp = xpels_per_metre;
                    xpels_per_metre = ypels_per_metre;  ypels_per_metre = tmp;
                }
                int xfac=0, yfac=0;
                siz->get(Ssampling,0,0,yfac);  siz->get(Kdu_global.Ssampling,0,1,xfac);
                assert((xfac > 0) && (yfac > 0));
                xpels_per_metre *= xfac;  ypels_per_metre *= yfac;
                res.init((float)(xpels_per_metre/ypels_per_metre));
                if (units_known) {
                    res.set_resolution((float) ypels_per_metre, false);
                }
            }
        }

        // Check for JPH non-colour bindings.
        int num_non_colours=0;
        kdu_uint16 non_colour_keys = null;
        if (args.find("-jph_non_space") != null)
        {
            char *string = args.advance();
            if (string == null)
            { kdu_error e; e << "The `-jph_non_space' argument requires a "
                "parameter string!"; }

            // First count the number of non-colours
            char *scan = string;
            do {
                char *endp=scan;
                strtoul(scan,&endp,16);
                if (endp != (scan+4))
                { kdu_error e; e << "The `-jph_non_space' argument requires a "
                    "comma-separated sequence of one or more 4-character hex "
                    "keys.  Error at:\n\t\"" << string << "\"."; }
                scan = endp;
                num_non_colours++;
                if (*scan == ',')
                scan++; // Skip over the delimiter
            } while (*scan != '\0');

            // Now allocate the array for keys and assign them
            non_colour_keys = new kdu_uint16[num_non_colours];
            int nc=0;
            scan = string;
            do {
                char *endp=scan;
                non_colour_keys[nc++] = (kdu_uint16) strtoul(scan,&endp,16);
                scan = endp;
                if (*scan != '\0')
                scan++; // Skip over the delimiter
            } while (*scan != '\0');
            assert(nc == num_non_colours);
            args.advance();
        }

        // Set colour space information
        boolean have_opponent_space=false;
        boolean have_non_opponent_space=false;
        int min_colours=(num_non_colours)?0:1;
        int max_colours=num_components-num_non_colours;
        if (max_colours < 0) {
            // no java equivalent for kdu_error
            kdu_error e; e << "Insufficient source components to support "
            "the bindings specified via \"-jph_non_space\".";
        }
        if (palette.exists()) {
            if (num_non_colours) {
                // no java equivalent for kdu_error
                kdu_error e; e << "This demo-app does not currently support "
                "the use of non-colour bindings (\"-jph_non_space\") in "
                "conjunction with palettized source data.";
            } if (palette.source_component == 0) {
                min_colours = max_colours = (palette.is_monochrome()) ? 1 : 3;
            } else {
                max_colours = palette.source_component;
            }
        }

        boolean have_premultiplied_alpha, have_unassociated_alpha;
        jp2_colour_space in_space;
        int in_space_confidence;
        int in_profile_len;
        int in_colours = // Will be zero if the file reader does not know # colours
                idims.get_colour_info(have_premultiplied_alpha,have_unassociated_alpha,
                        in_space_confidence,in_space,in_profile_len);
        if (doing_rgb_to_420_conversion && (max_colours >= 3))
        {
            if (in_profile_len > 0) {
                // no java equivalent for kdu_warning
                kdu_warning w; w << "ICC profile from input file being discarded "
                "due to conflicts with the \"-rgb_to_420\" option."; }
            in_profile_len = 0;
            in_colours = 3;
            in_space_confidence = 1;
            in_space = JP2_sYCC_SPACE;
        }
    const kdu_byte *in_profile=null;
        if (in_profile_len > 0) {
            in_profile = idims.get_profile();
        }

        if (args.find("-jp2_space") != null) {
            char *string = args.advance();
            if (string == null) {
            // no java equivalent for kdu_error
                kdu_error e; e << "The `-jp2_space' argument requires a parameter "
                "string!";
            }
            char *delim = strchr(string,',');
            if (delim != null) {
                *delim = '\0';
            }
            if (strcmp(string,"sRGB") == 0) {
                colr.init(Kdu_global.JP2_sRGB_SPACE);
            } else if (strcmp(string,"sYCC") == 0) {
                colr.init(Kdu_global.JP2_sYCC_SPACE);
            } else if (strcmp(string,"sLUM") == 0) {
                colr.init(Kdu_global.JP2_sLUM_SPACE);
            } else if (strcmp(string,"iccLUM") == 0) {
                double gamma=1.0, beta=0.0;
                string = delim+1;
                if ((delim == null) || ((delim = strchr(string,',')) == null) ||
                        ((*delim = '\0') > 0) || (sscanf(string,"%lf",&gamma) != 1) ||
                (sscanf(delim+1,"%lf",&beta) != 1) || (gamma <= 1.0) ||
                    (beta < 0.0) || (beta >= 1.0))
                { kdu_error e; e << "Missing or illegal gamma/beta parameters "
                    "supplied in comma-separated parameter list which must follow "
                    "the \"sLUM\" JP2 colour space specification supplied via the "
                    "`-jp2_space' argument.  `gamma' must be greater than 1 and "
                    "`beta' must be in the range 0 to 1."; }
                colr.init(gamma,beta);
            } else if (strcmp(string,"iccRGB") == 0) {
                double val, gamma=0.0, beta=0.0, xy_red[2], xy_green[2], xy_blue[2];
                booleanilluminant_is_D50=false;
                int p;
                for (p=0; p < 8; p++) {
                    string = delim+1;
                    if ((delim == null) ||
                            ((delim = strchr(string,',')) == null) ||
                            ((*delim = '\0') > 0) ||
                    (sscanf(string,"%lf",&val) != 1)) {
                        // no java equlivanet for kdu_error
                        kdu_error e; e << "The \"iccRGB\" specification must be "
                        "followed immediately by a comma-separated list of 9 "
                        "parameters, all within the single parameter string "
                        "supplied with the `-jp2_space' argument.  For more details "
                        "review the usage statement."; }
                    if (p == 0) {
                        gamma = val;
                    } else if (p == 1) {
                        beta = val;
                    } else if (p < 4) {
                        xy_red[p - 2] = val;
                    } else if (p < 6) {
                        xy_green[p - 4] = val;
                    } else if (p < 8) {
                        xy_blue[p - 6] = val;
                    }
                }
                string = delim + 1;
                if (delim == null) {
                    // no java equivalent for kdu_error
                    kdu_error e; e << "The \"iccRGB\" specification must be "
                    "followed by a list of 9 parameters, the last of which is "
                    "one of the strings \"D50\" or \"D65\".";
                } else if (strcmp(string,"D50") == 0) {
                    illuminant_is_D50 = true;
                } else if (strcmp(string,"D65") == 0) {
                    illuminant_is_D50 = false;
                } else {
                    // no java equivalent for kdu_error
                    kdu_error e; e << "The \"iccRGB\" specification must be "
                    "followed by a list of 9 parameters, the last of which is "
                    "one of the strings \"D50\" or \"D65\"."; }
                for (p=0; p < 2; p++) {
                    if ((beta < 0.0) || (beta >= 1.0) || (gamma <= 1.0) ||
                            (xy_red[p] < 0.0) || (xy_red[p] > 1.0) ||
                            (xy_green[p] < 0.0) || (xy_green[p] > 1.0) ||
                            (xy_blue[p] < 0.0) || (xy_blue[p] > 1.0)) {
                        // no java equivalent for kdu_error
                        kdu_error e;
                        e << "One or more parameters supplied with the "
                        "\"iccRGB\" `-jp2_space' argument lie outside the legal "
                        "range.";
                    }
                }
                colr.init(xy_red,xy_green,xy_blue,gamma,beta,100,illuminant_is_D50);
            } else {
                kdu_error e; e << "Invalid parameter string following `-jp2_space' "
                "argument.  The string must identify the colour space as one of "
                "\"sLUM\", \"sRGB\", \"sYCC\", \"iccLUM\" or \"iccRGB\".";
            }
            args.advance();

            if ((colr.get_num_colours() > max_colours) || (colr.get_num_colours() < min_colours)) {
                // no java equivalnet for kdu_error
                kdu_error e; e << "The number of colours associated with the "
                "colour space specified using `-jp2_space' are not compatible "
                "with the number of supplied image components and/or colour "
                "palette.";
            }
            min_colours = max_colours = colr.get_num_colours();

            if (colr.is_opponent_space()) {
                have_opponent_space = true;
            } else {
                have_non_opponent_space = true;
            }
            colr = jp2_colour(null); // So we know that colour space is initialized
        }

        if (args.find("-jpx_space") != null) {
            char *delim, *string = args.advance();
            if (string == null) {
                // no java equivalent for kdu_error
                kdu_error e; e << "The `-jpx_space' argument requires a parameter "
                "string!"; }
            if (!jpx_layer) {
                // no java equivalent for kdu_error
                kdu_error e; e << "The `-jpx_space' argument may only be used "
                "with JPX files -- i.e., your output file must have either a "
                "`.jpx' or `.jpf' suffix."; }
            int prec=0, approx=0;
            jp2_colour_space space=JP2_sLUM_SPACE;
            delim = strchr(string,',');
            if (delim != null) {
                *(delim++) = '\0';
            }
            if (strcmp(string,"bilevel1") == 0) {
                space = Kdu_global.JP2_bilevel1_SPACE;
            } else if (strcmp(string,"bilevel2") == 0) {
                space = Kdu_global.JP2_bilevel2_SPACE;
            } else if (strcmp(string,"YCbCr1") == 0) {
                space = Kdu_global.JP2_YCbCr1_SPACE;
            } else if (strcmp(string,"YCbCr2") == 0) {
                space = Kdu_global.JP2_YCbCr2_SPACE;
            } else if (strcmp(string,"YCbCr3") == 0) {
                space = Kdu_global.JP2_YCbCr3_SPACE;
            } else if (strcmp(string,"PhotoYCC") == 0) {
                space = Kdu_global.JP2_PhotoYCC_SPACE;
            } else if (strcmp(string,"CMY") == 0) {
                space = Kdu_global.JP2_CMY_SPACE;
            } else if (strcmp(string,"CMYK") == 0) {
                space = Kdu_global.JP2_CMYK_SPACE;
            } else if (strcmp(string,"YCCK") == 0) {
                space = Kdu_global.JP2_YCCK_SPACE;
            } else if (strcmp(string,"CIELab") == 0) {
                space = Kdu_global.JP2_CIELab_SPACE;
            } else if (strcmp(string,"CIEJab") == 0) {
                space = Kdu_global.JP2_CIEJab_SPACE;
            } else if (strcmp(string,"sLUM") == 0) {
                space = Kdu_global.JP2_sLUM_SPACE;
            } else if (strcmp(string,"sRGB") == 0) {
                space = Kdu_global.JP2_sRGB_SPACE;
            } else if (strcmp(string,"sYCC") == 0) {
                space = Kdu_global.JP2_sYCC_SPACE;
            } else if (strcmp(string,"esRGB") == 0) {
                space = Kdu_global.JP2_esRGB_SPACE;
            } else if (strcmp(string,"esYCC") == 0) {
                space = Kdu_global.JP2_esYCC_SPACE;
            } else if (strcmp(string,"ROMMRGB") == 0) {
                space = Kdu_global.JP2_ROMMRGB_SPACE;
            } else if (strcmp(string,"YPbPr60_SPACE") == 0) {
                space = Kdu_global.JP2_YPbPr60_SPACE;
            } else if (strcmp(string,"YPbPr50_SPACE") == 0) {
                space = Kdu_global.JP2_YPbPr50_SPACE;
            } else {
                // no java equivalent for kdu_error
                kdu_error e; e << "Unrecognized colour space type, \""
                    << string << "\", provided with `-jpx_space' argument."; }
            if ((delim != null) &&
                    ((sscanf(delim,"%d,%d",&prec,&approx) != 2) ||
            (prec < -128) || (prec > 127) || (approx < 0) || (approx > 4))) {
                kdu_error e; e << "Illegal or incomplete precedence/approximation "
                "information provided with `-jpx_space' argument."; }
            if (!colr.exists()) {
                colr = jpx_layer.add_colour(prec, (kdu_byte) approx);
            }
            colr.init(space);
            args.advance();

            if ((colr.get_num_colours() > max_colours) ||
                    (colr.get_num_colours() < min_colours)) {
                kdu_error e; e << "The number of colours associated with the "
                "colour space specified using `-jpx_space' are not compatible "
                "with the number of supplied image components and/or colour "
                "palette, or with the number of colours in a supplied "
                "`-jp2_space'.";
            }
            min_colours = max_colours = colr.get_num_colours();

            if (colr.is_opponent_space()) {
                have_opponent_space = true;
            } else {
                have_non_opponent_space = true;
            }
            colr = jp2_colour(null); // So we know that colour space is initialized
        }

        if (colr.exists() && ((in_space_confidence > 0) || (in_profile != null))) {
            // Colour space specification derived from the source file
            if (in_profile != null) {
                colr.init(in_profile);
            } else {
                colr.init(in_space);
            }
            if ((colr.get_num_colours() > max_colours) ||
                    (colr.get_num_colours() < min_colours)) {
                // no java equivalent for kdu_error
                kdu_error e; e << "The number of colours associated with the "
                "colour space identified by the source file (possible from an "
                "embedded ICC profile) is not consistent with the number of "
                "supplied image components and/or colour palette.  You can "
                "address this problem by supplying a `-jp2_space' or "
                "`-jpx_space' argument to explicitly identify a colour space that "
                "has anywhere from " << min_colours << " to " << max_colours <<
                        " colour components."; }
            min_colours = max_colours = colr.get_num_colours();
            if (colr.is_opponent_space()) {
                have_opponent_space = true;
            } else {
                have_non_opponent_space = true;
            }
            colr = jp2_colour(null); // So we know that colour space is initialized
        }

        if (have_opponent_space && have_non_opponent_space) {
            kdu_error e; e << "You have specified multiple colour specifications, "
            "where one specification represents an opponent colour space, while "
            "the other does not.  This contradictory information leaves us "
            "uncertain as to whether the code-stream colour transform should be "
            "used or not, but is almost certainly a mistake anyway.";
        }

        // Set the actual number of colour planes and the index of any alpha
        // component
        int opacity_idx=-1;
        if (palette.exists()) {
            opacity_idx = palette.source_component;
        }
        if (in_colours > 0) {
            // Source image file identifies the number of colours
            if (have_premultiplied_alpha && (opacity_idx < 0))
                opacity_idx = in_colours+num_non_colours;
            if ((min_colours > in_colours) || (max_colours < in_colours)) {
                // no java equivalent for kdu_warning
                kdu_warning w; w << "The number of colour planes identified by the "
                "image file format reading logic is not consistent with the "
                "indicated colour space, with the number of available image "
                "components, with the use of a colour palette, or with the use "
                "of \"-jp2_non_space\".";
                if (have_premultiplied_alpha && (args.find("-jp2_alpha") == null)) {
                    have_premultiplied_alpha = false;
                    // no java equivalent for kdu_warning
                    kdu_warning w; w << "Since you have specified a different "
                    "number of colours to that indicated by the file, the "
                    "premultiplied alpha channel embedded in the file will not "
                    "be regarded as an alpha channel unless you explicitly "
                    "supply the `-jp2_alpha' switch to confirm that this is "
                    "really what you want.  The alpha channel will be taken from "
                    "component " << opacity_idx << " (starting from 0), which "
                    "may or may not conflict with the use of components for "
                    "your colour space.";
                }
            } else {
                min_colours = max_colours = in_colours;
            }
        }

        int num_colours = max_colours;
        if (max_colours > min_colours) {
            // Actual number of colours is not known; we can make up our own mind
            if (num_non_colours > 0) {
                num_colours = 0; // Don't add any colour space; we have non-colours
            } else {
                assert(min_colours == 1);
                num_colours = (max_colours < 3)?1:3;
            }
        }
        if (opacity_idx < 0) {
            opacity_idx = num_colours + num_non_colours;
        }

        if (colr.exists()) {
            // Still have not initialized the colour space yet
            if (num_colours > 0) {
                colr.init((num_colours == 3) ? JP2_sRGB_SPACE : JP2_sLUM_SPACE);
            }
            colr = jp2_colour(null);
        }

        // Check for alpha support
        if (args.find("-jp2_alpha") != null) {
            args.advance();
            if (!have_premultiplied_alpha) {
                have_unassociated_alpha = true;
            }
        }

        // Set the colour palette and channel mapping as needed.
        if (palette.exists() && (palette.source_component == 0)) {
            assert(num_non_colours == 0); // We ensured this up above
            if ((have_unassociated_alpha || have_premultiplied_alpha) &&
                    (opacity_idx >= num_components)) {
                // no java equivalent for kdu_error
                kdu_error e; e << "The `-jp2_alpha' argument or the image "
                "file header itself suggest that there should be an alpha "
                "component.  Yet there are not sufficient image components "
                "available to accommodate an alpha channel."; }
            if (palette.is_monochrome()) {
                pclr.init(1,1<<palette.input_bits);
                pclr.set_lut(0,palette.red,palette.output_bits);
                assert(num_colours == 1);
                channels.init(1);
                channels.set_colour_mapping(0,palette.source_component,0);
                if (have_unassociated_alpha) {
                    channels.set_opacity_mapping(0, opacity_idx);
                } else if (have_premultiplied_alpha) {
                    channels.set_premult_mapping(0, opacity_idx);
                }
            } else {
                pclr.init(3,1<<palette.input_bits);
                pclr.set_lut(0,palette.red,palette.output_bits);
                pclr.set_lut(1,palette.green,palette.output_bits);
                pclr.set_lut(2,palette.blue,palette.output_bits);
                assert(num_colours == 3);
                channels.init(3);
                channels.set_colour_mapping(0,palette.source_component,0);
                channels.set_colour_mapping(1,palette.source_component,1);
                channels.set_colour_mapping(2,palette.source_component,2);
                if (have_unassociated_alpha) {
                    channels.set_opacity_mapping(-1, opacity_idx);
                } else if (have_premultiplied_alpha) {
                    channels.set_premult_mapping(-1, opacity_idx);
                }
            }
        } else if (have_unassociated_alpha || have_premultiplied_alpha) {
            if (opacity_idx >= num_components) {
                // no java equivalent for kdu_error
                kdu_error e; e << "The `-jp2_alpha' argument or the image "
                "file header itself suggest that there should be an alpha "
                "component.  Yet there are not sufficient image components "
                "available to accommodate an alpha channel.";
            }
            channels.init(num_colours,num_non_colours);
            int lut_idx = -1;
            if (palette.exists() && (palette.source_component == opacity_idx)) {
                assert(num_non_colours == 0); // We ensured this up above
                pclr.init(1,1<<palette.input_bits);
                pclr.set_lut(0,palette.red,palette.output_bits);
                lut_idx = 0;
            }
            if (have_unassociated_alpha) {
                channels.set_opacity_mapping(-1, opacity_idx, lut_idx);
            } else {
                channels.set_premult_mapping(-1, opacity_idx, lut_idx);
            }
            for (int c=0; c < num_colours; c++) {
                int format=JP2_CHANNEL_FORMAT_DEFAULT, format_exp_bits=0;
                boolean align_lsbs=false;
                if (idims.get_forced_precision(c,align_lsbs,format_exp_bits) && format_exp_bits > 0) {
                    format = JP2_CHANNEL_FORMAT_FLOAT;
                }
                channels.set_colour_mapping(c,c,-1,0,format,&format_exp_bits);
            }
        } else {
            channels.init(num_colours,num_non_colours);
            for (int c=0; c < num_colours; c++) {
                int format=JP2_CHANNEL_FORMAT_DEFAULT, format_exp_bits=0;
                boolean align_lsbs=false;
                if (idims.get_forced_precision(c,align_lsbs,format_exp_bits) &&
                        format_exp_bits > 0)
                    format = JP2_CHANNEL_FORMAT_FLOAT;
                channels.set_colour_mapping(c,c,-1,0,format,&format_exp_bits);
            }
        }
        for (int nc=0; nc < num_non_colours; nc++)
        {
            int c = num_colours+nc;
            int format=JP2_CHANNEL_FORMAT_DEFAULT, format_exp_bits=0;
            boolean align_lsbs=false;
            if (idims.get_forced_precision(c,align_lsbs,format_exp_bits) &&
                    format_exp_bits > 0)
                format = JP2_CHANNEL_FORMAT_FLOAT;
            channels.set_non_colour_mapping(nc,non_colour_keys[nc],c,-1,0,
                    format,&format_exp_bits);
        }
        if (non_colour_keys != null)
            delete[] non_colour_keys;

        // Find extra JP2 boxes.
        if (args.find("-jp2_box") != null) {
            char *string = args.advance();
            if (string == null {
                // no java equivalent for kdu_error
                kdu_error e; e << "The `-jp2_box' argument requires a parameter "
                    "string!";
            }
            extra_box_files = new char[strlen(string)+1];
            strcpy(extra_box_files,string);
            args.advance();
        }

        return extra_box_files;
    }

    public static kdu_long compress_multi_threaded(kdu_codestream codestream, kdu_dims tile_indices,
                            kdc_file_binding *inputs, boolean convert_rgb_to_420,
                            kdu_roi_image *roi_source,
                            kdu_long *layer_bytes, int num_layer_specs,
                            kdu_uint16 *layer_thresholds,
                            boolean record_info_in_comseg, double rate_tolerance,
                            boolean trim_to_rate, boolean allow_shorts,
                            int flush_period,
                            kdu_thread_entity_affinity &affinity,
                            int &num_threads, int &num_xform_threads,
                            int &num_coding_threads, boolean dwt_double_buffering,
                            int dwt_stripe_height, int progress_interval,
                            kdu_membroker *membroker,
                            kdu_push_pull_params *extra_params)
  /* This function provides exactly the same functionality as
     `compress_single_threaded', except that it uses Kakadu's multi-threaded
     processing features.  By and large, multi-threading does not substantially
     complicate the implementation, since Kakadu's threading framework
     conceal almost all of the details.  However, the application does have
     to create a multi-threaded environment, assigning it a suitable number
     of threads.  It must also be careful to close down the multi-threaded
     environment, which incorporates all required synchronization.  Finally,
     where incremental flushing of the codestream is required, this is best
     achieved by registering synchronized jobs with the multi-threading
     environment, rather than explicitly synchronizing all threads and then
     running the flush operation directly.
        On entry, `affinity' contains a single thread bundle that has been
     bound to a reasonable set of logical CPUs (usually the first NUMA
     processing node) or else has no CPU binding at all (empty affinity mask),
     and `affinity.get_total_threads()' identifies the total number of threads
     we would like to use for procesing, while `num_xform_threads' and
     `num_coding_threads' identify any threads we would like to dedicate
     specifically to transform and coding, respectively.
        Upon return, `affinity' has between 1 and 3 thread bundles, with the
     total number of threads we would like to use, unless fewer could be
     created.  The first bundle contains general purpose threads, starting
     with the main thread; the second describes threads that prefer to do
     transform processing, while the third describes threads that prefer to
     do coding.  Each set of threads is bound to a separate set of logical
     CPU's, unless `affinity' entered without any affinity bindings.  The
     `num_threads' member is always set to the actual total number of threads
     that are used, while `num_xform_threads' and `num_coding_threads' members
     are also updated to reflect the number of threads (if any) in the second
     and third bundles.
        For other aspects of the present function, refer to the comments
     found with `compress_single_threaded'. */
    {
        // Construct multi-threaded processing environment if required
        kdu_thread_env env;
        env.create();
        int affinity_context=0;
        kdu_int64 affinity_mask=0;
        num_threads = affinity.get_bundle_affinity(0,affinity_mask,affinity_context);
        if (num_threads < 1) num_threads = 1;
        kdu_int64 general_mask=0, xform_mask=0, coding_mask=0;
        int num_general_threads = (num_threads - num_xform_threads - num_coding_threads);
        while (num_general_threads < 1) {
            if (num_xform_threads > 0)
            { num_xform_threads--; num_general_threads++; }
            if (num_coding_threads > 0)
            { num_coding_threads--; num_general_threads++; }
        }
        int t;
        boolean unbound = ((num_threads < 2) || (affinity_mask == 0));
        kdu_int64 affinity_bit=1;
        for (t=num_general_threads; (t > 0) && !unbound; t--) {
            while (affinity_bit && !(affinity_bit & affinity_mask)) {
                affinity_bit <<= 1;
            }
            unbound = (affinity_bit == 0); // Insufficient CPU's; avoid affinity
            general_mask |= affinity_bit;  affinity_bit <<= 1;
        }
        for (t=num_xform_threads; (t > 0) && !unbound; t--) {
            while (affinity_bit && !(affinity_bit & affinity_mask)) {
                affinity_bit <<= 1;
            }
            unbound = (affinity_bit == 0); // Insufficient CPU's; avoid affinity
            xform_mask |= affinity_bit;  affinity_bit <<= 1;
        }
        for (t=num_coding_threads; (t > 0) && !unbound; t--) {
            while (affinity_bit && !(affinity_bit & affinity_mask)) {
                affinity_bit <<= 1;
            }
            unbound = (affinity_bit == 0); // Insufficient CPU's; avoid affinity
            coding_mask |= affinity_bit;  affinity_bit <<= 1;
        }
        while ((affinity_bit != 0) && !unbound) {
            // Distribute remaining logical CPUs in round-robbin fashion
            while (affinity_bit && !(affinity_bit & affinity_mask)) {
                affinity_bit <<= 1;
            }
            general_mask |= affinity_bit;  affinity_bit <<= 1;
            if (num_xform_threads != 0) {
                while (affinity_bit && !(affinity_bit & affinity_mask)) {
                    affinity_bit <<= 1;
                }
                xform_mask |= affinity_bit;  affinity_bit <<= 1;
            }
            if (num_coding_threads != 0) {
                while (affinity_bit && !(affinity_bit & affinity_mask)) {
                    affinity_bit <<= 1;
                }
                coding_mask |= affinity_bit;  affinity_bit <<= 1;
            }
        }
        if (unbound) {
            // Not enough affinity bindings on input: don't use affinity masks at all
            general_mask = 0;
            xform_mask = 0;
            coding_mask = 0;
        }
        affinity.reset(); // Prepare to build new bundles
        affinity.add_thread_bundle(num_general_threads,general_mask,
                affinity_context);
        if (num_xform_threads > 0) {
            affinity.add_thread_bundle(num_xform_threads, xform_mask,
                    affinity_context);
        }
        if (num_coding_threads > 0) {
            affinity.add_thread_bundle(num_coding_threads, coding_mask,
                    affinity_context);
        }
        if (!unbound) {
            env.set_cpu_affinity(affinity);
        }
        for (t=num_general_threads-1; t > 0; t--) {
            if (!env.add_thread())
                break;
        }
        num_general_threads -= t; // Perhaps we failed to add all of them
        for (t=num_xform_threads; t > 0; t--) {
            if (!env.add_thread(Kdu_global.KDU_TRANSFORM_THREAD_DOMAIN)) {
                break;
            }
        }
        num_xform_threads -= t; // Perhaps we failed to add all of them
        for (t=num_coding_threads; t > 0; t--) {
            if (!env.add_thread(Kdu_global.KDU_CODING_THREAD_DOMAIN)) {
                break;
            }
        }
        num_coding_threads -= t; // Perhaps we failed to add all of them
        num_threads = num_general_threads + num_xform_threads + num_coding_threads;

        // Start background tile opening process
        Kdu_dims tiles_to_open=tile_indices, trange=tile_indices;
        trange.size.y = 1; // Schedule opening of first row of tiles
        codestream.open_tiles(trange,true,&env);
        tiles_to_open.pos.y++;
        tiles_to_open.size.y--;

        // Now set up the tile processing objects.
        int x_tnum;
        kdc_flow_control **tile_flows = new kdc_flow_control *[tile_indices.size.x];
        for (x_tnum=0; x_tnum < tile_indices.size.x; x_tnum++) {
            kdu_thread_queue *tile_queue =
                    env.add_queue(null,null,"tile compressor");
            tile_flows[x_tnum] =
                    new kdc_flow_control(inputs,codestream,x_tnum,allow_shorts,
                            convert_rgb_to_420,roi_source,
                            dwt_stripe_height,dwt_double_buffering,
                            &env,tile_queue,membroker,extra_params);
        }

        // Now run the tile processing engines
        boolean done = false;
        int tile_row = 0; // Just for progress counter
        int progress_counter = 0;
        try {
            if ((flush_period > 0) && (flush_period < INT_MAX)) {
                // Set up `auto_flush'.  We have to go to a bit of effort here to
                // convert `flush_period' into a roughly equivalent set of internal
                // auto-flush trigger conditions.  In your application, however, it
                // may be preferable to pass parameters that work for you directly
                // to `auto_flush' without having to derive them from a `flush_period'
                // specification that is at best indicative of when incremental
                // flushing can actually occur.
                int c, num_comps=codestream.get_num_components(true);
                int min_sub_y=0; // Will be min component vertical sub-sampling
                for (c=0; c < num_comps; c++)
                {
                    Kdu_coords subs; codestream.get_subsampling(c,subs,true);
                    if ((min_sub_y == 0) || (min_sub_y > subs.y))
                        min_sub_y = subs.y;
                }
                Kdu_dims t_dims; codestream.get_tile_partition(t_dims);
                int max_tile_lines = 1 + ((t_dims.size.y-1) / min_sub_y);

                kdu_long tc_trigger_interval=1;
                if (flush_period > max_tile_lines)
                    tc_trigger_interval = flush_period / max_tile_lines;
                tc_trigger_interval *= num_comps;
                tc_trigger_interval *= tile_indices.size.x;
                if (tc_trigger_interval > (1<<30))
                    tc_trigger_interval = 1<<30; // Just in case

                kdu_long incr_trigger_interval = 0;
                if ((flush_period+(flush_period>>1)) < max_tile_lines)
                { // Otherwise, don't bother with incremental flush within tile
                    incr_trigger_interval = flush_period * min_sub_y;
                    incr_trigger_interval *= num_comps;
                    incr_trigger_interval *= tile_indices.size.x;
                    if (incr_trigger_interval > (1<<30))
                        incr_trigger_interval = 1<<30; // Just in case
                }

                codestream.auto_flush((int)tc_trigger_interval,
                        (int)tc_trigger_interval,
                        (int)incr_trigger_interval,
                        (int)incr_trigger_interval,
                        layer_bytes,num_layer_specs,layer_thresholds,
                        trim_to_rate,record_info_in_comseg,
                        rate_tolerance,&env,
                        Kdu_global.KDU_FLUSH_USES_THRESHOLDS_AND_SIZES);
            }
            while (!done) {
                if (tiles_to_open.size.y > 0) {
                    // Schedule background opening of next row of tile interfaces
                    trange = tiles_to_open;  trange.size.y = 1;
                    codestream.open_tiles(trange,true,&env);
                    tiles_to_open.pos.y++;  tiles_to_open.size.y--;
                }

                while (!done) {
                    // Process a row of tiles line by line.
                    done = true;
                    for (x_tnum=0; x_tnum < tile_indices.size.x; x_tnum++) {
                        if (tile_flows[x_tnum]->advance_components(&env))
                        {
                            done = false;
                            tile_flows[x_tnum]->process_components(&env);
                        }
                    }
                    if (!done) {
                        if ((++progress_counter) == progress_interval) {
                            pretty_cout << "\t\tProgress with current tile row = "
                                    << tile_flows[0]->percent_pushed()
                                << "%\n";
                            progress_counter = 0;
                        }
                    }
                }

                for (x_tnum=0; x_tnum < tile_indices.size.x; x_tnum++) {
                    if (tile_flows[x_tnum]->advance_tile( & env))
                }
                done = false;

                tile_row++;
                progress_counter = 0;
                if (progress_interval > 0) {
                    pretty_cout << "\tFinished processing " << tile_row
                            << " of " << tile_indices.size.y << " tile rows\n";
                }
            }
        } catch (kdu_exception exc) {
            env.handle_exception(exc); // In this application, it is not actually
            // necessary to catch and handle exceptions, because `kdu_error'
            // does not have an exception-throwing handler -- it just exits the
            // process.  However, if you choose to port this implementation to
            // an application that needs to stay alive, it is important to pay
            // attention to the fact that thrown exceptions should result in a
            // call to `kdu_thread_entity::handle_exception' for maximum robustness.
        }

        // Cleanup processing environment
        env.join(null,true); // Wait until all internal processing is complete
        env.cs_terminate(codestream); // Terminates background codestream processing
        env.destroy();

        kdu_long sample_processing_bytes = 0;
        for (x_tnum=0; x_tnum < tile_indices.size.x; x_tnum++) {
            sample_processing_bytes += tile_flows[x_tnum]->get_buffer_memory();
            delete tile_flows[x_tnum];
        }
        delete[] tile_flows;

        // Final flush
        if (progress_interval) {
            pretty_cout << "\tInitiating final codestream flush ...\n";
        }
        codestream.flush(layer_bytes,num_layer_specs,layer_thresholds,
                trim_to_rate,record_info_in_comseg,rate_tolerance,null,
                Kdu_global.KDU_FLUSH_USES_THRESHOLDS_AND_SIZES);
        return sample_processing_bytes;
    }

    public void setKakaduParams() {
        String clevels = "Clevels=6";
        String clayers = "Clayers=6";
        String cprecincts = "Cprecincts={256,256},{256,256},{128,128}";
        String stiles = "Stiles={512,512}";
        String corder = "Corder=RPCL";
        String orggenplt = "ORGgen_plt=yes";
        String orgtparts = "ORGtparts=R";
        String cblk = "Cblk={64,64}";
        String cusesop = "Cuse_sop=yes";
        String cuseeph = "Cuse_eph=yes";
        String flushPeriod = "-flush_period";
        String flushPeriodOptions = "1024";
        String rate = "-rate";
        String rateOptions = "3";
        String weights = "-no_weights";
        String jp2Space;
        String jp2SpaceOptions;
        String noPalette;

        //Kdu_params params = new Kdu_params();
        //params.Set(Kdu_global.Clevels, 0, 0, 6);
    }

    public void kduCompress(String sourceFileName, Path outputPath) {
        kdu_customize_warnings( & pretty_cout);
        kdu_customize_errors( & pretty_cerr);

        int rotate;
        // kdc_file_binding in /apps/kdu_compress/compress_local.h
        kdc_file_binding * files, *last_file, *new_file;

        files = last_file = null;
        rotate = 0;
        // Collect simple arguments.

        boolean transpose = false;
        boolean vflip = false;
        boolean hflip = false;
        boolean allow_slope_prediction = true;
        boolean allow_periodic_trimming = true;
        boolean trim_to_rate = false;
        boolean allow_shorts = true;
        boolean bc_noopt = false;
        boolean mem = false;
        boolean quiet = false;
        boolean no_info = false;
        boolean no_version = false;
        boolean no_weights = false;
        boolean grey_weights = false;
        boolean rgb_to_420 = false;
        boolean no_palette = false;
        double rate_tolerance = 0.02;
        int chroma_weights = 0;
        int flush_period = 1024;
        int num_jpx_layers = -1;
        int num_threads = 0;
        int num_xform_threads = 0;
        int num_coding_threads = 0;
        int pref_split_width = -1;
        int max_split_depth = -1;
        int max_split_frags = -1;
        int double_buffering_height = 0;
        int progress_interval = 0;
        int cpu_iterations = -1;
        String ofname = null;
        Kdu_membroker membroker = null;
        std::ostream * record_stream = null;
        // kdc_file_binding in /apps/kdu_compress/compress_local.h
        kdc_file_binding * inputs =
                parse_simple_args(args, ofname, membroker, record_stream, transpose, vflip,
                        hflip, flush_period, rate_tolerance, trim_to_rate,
                        allow_slope_prediction, allow_periodic_trimming,
                        allow_shorts, no_info, no_version, no_weights,
                        grey_weights, chroma_weights, rgb_to_420,
                        no_palette, num_jpx_layers, num_threads, num_xform_threads,
                        num_coding_threads, pref_split_width, max_split_depth,
                        max_split_frags, double_buffering_height,
                        progress_interval, cpu_iterations, bc_noopt, mem, quiet);

        // Create appropriate output file format
        Kdu_compressed_target output = null;
        Kdu_platform_file_target file_out;
        //kdc_null_target null_out(membroker);
        Jp2_family_tgt jp2_ultimate_tgt;
        Jp2_target jp2_out = new Jp2_target();
        Jp2_dimensions jp2_family_dimensions;
        Jp2_palette jp2_family_palette;
        Jp2_resolution jp2_family_resolution;
        Jp2_channels jp2_family_channels;
        Jp2_colour jp2_family_colour;
        boolean is_jp2 = true;

        if (ofname != null) {
            delete[] ofname;
            ofname = null;
        }

        // Collect any command-line information concerning the input files.
        Siz_params siz;
        const char string;
        for (string = args.get_first(); string != null; ) {
            if (*string == '-'){
                args.advance(false);
                string = args.advance(false);
            } else {
                string = args.advance(siz.parse_string(string));
                Siz_params siz_scratch;
                Siz_params * input_siz_ref =&siz, *codestream_siz_ref =&siz;
                Kdu_dims fragment_region;
                int fragment_tiles_generated = 0;
                Kdu_long fragment_tile_bytes_generated = 0;
                int fragment_tlm_tparts = 0;
            }
        }

        // Set up input image files, recovering dimensions and precision information
        // from them where we can.  The code below looks a little complex, only
        // because we want to allow for multi-component transforms, as defined in
        // JPEG2000 Part 2.  A multi-component transform is being used if the
        // `Mcomponents' attribute is defined and greater than 0.  In this case,
        // `Mcomponents' identifies the set of image components that will be
        // decoded after applying the multi-component tranform to the `Scomponents'
        // codestream components.
        //    During compression, we supply `num_source_components' source components
        // to the `kdc_flow_control' object, where `num_source_components' is
        // allowed to be less than `Mcomponents' if we believe that the
        // multi-component transform network can be inverted (this is done
        // automatically by `kdu_multi_analysis' on top of which
        // `kdc_flow_control' is built) to produce the `Scomponents' codestream
        // components from the `num_source_components' supplied source components.
        // These source components correspond to the initial `num_source_components'
        // components reconstructed by the decompressor, out of the total
        // `Mcomponents'.  This is why the code below involves three different
        // component counts (`m_components', `c_components' and
        // `num_source_components').
        //    For Part-1 codestreams, `Mcomponents' is 0 and `num_source_components'
        // and `c_components' are identical.  In this case, `Scomponents' can be
        // derived simply by counting the components supplied by the source files,
        // and `Ssigned' and `Sprecision' can be set based on the source file
        // headers (except for raw files).
        //    For Part-2 codestreams, `Mcomponents' is greater than 0 and
        // `Scomponents' must be explicitly set by the application (or by parsing the
        // command line).  If you have `Mcomponents' > 0 and no defined value for
        // `Scomponents', the default `Scomponents' value is set to
        // `num_source_components' (i.e., to the number of components found in the
        // input files).
        //    For all cases, we follow the recommendation documented with the
        // `siz_params' object, according to which precision and signed/unsigned
        // properties of each original image component should be specified via
        // `Nprecision' and `Nsigned' attributes, leaving `Mprecision'/`Msigned'
        // and/or `Sprecision'/`Ssigned' to be determined automatically or explicitly
        // set to accommodate specific attributes of a Part-2 multi-component
        // transform or non-linear point transform.
        int c;
        int c_components = 0; // Will become number of codestream components
        int m_components = 0;  // From an existing `Mcomponents' attribute, if any
        input_siz_ref -> get(Mcomponents, 0, 0, m_components);

        // no java equivalent for kdu_image_dims
        // Initialize component dimensions/precision from `input_siz_ref'
        kdu_image_dims idims; // To exchange component dimensions with `kdu_image_in'
        int siz_rows = -1, siz_cols = -1, siz_precision = -1, siz_signed = -1;
        for (c = 0; input_siz_ref -> get(Sdims, c, 0, siz_rows, false, false) ||
                input_siz_ref -> get(Nprecision, c, 0, siz_precision, false, false) ||
                        input_siz_ref -> get(Nsigned, c, 0, siz_signed, false, false); c++) {
            // Scan components so long as something is explicitly available
            input_siz_ref -> get(Sdims, c, 0, siz_rows);
            input_siz_ref -> get(Sdims, c, 1, siz_cols);
            input_siz_ref -> get(Nprecision, c, 0, siz_precision);
            input_siz_ref -> get(Nsigned, c, 0, siz_signed);
            if ((siz_rows < 0) || (siz_cols < 0) ||
                    (siz_precision < 0) || (siz_signed < 0)) {
                break; // Insufficient information to create a complete record for
                // even one component
            }
            idims.add_component(siz_rows, siz_cols, siz_precision, (siz_signed != 0), c);
        }
        parse_forced_precisions(args, idims);

        // Open images
        int num_source_components = 0; // Derived from source files
        // no java equivalent for kdu_rgb8_palette
        kdu_rgb8_palette palette; // To support palettized imagery.
        // kdc_file_binding in /apps/kdu_compress/compress_local.h
        kdc_file_binding * iscan;
        boolean extra_flip = false;
        for (iscan = inputs; iscan != null; iscan = iscan -> next) {
            int i;
            boolean flip;

            i = iscan -> first_comp_idx = num_source_components;
            if ((iscan -> next != null) && ((i + 1) >= idims.get_num_components())) {
                idims.append_component(); // This is relevant only for raw files where
                // Sprecision/Mprecision values supplied on the command line are
                // extrapolated and used to initialize the raw file reader; if we
                // do not explicitly invoke `append_component' here, the precision
                // information will not necessarily be extrapolated before the
                // file reader overwrites it in processing a `-fprec' forcing
                // precision.
            }

            if (!iscan -> cropping.is_empty()) {
                do {
                    idims.set_cropping(iscan -> cropping.pos.y, iscan -> cropping.pos.x,
                            iscan -> cropping.size.y, iscan -> cropping.size.x, i);
                    i++;
                } while (i < idims.get_num_components());
                iscan -> reader =
                        kdu_image_in(iscan -> fname, idims, num_source_components, flip,
                                ((no_palette ||
                                        !(is_jp2 || is_jpx || is_jph)) ? null : ( & palette)),
                        iscan -> offset, quiet);
                iscan -> num_components = num_source_components - iscan -> first_comp_idx;
            }
            if (iscan == inputs) {
                extra_flip = flip;
            }
            if (extra_flip != flip) {
                // no java equivalent for kdu_error
                kdu_error e;
                log.error("Cannot mix input file types which have different "
                    + "vertical ordering conventions (i.e., top-to-bottom and "
                    + "bottom-to-top).");
            }
            int crop_y, crop_x, crop_height, crop_width;
            for (i = iscan -> first_comp_idx; i < num_source_components; i++) {
                if (idims.get_cropping(crop_y, crop_x, crop_height, crop_width, i) &&
                        ((idims.get_width(i) != crop_width) ||
                                (idims.get_height(i) != crop_height))) {
                    // no java equivalent for kdu_error
                    kdu_error e;
                    log.error("Cropping requested for image component " + i
                            + " is not supported by the relevant image file reader at this "
                            + "time.  Try using a different image format (uncompressed "
                            + "TIFF files are likely to be best supported).");
                }
            }
        }
        if (extra_flip) {
            vflip = !vflip;
        }

        // Transfer dimension information back to `codestream_siz' object
        assert (num_source_components <= idims.get_num_components());
        if (!codestream_siz_ref -> get(Scomponents, 0, 0, c_components)) {
            codestream_siz_ref -> set(Scomponents, 0, 0, c_components = num_source_components);
        }
        boolean have_forced_floats = false;
        for (c = 0; c < num_source_components; c++) {
            int height = idims.get_height(c), width = idims.get_width(c);
            if (rgb_to_420 && ((c == 1) || (c == 2))) {
                if ((height & 1) || (width & 1)) {
                    // no java equivalent for kdu_error
                    kdu_error e;
                    log.error("The \"-rgb_to_420\" option can only "
                        + "be used when the first three image components have even dimensions.");
                }
                height >>= 1;
                width >>= 1;
            }
            codestream_siz_ref -> set(Sdims, c, 0, height);
            codestream_siz_ref -> set(Sdims, c, 1, width);
            codestream_siz_ref -> set(Nprecision, c, 0, idims.get_bit_depth(c));
            codestream_siz_ref -> set(Nsigned, c, 0, idims.get_signed(c));
            int exp_bits = 0;
            boolean align_lsbs;
            if ((idims.get_forced_precision(c, align_lsbs, exp_bits) > 0) && (exp_bits > 0)) {
                have_forced_floats = true;
            }
        }

        // Complete SIZ information and initialize JP2/JPX boxes
        ((Kdu_params *) codestream_siz_ref)->finalize(); // Access virtual function
        if (transpose) {
            siz_scratch.copy_from(codestream_siz_ref, -1, -1, -1, 0, 0, true, false, false);
            codestream_siz_ref = &siz_scratch;
        }

        char *extra_jp2_box_files = null;
        if (jp2_ultimate_tgt.exists()) {
            int num_available_comps = ((m_components > 0) ? m_components : c_components);
            extra_jp2_box_files =
                    set_jp2_attributes(jp2_family_dimensions, jp2_family_palette,
                            jp2_family_resolution, jp2_family_channels,
                            jp2_family_colour, codestream_siz_ref, palette,
                            num_available_comps, args, jpx_layer, idims, transpose,
                            rgb_to_420);
            if (num_jpx_layers != 1) {
                create_extra_jpx_layers(jpx_out, jpx_layer, num_jpx_layers,
                        num_available_comps);
            }
        }

        // Construct the `kdu_codestream' object and parse all remaining arguments.
        Kdu_codestream codestream;
        codestream.create(codestream_siz_ref, output);
        for (string = args.get_first(); string != null; ) {
            if (*string == '-'){
                args.advance(false);
                string = args.advance(false);
            } else{
                string = args.advance(codestream.access_siz()->parse_string(string));
            }
        }

        while (args.find("-com") != null) {
            if ((string = args.advance()) == null) {
                // no java equivalent for kdu_error
                kdu_error e;
                log.error("The \"-com\" argument must be followed by a string parameter.");
            }
            codestream.add_comment() << string;
            args.advance();
        }

        if (have_forced_floats) {
            set_forced_float_nlts(codestream.access_siz(), num_source_components, idims);
        }

        if (jp2_ultimate_tgt.exists()) {
            set_jp2_coding_defaults(jp2_family_palette, jp2_family_colour,
                    codestream.access_siz());
        }
        boolean using_pcrd_opt = false;
        int num_layer_specs = 0;
        kdu_long layer_bytes = assign_layer_bytes(args, codestream, num_layer_specs, using_pcrd_opt);
        kdu_uint16 layer_thresholds = assign_layer_thresholds(args, num_layer_specs, using_pcrd_opt);
        check_and_set_default_component_types(codestream.access_siz(),
                c_components, grey_weights,
                chroma_weights, no_weights,
                using_pcrd_opt, quiet);
        if (rgb_to_420) {
            set_420_registration(codestream.access_siz(), c_components);
        }


        // Like much of what is done in the "kdu_compress" demo-app, the following
        // logic is all implemented for you if you use the `kdu_stripe_compressor'
        // API.  These statements are setting up configuring Kakadu's complexity
        // control capabilities, taking into account any special options specified
        // on the command-line.
        if (!allow_slope_prediction) {
            codestream.set_slope_hint_policy(KDU_SLOPE_HINT_DEFAULT & KDU_SLOPE_HINT_ESTIMATE);
        }
        if ((num_layer_specs > 0) && allow_slope_prediction &&
                (layer_thresholds[num_layer_specs - 1] > 0) &&
                (layer_bytes[num_layer_specs - 1] <= 0)) {
            // We have a distortion-length slope constraint -- tell the system about
            // it unless we really don't want to allow the block encoder to take
            // advantage of this information by terminating early, if appropriate.
            codestream.set_min_slope_threshold(layer_thresholds[num_layer_specs - 1]);
        }
        boolean rate_limited = false;
        if ((num_layer_specs > 0) && (layer_bytes[num_layer_specs - 1] > 0) &&
                (layer_thresholds[num_layer_specs - 1] == 0)) {
            // We have a target size constraint -- tell the system about it up front.
            // Note that if both layer_bytes and layer_thresholds provide non-trivial
            // values, this application assumes `KDU_FLUSH_USES_THRESHOLDS_AND_SIZES'
            // and so the `layer_bytes' value is not actually a maximum compressed
            // size target.
            codestream.set_max_bytes(layer_bytes[num_layer_specs - 1],
                    false, allow_periodic_trimming);
            rate_limited = true;
        }
        if (num_layer_specs == 0) {
            // We don't have any constraints; compressed quality will depend upon any
            // quantization parameters.  Tell the system that there will be no
            // post-compression truncation of generated block bit-streams.
            codestream.set_max_slope_threshold(0);
        } else if ((num_layer_specs == 1) && (layer_bytes[0] == 0)) {
            // Similar to the above, but we have one layer specification, driven
            // by slope; this will be the largest slope threshold, which may allow
            // some initial coding passes to be skipped when working with block
            // coders that are not fully embedded -- notably the HT block encoder.
            codestream.set_max_slope_threshold((layer_thresholds == null) ? 0 :
                    layer_thresholds[0]);
        }

        codestream.access_siz()->finalize_all();

        if (!quiet) {
            check_and_warn_qstep(codestream); // Warn user who may be forgetting to
            // set Qstep if irreversibly compressing
            // high precision imagery.
        }

        boolean using_ht = false;
        Kdu_params cod = codestream.access_siz()->access_cluster(COD_params);
        int c_modes = 0;
        using_ht = ((cod != null) && cod -> get(Cmodes, 0, 0, c_modes) && (c_modes & (Cmodes_HT | Cmodes_HTMIX)));
        boolean cbr_mode = codestream.cbr_flushing();
        if (cbr_mode) {
            if (layer_bytes[num_layer_specs - 1] <= 0) {
                // no java equivalent for kdu_error
                kdu_error e;
                log.error("With the `Scbr' option, you must specify a "
                    + "specific overall target bit-rate via `-rate'!");
            }
        } else if ((num_layer_specs < 2) && (!using_ht) && !quiet) {
            System.out.println("Note:\n\tIf you want quality scalability, you should "
                    + "generate multiple layers with `-rate' or by using "
                    + "the \"Clayers\" option.\n");
        }

        Kdu_push_pull_params extra_params;
        char bstats_save_filename = null;
        Kdu_cplex_bkgnd_store bstats =
                prepare_cplex_bstats(args, rate_limited, num_layer_specs, cbr_mode, using_ht,
                        codestream.access_siz(), bstats_save_filename, !quiet);
        extra_params.set_cplex_bkgnd(bstats);
        if (bc_noopt) {
            extra_params.set_opt_flags(extra_params.get_opt_flags() |
                    KDU_BLOCK_CODING_NO_ACCELERATE);
        }

        if (jp2_family_dimensions.exists()) {
            jp2_family_dimensions.finalize_compatibility(codestream.access_siz());
        }

        Kdu_message_formatter formatted_recorder = null;
        ;
        kdu_stream_message recorder (record_stream);
        if (record_stream != null) {
            formatted_recorder = new Kdu_message_formatter( & recorder);
            codestream.set_textualization(formatted_recorder);
        }
        if (cpu_iterations >= 0) {
            codestream.collect_timing_stats(cpu_iterations);
        }
        codestream.change_appearance(transpose, vflip, hflip);
        Kdu_roi_image roi_source = create_roi_source(codestream, args);
        if (args.show_unrecognized(pretty_cout) != 0) {
            // no java equivalent for kdu_error
            //kdu_error e;
            log.error("There were unrecognized command line arguments!");
        }

        // Configure simultaneous processing fragments (split attributes), being
        // careful to disable fragmentation (splitting) if we need region-of-interest
        // coding.
        if (roi_source != null) {
            pref_split_width = max_split_depth = 0;
        }
        if (pref_split_width >= 0) {
            codestream.configure_simultaneous_processing_fragments(pref_split_width,
                    max_split_depth,
                    max_split_frags);
        }

        // Write JP2/JPH/JPX headers, if required
        if (jp2_ultimate_tgt.exists()) {
            Kdu_uint32 brand = jp2_out.get_brand();
            jp2_out.write_header();
        }
        if (jp2_ultimate_tgt.exists()) {
            write_extra_jp2_boxes( & jp2_ultimate_tgt,
                    extra_jp2_box_files, idims);
            if (extra_jp2_box_files != null) {
                delete[] extra_jp2_box_files;
            }
        }
        if (jp2_ultimate_tgt.exists()) {
            jp2_out.open_codestream(true);
        }

        // See if we need to disable automatic comments
        if (no_version) {
            codestream.set_disabled_auto_comments(KDU_AUTO_COMMENT_VERSION);
        }

        // Now we are ready for sample data processing.
        Kdu_dims tile_indices;
        codestream.get_valid_tiles(tile_indices);
        kdu_long sample_processing_bytes = 0;
        if (rgb_to_420 && (tile_indices.size.x > 1)) {
            // no java equivalent for kdu_error
            kdu_error e;
            log.error("The `-rgb_to_420' option cannot be used in conjunction with "
                + "horizontal tiling in this application.  This is not a fundamental "
                + "issue; just a consequence of the way in which tiling is handled by "
                + "this object's image reading logic, which requires each line to be "
                + "read completely before any part of the next line is read, while "
                + "chrominance sub-sampling is implemented here by reading line pairs "
                + "within each tile and converting them.");
        }
        Kdu_thread_entity_affinity affinity;
        if (num_threads == 0) {
            int dwt_stripe_height = 1;
            if (double_buffering_height > 0) {
                dwt_stripe_height = double_buffering_height;
                sample_processing_bytes =
                        compress_single_threaded(codestream, tile_indices, inputs, rgb_to_420,
                                roi_source, layer_bytes, num_layer_specs,
                                layer_thresholds, !no_info, rate_tolerance,
                                trim_to_rate, allow_shorts, flush_period,
                                dwt_stripe_height, progress_interval,
                                membroker, & extra_params);
            }
        } else {
            if (cpu_iterations > 0) {
                // no java equivalent for kdu_warning
                kdu_warning w;
                log.warn("CPU time statistics are likely to be "
                    + "incorrect unless you explicitly specify \"-num_threads 0\".");
            }
            boolean dwt_double_buffering = false;
            int dwt_stripe_height = 1;
            if ((double_buffering_height != 0) && (num_threads > 0)) {
                dwt_double_buffering = true;
                dwt_stripe_height = double_buffering_height;
            }
            if (num_threads < 0) {
                // Add all threads as an unbound bundle, without constraints
                num_threads = -num_threads;
                affinity.add_thread_bundle(num_threads, 0, 0);
            } else { // Add a single bundle, bound to the first NUMA node, if
                // discoverable, and constrained to at most one thread per logical
                // CPU in that node.
                num_threads = affinity.add_node_bundle(0, num_threads);
            }
            sample_processing_bytes =
                    compress_multi_threaded(codestream, tile_indices, inputs, rgb_to_420,
                            roi_source, layer_bytes, num_layer_specs,
                            layer_thresholds, !no_info, rate_tolerance,
                            trim_to_rate, allow_shorts, flush_period,
                            affinity, num_threads, num_xform_threads,
                            num_coding_threads, dwt_double_buffering,
                            dwt_stripe_height, progress_interval,
                            membroker, & extra_params);
        }

        // Finalize the compressed output.
        boolean last_fragment = true;

        // Cleanup
        if (cpu_iterations >= 0) {
            kdu_long num_samples;
            double seconds = codestream.get_timing_stats( & num_samples);
            System.out.println("\nEnd-to-end CPU time ");
            if (cpu_iterations > 0) {
                System.out.println("(estimated) ");
                System.out.println("= " + seconds + " seconds (" + 1.0E6 * seconds / num_samples + " us/sample)\n");
            }
            if (cpu_iterations > 0) {
                kdu_long num_samples;
                double seconds = codestream.get_timing_stats( & num_samples, true);
                if (seconds > 0.0) {
                    System.out.println("Block encoding CPU time (estimated) ");
                    System.out.println("= " + seconds + " seconds (" + 1.0E6 * seconds / num_samples + " us/sample)\n");
                }
            }
            if (mem) {
                System.out.println("\nSample processing/buffering memory = " + sample_processing_bytes + " bytes.\n");
                System.out.println("Compressed data memory = " + codestream.get_compressed_data_memory() + " bytes.\n");
                System.out.println("State memory associated with compressed data = "
                        + codestream.get_compressed_state_memory() + " bytes.\n");
                System.out.println("Coding parameter sub-system memory = " + codestream.get_params_memory() + " bytes.\n");
            }
            if (!quiet) {
                double bpp_dims = (double) get_bpp_dims(codestream);

                System.out.println("\nGenerated " + codestream.get_num_tparts()
                        + " tile-part(s) for a total of "
                        + tile_indices.area() << " tile(s).\n");
                System.out.println("Code-stream bytes (excluding any file format) = "
                        + codestream.get_total_bytes() + " = "
                        + 8.0 * codestream.get_total_bytes() / bpp_dims
                        + " bits/pel.\n");
                System.out.println("Compressed bytes (excludes codestream headers) = "
                        + codestream.get_packet_bytes() + " = "
                        + 8.0 * codestream.get_packet_bytes() / bpp_dims
                        + " bpp.\n");
                System.out.println("Body bytes (excludes packet and codestream headers) = "
                        + (codestream.get_packet_bytes() - codestream.get_packet_header_bytes()) + " = "
                        + 8.0 * (codestream.get_packet_bytes() - codestream.get_packet_header_bytes()) / bpp_dims
                        + " bpp.\n");

                int layer_idx;
                System.out.println("Layer bit-rates (possibly inexact if tiles are "
                    + "divided across tile-parts):\n\t\t");
                for (layer_idx = 0; layer_idx < num_layer_specs; layer_idx++) {
                    if (layer_idx > 0) {
                        System.out.println(", ");
                    }
                    System.out.println(8.0 * layer_bytes[layer_idx] / bpp_dims);
                }
                System.out.println("\n");
                System.out.println("Layer thresholds:\n\t\t");
                for (layer_idx = 0; layer_idx < num_layer_specs; layer_idx++) {
                    if (layer_idx > 0) {
                        System.out.println(", ");
                    }
                    System.out.println((int) (layer_thresholds[layer_idx]));
                }
                System.out.println("\n");

                if (num_threads == 0) {
                    System.out.println("Processed using the single-threaded environment (see `-num_threads')\n");
                } else {
                    System.out.println("Processed using the multi-threaded environment, with\n\t"
                            + num_threads + " parallel threads of execution\n");
                    if (num_xform_threads | num_coding_threads) {
                        System.out.println("\t" + num_xform_threads + " prefer to do transform processing;\n\t"
                                + num_coding_threads + " prefer to do block coding;\n\t"
                                + (num_threads - num_xform_threads - num_coding_threads)
                                + " have no preferences (see `-num_threads').\n");
                        int g = 0, thrds = 0, cxt = 0;
                        kdu_int64 mask = 0;
                        for (; (thrds = affinity.get_bundle_affinity(g, mask, cxt)) != 0; g++) {
                            char string[ 80];
                            snprintf(string, sizeof(string), "0x%08x%08x",
                                    (kdu_uint32) (mask >> 32), (kdu_uint32) mask);
                            System.out.println("\t\tBundle " + g + ": " + thrds + " threads");
                            if (mask != 0) {
                                System.out.println(" -- affinity cxt=" << cxt << ",mask=" << string);
                            }
                            System.out.println("\n");
                        }
                    }
                }
            }

            if (cbr_mode && !quiet) {
                float delay_lines = 0.0f, bucket_lines = 0.0f;
                kdu_uint32 flush_lines = 0, dwtA_lines = 0, dwtS_lines = 0;
                if (codestream.get_cbr_model(delay_lines, flush_lines, bucket_lines, dwtA_lines, dwtS_lines)) {
                    float fund = (bucket_lines + (float) (flush_lines + dwtA_lines + dwtS_lines));
                    kdu_uint32 encode_lines = flush_lines;
                    kdu_uint32 decode_lines = 4;
                    if (!using_ht) {
                        int nominal_blk_height = 0;
                        cod -> get(Cblk, 0, 0, nominal_blk_height);
                        decode_lines = 2 * (kdu_uint32) nominal_blk_height;
                    }
                    float start = delay_lines + flush_lines + dwtA_lines;
                    float overall = fund + (float) (encode_lines + decode_lines);
                    System.out.println("\nCBR Latency Analysis:\n"
                            + "* Fundamental end-to-end delay = " + fund + " lines\n"
                            + "      Flush-set L = " + flush_lines + " lines\n"
                            + "      Comms bucket B = " + bucket_lines + " lines\n"
                            + "      DWT analysis A = " + dwtA_lines + " lines\n"
                            + "      DWT synthesis S = " + dwtS_lines + " lines\n"
                            + "    - Includes all header and coded data communication.\n"
                            + "* Fundamental xmit start = " + start + " lines from top\n"
                            + "      L + A = " + (flush_lines + dwtA_lines) + " lines\n"
                            + "      Tuned delay D = " + delay_lines + " lines\n"
                            + "    - D improves quality uniformity; no effect on latency\n"
                            + "* Encoder computation delay = " + encode_lines + " lines\n"
                            + "    - this is an estimate, mainly for hardware\n"
                            + "    - can be smaller with HTJ2K and low-memory Cplex-EST\n"
                            + "* Decoder computation delay = " + decode_lines + " lines\n"
                            + "    - mostly for hardware with guaranteed throughput\n"
                            + "    - considerations are complex\n"
                            + "    - see `kdu_codestream::get_cbr_model' documentation\n"
                            + "* Overall end-to-end delay = " + overall + " lines\n"
                            + "    - excludes packetization & physical delays of course.\n";
                }
            }

            if (bstats != null) {
                kdu_uint32 n_levels = 0, n_incomp_non_ll_tally = 0, n_incomp_comp_tally = 0;
                kdu_uint32 n_incomp_rel_stats = 0, n_store_stats = 0;
                kdu_uint32 n_incomp_store_stats = 0;
                bstats -> get_info(n_levels, n_incomp_non_ll_tally,
                        n_incomp_comp_tally, n_incomp_rel_stats,
                        n_store_stats, n_incomp_store_stats);
                if ((n_incomp_non_ll_tally || n_incomp_comp_tally)) {
                    // no java equivalent for kdu_warning
                    // kdu_warning w;
                    log.warn("\nThe \"-bstats\" model used for background statistics is not "
                    + "completely consistent with each compressed tile-component.  "
                    + "There were " + n_incomp_comp_tally + " whole inconsistent "
                    + "tile-components and " + n_incomp_non_ll_tally + " non-LL "
                    + "subbands from tile-components or tile-component-resolutions that "
                    + "were not completely consistent.\n");
                }
                if (n_incomp_rel_stats) {
                    // no java equivalent for kdu_warning
                    // kdu_warning w;
                    log.warn("\nThe \"-bstats\" model used for background statistics involves "
                        + "quantization parameters that are not completely consistent with "
                        + "those used here for compression.  On " + n_incomp_rel_stats + " "
                        + "occasions, subband complexity parameters from the background "
                        + "model could not be used, due to quantization inconsistencies.\n");
                }
                if (n_incomp_store_stats) {
                    // no java equivalent for kdu_warning
                    // kdu_warning w;
                    log.warn("\nThe \"-bstats\" model used for background statistics was "
                        "updated using statistics from subbands whose quantization "
                        "parameters are not completely compatible with those of the "
                        "original background statistics loaded from the \"-bstats\" "
                        "source file.  This happened on " + n_incomp_store_stats + " of "
                        "the " + n_store_stats + " occasions on which statistics "
                        "were updated.  This inconsistency means that original background "
                        "statistics had to be discarded from the \"-bstats\" save file.\n");
                }
                if (bstats_save_filename != null) {
                    if (n_store_stats == 0) {
                        // no java equivalent for kdu_warning
                        // kdu_warning w;
                        log.warn("No new statistics were accumulated for "
                            + "writing to the \"-bstats\" save file \""
                            + bstats_save_filename + "\".  Looks like you might have "
                            + "set the `Cplex' attribute to something other than the EST "
                            + "complexity constraint method, or else you did not specify any "
                            + "bit-rate constraint.");
                    } else if (!bstats -> save(bstats_save_filename)) {
                        // no java equivalent for kdu_warning
                        // kdu_warning w;
                        log.warn("Unable to write to the \"-bstats\" save "
                            "file \"" + bstats_save_filename + "\".");
                    }
                    delete[] bstats_save_filename;
                    bstats_save_filename = null;
                }
                delete bstats;
                bstats = null;
            }
            delete[] layer_bytes;
            delete[] layer_thresholds;
            codestream.Destroy();
            output -> close();
            if (jp2_ultimate_tgt.Exists()) {
                jp2_ultimate_tgt.Close();
            }
            if (roi_source != null) {
                delete roi_source;
            }
            delete inputs;
            //return 0;
        }
    }
}

# colorscanner
Scan images for color space information

Given a image or list of images, the colorscanner can retrieve image metadata using EXIF fields and the
ImageMagick identify command. It can also convert images to JP2 using the Kakadu kduCompress command.

## Image Conversion
The colorscanner JP2 conversion supports the following image formats: TIFF, JPEG, PNG, GIF, PICT, BMP.

Kakadu kduCompress struggles to convert non-TIFF images. To work around this, we convert images in other formats to 
temporary TIFF files before JP2 conversion. GIF images have an additional `-no_palette` argument in the kduCompress
command to avoid pixelization.

The colorscanner JP2 conversion should work on images with the following colorspaces: RGB, Grayscale, and CMYK.

Kakadu kduCompress struggles to convert images with certain color space data.
We fixed our issues with grayscale and CMYK images, but we may not have accounted for all unusual color spaces.
Grayscale images have an additional `-jp2_space sLUM` argument in the kduCompress command.
Images with a CMYK color space are first converted to a temporary TIFF file before JP2 conversion.

## Commands
- `colorscanner list -f <filename>`: retrieve image color fields and attributes for an image file
- `colorscanner list_all -f <filename>`: retrieve image color fields and attributes for a list of files
- `colorscanner kdu_compress -f <filename> -o <outputPath>`: run kduCompress on an image file, must set output path
- `colorscanner kdu_compress_all -f <filename> -o <outputPath>`: run kduCompress on a list of image files, must set output path
- `colorscanner -sf <sourceFormat>`: override source file type detection, for files without file extensions

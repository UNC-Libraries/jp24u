# JP2ImageConverter

Image to JP2 for You!

Given a image or list of images, the JP2ImageConverter can convert images to JP2 using the Kakadu kduCompress command. 
It can also retrieve image metadata using EXIF fields and the ImageMagick identify command.

## Image Conversion
The JP2ImageConverter supports the following image formats: TIFF, JPEG, PNG, GIF, PICT, BMP, PSD, JP2.

Kakadu kduCompress struggles to convert non-TIFF images. To work around this, we convert images in other formats to 
temporary TIFF files before JP2 conversion. GIF images have an additional `-no_palette` argument in the kduCompress
command to avoid pixelization.

The JP2ImageConverter JP2 conversion should work on images with the following colorspaces: RGB, Grayscale, and CMYK.

Kakadu kduCompress struggles to convert images with certain color space data.
We fixed our issues with grayscale and CMYK images, but we may not have accounted for all unusual color spaces.
Grayscale images have an additional `-jp2_space sLUM` argument in the kduCompress command.
Images with a CMYK color space are first converted to a temporary TIFF file before JP2 conversion.

## Commands
- `jp24u list -f <filename>`: retrieve image color fields and attributes for an image file
- `jp24u list_all -f <filename>`: retrieve image color fields and attributes for a list of files
- `jp24u kdu_compress -f <filename> -o <outputPath>`: run kduCompress on an image file, must set output path
- `jp24u kdu_compress_all -f <filename> -o <outputPath>`: run kduCompress on a list of image files, must set output path
- `jp24u -sf <sourceFormat>`: override source file type detection, for files without file extensions

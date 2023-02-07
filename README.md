# colorscanner
Scan images for color space information

Given a image or list of images, the colorscanner can retrieve image metadata using EXIF fields and the
ImageMagick identify command. It can also convert TIFF to JP2 using the Kakadu kduCompress command.

## Image Conversion
The colorscanner TIFF-to-JP2 conversion should work on images with the following colorspaces: RGB, Grayscale, and CMYK.

Kakadu kduCompress struggles to convert images with certain color space data.
We fixed our issues with grayscale and CMYK images, but we may not have accounted for all unusual color spaces.
Grayscale images have an additional `-jp2_space sLUM` argument in the kduCompress command.
Images with a CMYK color space are first converted to a temporary JPG file before JP2 conversion.

## Commands
- `colorscanner list [filename]`: retrieve image color fields and attributes for an image file
- `colorscanner list_all [filename]`: retrieve image color fields and attributes for a list of files
- `colorscanner kdu_compress [filename]`: run kduCompress on an image file
- `colorscanner kdu_compress_all [filename]`: run kduCompress on a list of image files

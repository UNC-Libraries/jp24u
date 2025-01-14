# JP2 Image Converter

Image to JP2 for You!

Given a image, the JP2ImageConverter can convert image to JP2 using the Kakadu kduCompress command. 
It can also retrieve image metadata using EXIF fields and the ImageMagick identify command.

## Dependencies
- [ImageMagick](https://imagemagick.org/script/)
- [GraphicsMagick 1.3.x](http://www.graphicsmagick.org/)
- [Exiftool 12.40](https://exiftool.org/)
- [Kakadu 8.3.0](https://kakadusoftware.com/)

## Image Conversion
### Image Formats
The JP2ImageConverter supports the following image formats: TIFF, JPEG, PNG, GIF, PICT, BMP, PSD, JP2, 
NEF, NRW, CRW, CR2, DNG, RAF, RW2.

Kakadu kduCompress struggles to convert non-TIFF images. To work around this, we preprocess images in other formats.
This involves converting non-TIFF images to temporary TIFF files. 
GIF images have an additional `-no_palette` argument in the kduCompress command to avoid pixelization.

### Color Spaces
The JP2ImageConverter should work on images with the following color spaces: RGB, Grayscale, and CMYK.

Kakadu kduCompress struggles to convert images with certain color space data.
We fixed our issues with grayscale and CMYK images, but we may not have accounted for all unusual color spaces.
Grayscale images have an additional `-jp2_space sLUM` argument in the kduCompress command.
Images with a CMYK color space are first converted to a temporary TIFF file before JP2 conversion.

## Commands
- `jp24u list -f <filename>`: retrieve image color fields and attributes for an image file
- `jp24u list_all -f <filename>`: retrieve image color fields and attributes for a list of files
- `jp24u kdu_compress -f <filename> -o <outputPath>`: run kduCompress on an image file, set output path
- `jp24u -sf <sourceFormat>`: override source file type detection

/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Utility class for image processing operations
 */
public class ImageProcessingUtils {

    private static final Log log = LogFactory.getLog(ImageProcessingUtils.class);
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    /**
     * Removes EXIF metadata from image bytes.
     *
     * @param imageBytes the raw bytes of the image
     * @param mediaType  the detected media type of the image (e.g., "image/jpeg", "image/png")
     * @return byte array of the normalized image with EXIF metadata removed
     * @throws APIManagementException if an error occurs during image processing
     */
    public static byte[] removeExifMetadata(byte[] imageBytes, String mediaType) throws APIManagementException {

        if (imageBytes == null || imageBytes.length == 0) {
            throw new APIManagementException("Thumbnail image is empty");
        }
        if (imageBytes.length > MAX_IMAGE_BYTES) {
            throw new APIManagementException("Thumbnail image exceeds maximum allowed size: 5MB");
        }
        if (mediaType == null) {
            throw new APIManagementException("Thumbnail media type is not provided");
        }
        if (RestApiConstants.GIF_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping EXIF metadata removal for GIF image");
            }
            return imageBytes;
        } else if (!RestApiConstants.JPEG_MEDIA_TYPE.equalsIgnoreCase(mediaType)
                && !RestApiConstants.PNG_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
            if (log.isDebugEnabled()) {
                log.debug("Unsupported image type for meta data processing. Detected media type: " + mediaType);
            }
            return imageBytes;
        }

        try {
            // Extract EXIF orientation before re-encoding (both JPEG and PNG can contain EXIF orientation)
            int orientation = extractExifOrientation(imageBytes, mediaType);
            if (log.isDebugEnabled()) {
                log.debug("Extracted EXIF orientation from thumbnail: " + orientation);
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new APIManagementException("Failed to read image for EXIF metadata processing");
            }
            // Apply orientation transformation so image looks correct without the EXIF tag
            if (orientation > 1 && orientation <= 8) {
                image = applyExifOrientation(image, orientation);
                if (log.isDebugEnabled()) {
                    log.debug("Applied EXIF orientation " + orientation + " to thumbnail pixel data");
                }
            }
            if (RestApiConstants.JPEG_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
                return writeCleanJpeg(image);
            } else {
                String formatName = getImageWriteFormatName(mediaType);
                ByteArrayOutputStream cleanImageStream = new ByteArrayOutputStream();
                if (!ImageIO.write(image, formatName, cleanImageStream)) {
                    log.warn("No appropriate image writer found for format: " + formatName
                            + ". Returning original image bytes.");
                    return imageBytes;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed EXIF metadata from thumbnail image of type: " + mediaType);
                }
                return cleanImageStream.toByteArray();
            }
        } catch (IOException e) {
            throw new APIManagementException("Error processing thumbnail image", e);
        }
    }

    /**
     * Extracts the EXIF orientation value from image bytes based on the media type.
     *
     * @param imageBytes the raw image file bytes
     * @param mediaType  the detected media type of the image
     * @return the EXIF orientation value (1-8), or 1 (normal) if not found or on parse error
     */
    private static int extractExifOrientation(byte[] imageBytes, String mediaType) {

        if (RestApiConstants.JPEG_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
            return extractJpegExifOrientation(imageBytes);
        } else if (RestApiConstants.PNG_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
            return extractPngExifOrientation(imageBytes);
        }
        return 1;
    }

    /**
     * Extracts the EXIF orientation value from raw JPEG bytes by parsing the APP1 marker segment.
     *
     * @param jpeg the raw JPEG file bytes
     * @return the orientation value (1-8), or 1 (normal) if not found or on parse error
     */
    private static int extractJpegExifOrientation(byte[] jpeg) {

        try {
            // JPEG starts with SOI marker (0xFFD8)
            if (jpeg.length < 4 || (jpeg[0] & 0xFF) != 0xFF || (jpeg[1] & 0xFF) != 0xD8) {
                return 1;
            }
            int index = 2;
            while (index + 4 < jpeg.length) {
                if ((jpeg[index] & 0xFF) != 0xFF) {
                    break;
                }
                int marker = jpeg[index + 1] & 0xFF;
                int segmentLength = ((jpeg[index + 2] & 0xFF) << 8) | (jpeg[index + 3] & 0xFF);

                // APP1 marker (0xE1) contains EXIF data
                if (marker == 0xE1) {
                    int exifStart = index + 4;
                    if (exifStart + 6 > jpeg.length) {
                        return 1;
                    }
                    // Verify "Exif\0\0" header
                    if (jpeg[exifStart] != 'E' || jpeg[exifStart + 1] != 'x' || jpeg[exifStart + 2] != 'i'
                            || jpeg[exifStart + 3] != 'f' || jpeg[exifStart + 4] != 0 || jpeg[exifStart + 5] != 0) {
                        // Not an EXIF APP1 segment, skip to next marker
                        index += segmentLength + 2;
                        continue;
                    }
                    // TIFF header starts after "Exif\0\0"
                    int tiffStart = exifStart + 6;
                    return extractOrientationFromTiffData(jpeg, tiffStart);
                }
                // Stop at SOS marker (start of scan data) or EOI
                if (marker == 0xDA || marker == 0xD9) {
                    break;
                }
                index += segmentLength + 2;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not extract EXIF orientation from JPEG. Defaulting to normal orientation.", e);
            }
        }
        return 1;
    }

    /**
     * Reads a 16-bit unsigned integer from a byte array with the specified byte order.
     *
     * @param data         the byte data
     * @param offset       the offset to read from
     * @param littleEndian true for little-endian (Intel, II), false for big-endian (Motorola, MM)
     * @return the 16-bit unsigned integer value, or 0 if offset is out of bounds
     */
    private static int readTiffInt16(byte[] data, int offset, boolean littleEndian) {

        if (offset + 1 >= data.length) {
            return 0;
        }
        if (littleEndian) {
            return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
        } else {
            return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        }
    }

    /**
     * Reads a 32-bit unsigned integer from a byte array with the specified byte order.
     *
     * @param data         the byte data
     * @param offset       the offset to read from
     * @param littleEndian true for little-endian (Intel, II), false for big-endian (Motorola, MM)
     * @return the 32-bit integer value, or 0 if offset is out of bounds
     */
    private static int readTiffInt32(byte[] data, int offset, boolean littleEndian) {

        if (offset + 3 >= data.length) {
            return 0;
        }
        if (littleEndian) {
            return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) | ((data[offset + 2] & 0xFF) << 16) | (
                    (data[offset + 3] & 0xFF) << 24);
        } else {
            return ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16) | ((data[offset + 2] & 0xFF) << 8)
                    | (data[offset + 3] & 0xFF);
        }
    }

    /**
     * Extracts the EXIF orientation tag from raw TIFF IFD data starting at the given offset.
     *
     * @param data      the byte array containing the TIFF data
     * @param tiffStart the offset where the TIFF header begins
     * @return the orientation value (1-8), or 1 if not found
     */
    private static int extractOrientationFromTiffData(byte[] data, int tiffStart) {

        if (tiffStart + 8 > data.length) {
            return 1;
        }
        boolean littleEndian = (data[tiffStart] == 'I' && data[tiffStart + 1] == 'I');
        if (!littleEndian && !(data[tiffStart] == 'M' && data[tiffStart + 1] == 'M')) {
            return 1;
        }
        int ifdOffset = readTiffInt32(data, tiffStart + 4, littleEndian) + tiffStart;
        if (ifdOffset + 2 > data.length) {
            return 1;
        }
        int entryCount = readTiffInt16(data, ifdOffset, littleEndian);
        int ptr = ifdOffset + 2;
        for (int i = 0; i < entryCount && ptr + 12 <= data.length; i++) {
            int tag = readTiffInt16(data, ptr, littleEndian);
            if (tag == 0x0112) {
                int type = readTiffInt16(data, ptr + 2, littleEndian);
                if (type == 3) { // SHORT type
                    int orientation = readTiffInt16(data, ptr + 8, littleEndian);
                    if (orientation >= 1 && orientation <= 8) {
                        return orientation;
                    }
                }
                return 1;
            }
            ptr += 12;
        }
        return 1;
    }

    /**
     * Extracts the EXIF orientation value from raw PNG image bytes by locating the eXIf chunk.
     * PNG stores EXIF data in an 'eXIf' chunk whose payload is raw TIFF data (starting with
     * the byte order marker "MM" or "II").
     *
     * @param pngBytes the raw PNG file bytes
     * @return the orientation value (1-8), or 1 (normal) if not found or on parse error
     */
    private static int extractPngExifOrientation(byte[] pngBytes) {

        try {
            // PNG signature is 8 bytes
            if (pngBytes.length < 8) {
                return 1;
            }
            int offset = 8; // Skip PNG signature
            while (offset + 12 <= pngBytes.length) {
                // Each chunk: 4 bytes data length (big-endian) + 4 bytes chunk type + data + 4 bytes CRC
                int chunkLength = ((pngBytes[offset] & 0xFF) << 24) | ((pngBytes[offset + 1] & 0xFF) << 16) | (
                        (pngBytes[offset + 2] & 0xFF) << 8) | (pngBytes[offset + 3] & 0xFF);
                String chunkType = new String(pngBytes, offset + 4, 4, StandardCharsets.US_ASCII);
                if ("eXIf".equals(chunkType)) {
                    int dataStart = offset + 8;
                    if (dataStart + chunkLength > pngBytes.length) {
                        return 1;
                    }
                    return extractOrientationFromTiffData(pngBytes, dataStart);
                }
                // Move to next chunk: 4 (length) + 4 (type) + data + 4 (CRC)
                offset += 12 + chunkLength;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not extract EXIF orientation from PNG. Defaulting to normal orientation.", e);
            }
        }
        return 1;
    }

    /**
     * Applies EXIF orientation transformation to a BufferedImage so that the pixel data
     * reflects the intended display orientation. This ensures the image displays correctly
     * after re-encoding removes the EXIF orientation tag.
     * <p>
     * EXIF orientation values:
     * <ul>
     *   <li>1 = Normal (no transformation needed)</li>
     *   <li>2 = Flipped horizontally</li>
     *   <li>3 = Rotated 180 degrees</li>
     *   <li>4 = Flipped vertically</li>
     *   <li>5 = Rotated 90 degrees CW then flipped horizontally</li>
     *   <li>6 = Rotated 90 degrees CW</li>
     *   <li>7 = Rotated 90 degrees CCW then flipped horizontally</li>
     *   <li>8 = Rotated 90 degrees CCW (270 degrees CW)</li>
     * </ul>
     *
     * @param image       the source image to transform
     * @param orientation the EXIF orientation value (1-8)
     * @return a new BufferedImage with the orientation applied, or the original if no transformation needed
     */
    private static BufferedImage applyExifOrientation(BufferedImage image, int orientation) {

        int width = image.getWidth();
        int height = image.getHeight();
        // Preserve original image type; fall back to TYPE_INT_ARGB if type is undefined (0)
        int imageType = (image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType();
        BufferedImage result;
        Graphics2D g;
        switch (orientation) {
        case 2: // Flip horizontal
            result = new BufferedImage(width, height, imageType);
            g = result.createGraphics();
            g.drawImage(image, width, 0, -width, height, null);
            g.dispose();
            return result;
        case 3: // Rotate 180
            result = new BufferedImage(width, height, imageType);
            g = result.createGraphics();
            g.rotate(Math.PI, width / 2.0, height / 2.0);
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return result;
        case 4: // Flip vertical
            result = new BufferedImage(width, height, imageType);
            g = result.createGraphics();
            g.drawImage(image, 0, height, width, -height, null);
            g.dispose();
            return result;
        case 5: // Rotate 90 CW + flip horizontal
            result = new BufferedImage(height, width, imageType);
            g = result.createGraphics();
            g.translate(height, 0);
            g.rotate(Math.PI / 2);
            g.drawImage(image, width, 0, -width, height, null);
            g.dispose();
            return result;
        case 6: // Rotate 90 CW
            result = new BufferedImage(height, width, imageType);
            g = result.createGraphics();
            g.translate(height, 0);
            g.rotate(Math.PI / 2);
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return result;
        case 7: // Rotate 90 CCW + flip horizontal
            result = new BufferedImage(height, width, imageType);
            g = result.createGraphics();
            g.translate(0, width);
            g.rotate(-Math.PI / 2);
            g.drawImage(image, width, 0, -width, height, null);
            g.dispose();
            return result;
        case 8: // Rotate 90 CCW (270 CW)
            result = new BufferedImage(height, width, imageType);
            g = result.createGraphics();
            g.translate(0, width);
            g.rotate(-Math.PI / 2);
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return result;
        default:
            return image;
        }
    }

    /**
     * Writes a BufferedImage as a JPEG with 90% quality compression.
     *
     * @param image the BufferedImage to write
     * @return byte array containing the re-encoded JPEG
     * @throws IOException if an error occurs during writing
     */
    private static byte[] writeCleanJpeg(BufferedImage image) throws IOException {

        BufferedImage jpegImage = toRgbImage(image);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (writers.hasNext()) {
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.9f);
            }
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(jpegImage, null, null), param);
            } finally {
                writer.dispose();
            }
        } else {
            // Fallback to default ImageIO write
            ImageIO.write(image, "jpeg", outputStream);
        }
        return outputStream.toByteArray();
    }

    /**
     * Converts a BufferedImage to an RGB image.
     *
     * @param image the input BufferedImage
     * @return a BufferedImage of type TYPE_INT_RGB
     */
    private static BufferedImage toRgbImage(BufferedImage image) {

        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        try {
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }
        return rgbImage;
    }

    /**
     * Returns the ImageIO format name corresponding to the given media type.
     *
     * @param mediaType the MIME type of the image (e.g., "image/jpeg", "image/png")
     * @return the ImageIO writer format name
     */
    private static String getImageWriteFormatName(String mediaType) {

        if (RestApiConstants.PNG_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
            return "png";
        } else if (RestApiConstants.JPEG_MEDIA_TYPE.equalsIgnoreCase(mediaType)) {
            return "jpeg";
        } else {
            return "png";
        }
    }
}

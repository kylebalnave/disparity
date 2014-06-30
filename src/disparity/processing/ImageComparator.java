package disparity.processing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Compares 2 images
 *
 * @author kyleb2
 */
public class ImageComparator {

    protected BufferedImage referenceImage;
    protected BufferedImage imageToCompare;

    public ImageComparator(String referenceImagePath, String imagePathToCompare) throws IOException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.format("Compare images '%s' & '%s'", referenceImagePath, imagePathToCompare));
        referenceImage = ImageIO.read(new File(referenceImagePath));
        imageToCompare = ImageIO.read(new File(imagePathToCompare));
    }

    public ImageComparator(File referenceFile, File fileToCompare) throws IOException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.format("Compare images '%s' & '%s'", referenceFile.getAbsolutePath(), fileToCompare.getAbsolutePath()));
        referenceImage = ImageIO.read(referenceFile);
        imageToCompare = ImageIO.read(fileToCompare);
    }

    public ImageComparator(BufferedImage referenceImage, BufferedImage imageToCompare) {
        this.referenceImage = referenceImage;
        this.imageToCompare = imageToCompare;
    }
    

    /**
     * Creates a new image which is a differenced filter representation of the
     * first images
     *
     * @param diffFileOut
     * @param fuzzyness
     * @return 
     * @throws java.io.IOException
     */
    public long createRGBDiff(File diffFileOut, int fuzzyness) throws IOException {
        int pixelDiffCount = 0;
        int totalPixelCount = 0;
        int minWidth = Math.min(referenceImage.getWidth(), imageToCompare.getWidth());
        int minHeight = Math.min(referenceImage.getHeight(), imageToCompare.getHeight());
        BufferedImage iOutBuff = new BufferedImage(minWidth, minHeight, BufferedImage.TYPE_BYTE_GRAY);
        Color rgbColour;
        int pxIncrement = 1;
        int rgbDiff;
        int[] rgbArr = new int[3];
        WritableRaster raster = iOutBuff.getRaster();
        for (int y = 0; y < minHeight; y += pxIncrement) {
            for (int x = 0; x < minWidth; x += pxIncrement) {
                rgbDiff = compareRGB(referenceImage, imageToCompare, x, y);
                rgbColour = new Color(rgbDiff);
                rgbArr[0] = Math.max(0, rgbColour.getRed() - fuzzyness);
                rgbArr[1] = Math.max(0, rgbColour.getBlue() - fuzzyness);
                rgbArr[2] = Math.max(0, rgbColour.getBlue() - fuzzyness);
                totalPixelCount++;
                if (rgbArr[0] > 0 || rgbArr[1] > 0 || rgbArr[2] > 0) {
                    pixelDiffCount++;
                }
                raster.setPixel(x, y, rgbArr);
            }
        }
        long percentPixelDiff = totalPixelCount > 0 ? (100 * pixelDiffCount) / totalPixelCount : 100;
        ImageIO.write(iOutBuff, getPathExtension(diffFileOut.getAbsolutePath()), diffFileOut);
        return percentPixelDiff;
    }
    

    /**
     * Creates an RGB diff image with a 0 fuzzyness threshold
     *
     * @param outPath
     * @param fuzzyness
     * @throws IOException
     */
    public void createRGBDiff(String outPath, int fuzzyness) throws IOException {
        createRGBDiff(new File(outPath), fuzzyness);
    }

    /**
     * Compares the RGB values at a point
     *
     * @param referenceImage
     * @param imageToCompare
     * @param x
     * @param y
     * @return
     */
    protected int compareRGB(BufferedImage referenceImage, BufferedImage imageToCompare, int x, int y) {
        return Math.abs(referenceImage.getRGB(x, y) - imageToCompare.getRGB(x, y));
    }

    /**
     * Gets the RGB value at a point
     *
     * @param img
     * @param x
     * @param y
     * @return
     */
    protected int getRGB(BufferedImage img, int x, int y) {
        return img.getRGB(x, y);
    }
    
    /**
     * Gets the path extension
     * @param fullPath
     * @return 
     */
    protected String getPathExtension(String fullPath) {
        String[] split = fullPath.split("\\.");
        if(split.length < 2) {
            return "";
        } else {
            return split[split.length - 1];
        }
    }

}

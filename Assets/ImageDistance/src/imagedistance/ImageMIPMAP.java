/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagedistance;

import static imagedistance.ImageMIPMAP.dir;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Kajos
 */
public class ImageMIPMAP {
    static String dir = "C:\\Users\\Kajos\\Documents\\TerrainCaster\\Assets\\TerrainCaster\\";
    static String input = "gimpheightmap.png";
    static String outputSmall = "gimpheightmap_small.png";
    static String outputSmaller = "gimpheightmap_smaller.png";
    /**
     * @param args the command line arguments
     */
    
    private static double GetDistance(int x, int y) {
        return Math.floor(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));  
    }
    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    public static void main(String[] args) {
        try {
            final BufferedImage image = ImageIO.read(new File(dir + input));
            final BufferedImage imageOutSmall = new BufferedImage(image.getWidth() / 2, image.getHeight() / 2, BufferedImage.TYPE_INT_ARGB);
            final BufferedImage imageOutSmaller = new BufferedImage(image.getWidth() / 4, image.getHeight() / 4, BufferedImage.TYPE_INT_ARGB);
            
            final int width = image.getWidth();
            final int height = image.getHeight();
            int i = 0;
            for(int y = 0; y < image.getHeight(); y++) {
                System.out.println((float)y / (float)image.getHeight() * 100.0);
                for(int x = 0; x < width; x++, i++) {
                    int origRed = image.getRGB(x, y) & 0xFF0000;
                    
                    if (x % 2 == 0 && y % 2 == 0) {
                        int biggest = origRed;
                        for (int dx = -1; dx < 0; dx++) 
                            for (int dy = -1; dy < 0; dy++) {
                                int nx = dx + x;
                                int ny = dy + y;

                                nx %= width;
                                ny %= height;
                                if(nx < 0) nx += width;
                                if(ny < 0) ny += height;

                                int clr = image.getRGB(nx, ny) & 0xFF0000;
                                if (biggest < clr)
                                    biggest = clr;
                            }
                        Color scolor = new Color(biggest >> 16, 0, 0);
                        imageOutSmall.setRGB(x / 2, y / 2, scolor.getRGB());
                        
                        if (x % 4 == 0 && y % 4 == 0) {
                            for (int dx = -1; dx < 0; dx++) 
                                for (int dy = -1; dy < 0; dy++) {
                                    int nx = dx + x;
                                    int ny = dy + y;

                                    nx %= width;
                                    ny %= height;
                                    if(nx < 0) nx += width;
                                    if(ny < 0) ny += height;

                                    int clr = image.getRGB(nx, ny) & 0xFF0000;
                                    if (biggest < clr)
                                        biggest = clr;
                                }
                            Color sscolor = new Color(biggest >> 16, 0, 0);
                            imageOutSmaller.setRGB(x / 4, y / 4, sscolor.getRGB());
                        }
                    }
                }
            }
            
            File outputfile = new File(dir + outputSmall);
            ImageIO.write(imageOutSmall, "png", outputfile);
            outputfile = new File(dir + outputSmaller);
            ImageIO.write(imageOutSmaller, "png", outputfile);
        } catch (Exception ex) {
            Logger.getLogger(ImageMIPMAP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
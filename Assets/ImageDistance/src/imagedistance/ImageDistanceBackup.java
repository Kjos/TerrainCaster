/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagedistance;

import static imagedistance.ImageDistance.dir;
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
public class ImageDistanceBackup {
    static String dir = "C:\\Users\\Kajos\\Documents\\TerrainCaster\\Assets\\TerrainCaster\\";
    static String input = "heightmap.bmp";
    static String output = "heightmap_c.png";
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
    
    private static double angleBetween(int x1, int y1, int x2, int y2) {
        if (x1 - x2 == 0)
            return Math.atan(y1 - y2);
        else
            return Math.atan((y1 - y2) / (x1 - x2));
    }
    
    public static void main(String[] args) {
        try {
            final BufferedImage image = ImageIO.read(new File(dir + input));
            final BufferedImage imageOut = deepCopy(image);
            final int width = image.getWidth();
            final int height = image.getHeight();
            int i = 0;
            for(int y = 0; y < image.getHeight(); y++) {
                System.out.println((float)y / (float)image.getHeight() * 100.0);
                Thread threads[] = new Thread[width];
                for(int x = 0; x < width; x++, i++) {
                    final int gx = x;
                    final int gy = y;
                    threads[x] = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            int origRed = (image.getRGB(gx, gy) & 0xFF0000) >> 16;
                            float coneAngle = (float)(255.0f - origRed) / 255.0f;
                            int coneStart = origRed + 1;
                            
                            try {
                                // Bottom radius
                                
                                int rowSize = 1;
                                int rowSizeMax = 256;
                                int rowSizeConeMax = 256;
                                while(rowSize < rowSizeMax) {
                                    for (int dx = -rowSize; dx < rowSize + 1; dx++) {
                                        int dy = -rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        if (comp > origRed) {
                                            if (rowSizeMax > dist) {
                                                rowSizeMax = dist;
                                            }
                                        }
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }

                                    for (int dx = -rowSize; dx < rowSize + 1; dx++) {
                                        int dy = rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        if (comp > origRed) {
                                            if (rowSizeMax > dist) {
                                                rowSizeMax = dist;
                                            }
                                        }
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }

                                    for (int dy = -rowSize; dy < rowSize + 1; dy++) {
                                        int dx = -rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        if (comp > origRed) {
                                            if (rowSizeMax > dist) {
                                                rowSizeMax = dist;
                                            }
                                        }
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }

                                    for (int dy = -rowSize; dy < rowSize + 1; dy++) {
                                        int dx = rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        if (comp > origRed) {
                                            if (rowSizeMax > dist) {
                                                rowSizeMax = dist;
                                            }
                                        }
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }
                                    
                                    rowSize++;
                                }   
                                
                                int cylRowSizeResult = rowSizeMax - 1;
                                
                                rowSizeMax = 256;
                                while(rowSize < rowSizeConeMax) {
                                    for (int dx = -rowSize; dx < rowSize + 1; dx++) {
                                        int dy = -rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }

                                    for (int dx = -rowSize; dx < rowSize + 1; dx++) {
                                        int dy = rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }

                                    for (int dy = -rowSize; dy < rowSize + 1; dy++) {
                                        int dx = -rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }

                                    for (int dy = -rowSize; dy < rowSize + 1; dy++) {
                                        int dx = rowSize;

                                        int nx = dx + gx;
                                        int ny = dy + gy;
                                        
                                        nx %= width;
                                        ny %= height;
                                        if(nx < 0) nx += width;
                                        if(ny < 0) ny += height;

                                        int comp = (image.getRGB(nx, ny) & 0xFF0000) >> 16;
                                        int dist = (int)GetDistance(dx, dy);
                                        float angle = ((float)comp - (float)coneStart) / (float)dist;
                                        if (angle > coneAngle) {
                                            coneAngle = angle;
                                            rowSizeConeMax = (int)Math.floor((255.0f - (float)origRed) / coneAngle);
                                        }
                                    }
                                    
                                    rowSize++;
                                }   
                                
                                int coneRowSizeResult = rowSizeConeMax - 1;
                                
                                Color color = new Color(origRed, cylRowSizeResult, coneRowSizeResult);
                                imageOut.setRGB(gx, gy, color.getRGB());
                            } catch (Exception ex) {
                                System.out.println(coneAngle);
                                ex.printStackTrace();
                                System.exit(1);
                            }
                        }


                    });
                    threads[x].start();
                }
                for(int x = 0; x < width; x++, i++) {
                    threads[x].join();
                }
            }
            
            File outputfile = new File(dir + output);
            ImageIO.write(imageOut, "png", outputfile);
        } catch (Exception ex) {
            Logger.getLogger(ImageDistance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

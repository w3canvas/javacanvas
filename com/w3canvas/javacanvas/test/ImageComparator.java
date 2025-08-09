package com.w3canvas.javacanvas.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageComparator {

    public static boolean compare(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(path2);

        if (!file1.exists()) {
            System.err.println("Error: File does not exist: " + path1);
            return false;
        }
        if (!file2.exists()) {
            System.err.println("Error: File does not exist: " + path2);
            return false;
        }

        try {
            BufferedImage img1 = ImageIO.read(file1);
            BufferedImage img2 = ImageIO.read(file2);

            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                System.err.println("Images have different dimensions.");
                return false;
            }

            for (int y = 0; y < img1.getHeight(); y++) {
                for (int x = 0; x < img1.getWidth(); x++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        System.err.println("Images differ at pixel (" + x + ", " + y + ")");
                        return false;
                    }
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

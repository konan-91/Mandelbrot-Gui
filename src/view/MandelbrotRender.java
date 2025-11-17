package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/*
 * Rendering panel for mandelbrot. Using BufferedImage over drawing zero-length lines as it is both
 * more simple and more computationally efficient.
 */
public class MandelbrotRender extends JPanel {
    private BufferedImage image;

    // Constructor which initialises panel where mandelbrot is rendered, setting dimension and initialising image
    public MandelbrotRender(int width, int height) { // ! Using a dimension object to set size of panel as method requires Dimension object
        setPreferredSize(new Dimension(width, height));
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    // Method which renders the image (colour each pixel based on whether
    public void renderMandelbrot(int[][] data, int maxIterations) {
        // If there is no data to render, throw an exception
        if (data == null) {
            throw new IllegalArgumentException("Data parameter cannot be null");
        }

        // Dimensions of image??
        int width = data[0].length;
        int height = data.length;

        // Iterate through data and colour each pixel of BufferedImage based on iterations before escape
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < height; x++) {
                // Get no. iterations for current pixel
                int iterations = data[y][x];

                // Colour image pixel based on iterations
                if (iterations >= maxIterations) {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    // gradient from 0 -> maxIterations
                    float ratio = (float) iterations / maxIterations;
                    int r = (int)(255 * ratio);
                    int g = (int)(255 * ratio);
                    int b = (int)(255 * ratio);
                    image.setRGB(x, y, new Color(r, g, b).getRGB());
                }
            }
        }

        // Redraw the panel
        repaint();
    }

    // Tell swing how to display BufferedImage? Verify
    public void paint(Graphics g) {
        super.paint(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

}

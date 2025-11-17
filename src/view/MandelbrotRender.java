package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

/*
 * Rendering panel for mandelbrot. Using BufferedImage over drawing zero-length lines as it is both
 * more computationally efficient.
 */
public class MandelbrotRender extends JPanel {
    private BufferedImage image;

    // Variables for highlighting section of image for zoom
    private Point dragStart;
    private Point dragEnd;
    private boolean dragging;

    // Constructor which initialises panel where mandelbrot is rendered, setting dimension and initialising image
    public MandelbrotRender(int width, int height) {
        // ! Using a dimension object to set size of panel as method requires Dimension object
        setPreferredSize(new Dimension(width, height));
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Adding a mouse event handler which triggers when the user presses and releases mouse 1 ???
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                dragStart = event.getPoint();
                dragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                dragEnd = event.getPoint();
                dragging = false;
                repaint();
            }
        });

        // Handles the actual dragging once m1 is held
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                dragEnd = event.getPoint();
                repaint();
            }
        });
    }

    // Method which renders the image (colour each pixel based on whether
    public void renderMandelbrot(int[][] data, int maxIterations) {
        // If there is no data to render, throw an exception
        if (data == null) {
            throw new IllegalArgumentException("Data parameter cannot be null");
        }

        // Image dimensions
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

    // Render BufferedImage onto JPanel's surface using Graphics object
    public void paint(Graphics g) {
        super.paint(g);

        // Render image
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }

        // Draw a zoom rectangle while dragging
        if (dragging && dragStart != null && dragEnd != null) {
            Graphics2D selection = (Graphics2D) g;
            selection.setColor(Color.RED);
            selection.setStroke(new BasicStroke(2));

            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);
            int width = Math.abs(dragEnd.x - dragStart.x);
            int height = Math.abs(dragEnd.y - dragStart.y);

            selection.drawRect(x, y, width, height);
        }
    }

    // Get the rectangle the user dragged
    public Rectangle getZoomRectangle() {
        if (dragStart != null && dragEnd != null) {
            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);
            int width = Math.abs(dragEnd.x - dragStart.x);
            int height = Math.abs(dragEnd.y - dragStart.y);
            return new Rectangle(x, y, width, height);
        }
        return null;
    }

    // Clear selection after dragging
    public void clearDragSelection() {
        dragStart = null;
        dragEnd = null;
        repaint();
    }

}

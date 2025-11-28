package delegate;

import model.MandelbrotModel; // Import the model to get the enum

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * The JPanel responsible for rendering the Mandelbrot set and handling
 * mouse interactions (zoom and pan).
 *
 * @author 250014506
 * @version 1
 * @since 1
 */
public class MandelbrotPanel extends JPanel {
    // Buffered image is used over drawing zero-length lines as it is more
    // computationally efficient
    private BufferedImage image;

    // For mouse dragging
    private Point dragStart;
    private Point dragEnd;

    private enum DragMode {NONE, ZOOM, PAN} // Enums for mouse action state

    private DragMode currentDragMode = DragMode.NONE;

    // Listeners for UI events
    private ZoomListener zoomListener;
    private PanListener panListener;

    private double currentMagnification = 1.0;

    /**
     * Interface for zoom selection listener.
     */
    public interface ZoomListener {
        /**
         * Called when a zoom rectangle has been selected.
         *
         * @param rect The selected rectangle in pixel coordinates.
         */
        void onZoomSelected(Rectangle rect);
    }

    /**
     * Interface for a pan operation listener.
     */
    public interface PanListener {
        /**
         * Called when a pan drag has been completed.
         *
         * @param X The horizontal distance panned in pixels.
         * @param Y The vertical distance panned in pixels.
         */
        void onPan(int X, int Y);
    }

    /**
     * Constructs the Mandelbrot rendering panel.
     *
     * @param width  The width of the panel.
     * @param height The height of the panel.
     */
    public MandelbrotPanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        MouseAdapter mouseHandler = new MouseAdapter() {
            /**
             * Detects mouse press to initiate a zoom (LMB) or pan (RMB).
             */
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    currentDragMode = DragMode.ZOOM;
                    dragStart = e.getPoint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    currentDragMode = DragMode.PAN;
                    dragStart = e.getPoint();
                    dragEnd = e.getPoint();
                }
            }

            /**
             * Updates the drag end point and repaints to show the visual feedback.
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentDragMode != DragMode.NONE) {
                    dragEnd = e.getPoint();
                    repaint();
                }
            }

            /**
             * Finalises drag operation, triggering a zoom/pan event.
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentDragMode == DragMode.ZOOM) {
                    // Handle zoom
                    Rectangle rect = getZoomRectangle();
                    if (rect != null && rect.width > 5 && rect.height > 5 && zoomListener != null) {
                        zoomListener.onZoomSelected(rect);
                    }
                } else if (currentDragMode == DragMode.PAN) {
                    // Handle pan
                    if (dragStart != null && dragEnd != null && panListener != null) {
                        int dx = dragEnd.x - dragStart.x;
                        int dy = dragEnd.y - dragStart.y;
                        panListener.onPan(dx, dy);
                    }
                }
                clearSelection();
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    /**
     * Sets the listener for zoom events.
     *
     * @param listener The listener to notify.
     */
    public void setZoomListener(ZoomListener listener) {
        this.zoomListener = listener;
    }

    /**
     * Sets the listener for pan events.
     *
     * @param listener The listener to notify.
     */
    public void setPanListener(PanListener listener) {
        this.panListener = listener;
    }

    /**
     * Renders the Mandelbrot data using a selected colour scheme.
     *
     * @param data          The iteration matrix.
     * @param maxIterations The cutoff for being inside the set.
     * @param magnification The current zoom magnification to display.
     * @param scheme        The colour scheme to use.
     */
    public void renderMandelbrot(int[][] data, int maxIterations, double magnification, MandelbrotModel.ColourScheme scheme) {
        if (data == null) return;
        this.currentMagnification = magnification;

        int height = data.length;
        int width = data[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int iterations = data[y][x];

                if (iterations >= maxIterations) {
                    // Points in the set are black
                    image.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    switch (scheme) {
                        case BLACK_AND_WHITE:
                            image.setRGB(x, y, Color.WHITE.getRGB());
                            break;

                        case BW_GRADIENT:
                            // Gradient from black to white
                            float ratio_bw = (float) iterations / maxIterations;
                            int val = (int) (255 * ratio_bw);
                            image.setRGB(x, y, new Color(val, val, val).getRGB());
                            break;

                        case RED:
                            float r_hue = 0.0f; // red
                            float r_sat = 1.0f;
                            float r_bri = iterations / (float) maxIterations;
                            image.setRGB(x, y, Color.getHSBColor(r_hue, r_sat, r_bri).getRGB());
                            break;

                        case GREEN:
                            float g_hue = 1.0f / 3.0f; // green
                            float g_sat = 1.0f;
                            float g_bri = iterations / (float) maxIterations;
                            image.setRGB(x, y, Color.getHSBColor(g_hue, g_sat, g_bri).getRGB());
                            break;

                        case BLUE:
                            float b_hue = 2.0f / 3.0f; // blue
                            float b_sat = 1.0f;
                            float b_bri = iterations / (float) maxIterations;
                            image.setRGB(x, y, Color.getHSBColor(b_hue, b_sat, b_bri).getRGB());
                            break;

                        case MULTI:
                            // Points outside the set are coloured based on escape time
                            // Iteration counts are mapped to hues and the mod operator is used
                            // to create repeating colour bands
                            // 'xx.xf' controls the width of the bands
                            // This colour scheme looks much better with higher iteration counts!
                            float hue = (iterations % 100) / 100.0f;


                            // Full saturation (1.0f) and brightness (1.0f) used for more vibrant colours.
                            Color c = Color.getHSBColor(hue, 1.0f, 1.0f);
                            image.setRGB(x, y, c.getRGB());
                            break;

                        default:
                            image.setRGB(x, y, Color.WHITE.getRGB());
                            break;
                    }
                }
            }
        }
        repaint();
    }

    /**
     * Overridden paintComponent for drawing the rendered image,
     * dragging feedback, and the magnification text.
     *
     * @param g The Graphics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw the Mandelbrot image
        if (image != null) {
            g2d.drawImage(image, 0, 0, null);
        }

        // Draw visual feedback for dragging
        if (dragStart != null && dragEnd != null) {
            if (currentDragMode == DragMode.ZOOM) {
                // Draw zoom rectangle
                g2d.setColor(Color.PINK);
                g2d.setStroke(new BasicStroke(2));
                Rectangle rect = getZoomRectangle();
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);

            } else if (currentDragMode == DragMode.PAN) {
                // Draw pan line
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(dragStart.x, dragStart.y, dragEnd.x, dragEnd.y);
            }
        }

        // Draw zoom magnification estimate
        String magString = String.format("%.2fx", currentMagnification);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        // Draw a black outline for visibility
        g2d.setColor(Color.BLACK);
        g2d.drawString(magString, 11, 21);
        // Draw the white text
        g2d.setColor(Color.WHITE);
        g2d.drawString(magString, 10, 20);
    }

    /**
     * Calculates the zoom rectangle based on current drag.
     * Forces the rectangle to be a square to maintain the aspect ratio,
     * else the image becomes distorted and stretched.
     *
     * @return A square rectangle representing the selected zoom area.
     */
    private Rectangle getZoomRectangle() {
        if (dragStart != null && dragEnd != null) {
            // Get top-left corner of drag
            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);

            // Get raw width and height
            int width = Math.abs(dragEnd.x - dragStart.x);
            int height = Math.abs(dragEnd.y - dragStart.y);

            // Get larger of two measurements to make square
            int side = Math.max(width, height);

            return new Rectangle(x, y, side, side);
        }
        return null;
    }

    /**
     * Clears the drag state and repaints the panel to remove drag visuals.
     */
    private void clearSelection() {
        dragStart = null;
        dragEnd = null;
        currentDragMode = DragMode.NONE;
        repaint();
    }

    /**
     * Method for saving the BufferedImage
     */
    public BufferedImage getImage() {
        return image;
    }
}
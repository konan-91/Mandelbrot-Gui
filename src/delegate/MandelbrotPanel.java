package delegate;

import model.MandelbrotModel; // Import the model to get the enum

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * The JPanel responsible for rendering the Mandelbrot set (the "View")
 * and handling all mouse interactions (zoom and pan).
 *
 * @author 250014506
 * @version 1
 * @since 1
 */
public class MandelbrotPanel extends JPanel {
    /** The off-screen buffer image where the Mandelbrot set is drawn. */
    private BufferedImage image;

    // For mouse dragging
    /** The screen coordinate where a mouse drag started. */
    private Point dragStart;
    /** The current screen coordinate of a mouse drag. */
    private Point dragEnd;
    /** Enum to track whether the user is zooming, panning, or not dragging. */
    private enum DragMode { NONE, ZOOM, PAN }
    /** The current mouse drag operation. */
    private DragMode currentDragMode = DragMode.NONE;

    // Listeners for UI events
    /** Listener to be notified when a zoom rectangle is selected. */
    private ZoomListener zoomListener;
    /** Listener to be notified when a pan operation is performed. */
    private PanListener panListener;

    // State for drawing
    /** The current magnification level to display. */
    private double currentMagnification = 1.0;
    /** Flag to control the display of the magnification text. */
    private final boolean showMagnification = true;

    /**
     * Functional interface for a zoom selection listener.
     */
    public interface ZoomListener {
        /**
         * Called when a zoom rectangle has been selected.
         * @param rect The selected rectangle in pixel coordinates.
         */
        void onZoomSelected(Rectangle rect);
    }

    /**
     * Functional interface for a pan operation listener.
     */
    public interface PanListener {
        /**
         * Called when a pan drag has been completed.
         * @param deltaX The horizontal distance panned in pixels.
         * @param deltaY The vertical distance panned in pixels.
         */
        void onPan(int deltaX, int deltaY);
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
             * Detects mouse press to initiate a zoom (left-click) or pan (right-click).
             */
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    currentDragMode = DragMode.ZOOM;
                    dragStart = e.getPoint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    currentDragMode = DragMode.PAN;
                    dragStart = e.getPoint();
                    dragEnd = e.getPoint(); // Init dragEnd for panning draw
                }
            }

            /**
             * Updates the drag-end point and repaints to show the drag visual feedback.
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentDragMode != DragMode.NONE) {
                    dragEnd = e.getPoint();
                    repaint(); // Redraw selection rectangle or pan vector
                }
            }

            /**
             * Finalises the drag operation, triggering a zoom or pan event.
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentDragMode == DragMode.ZOOM) {
                    // Handle Zoom
                    Rectangle rect = getZoomRectangle();
                    if (rect != null && rect.width > 5 && rect.height > 5 && zoomListener != null) {
                        zoomListener.onZoomSelected(rect);
                    }
                } else if (currentDragMode == DragMode.PAN) {
                    // Handle Pan
                    if (dragStart != null && dragEnd != null && panListener != null) {
                        int dx = dragEnd.x - dragStart.x;
                        int dy = dragEnd.y - dragStart.y;
                        // Only pan if drag is significant
                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                            panListener.onPan(dx, dy);
                        }
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
     * @param listener The listener to notify.
     */
    public void setZoomListener(ZoomListener listener) {
        this.zoomListener = listener;
    }

    /**
     * Sets the listener for pan events.
     * @param listener The listener to notify.
     */
    public void setPanListener(PanListener listener) {
        this.panListener = listener;
    }

    /**
     * Renders the Mandelbrot data onto the internal image using a selected colour scheme.
     *
     * @param data            The 2D iteration data.
     * @param maxIterations   The cutoff for 'in the set'.
     * @param magnification   The current zoom magnification to display.
     * @param scheme          The colour scheme to use for rendering.
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
                    // Points in the set are always black
                    image.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    // Colouring for points outside the set, based on the scheme
                    switch (scheme) {
                        case BLACK_AND_WHITE:
                            image.setRGB(x, y, Color.WHITE.getRGB());
                            break;

                        case BW_GRADIENT:
                            // Gradient from Black to White
                            float ratio_bw = (float) iterations / maxIterations;
                            int val = (int)(255 * ratio_bw);
                            image.setRGB(x, y, new Color(val, val, val).getRGB());
                            break;

                        case RED:
                            float r_hue = 0.0f; // red
                            float r_sat = 1.0f;
                            float r_bri = iterations / (float) maxIterations;
                            image.setRGB(x, y, Color.getHSBColor(r_hue, r_sat, r_bri).getRGB());
                            break;

                        case GREEN:
                            float g_hue = 1.0f/3.0f; // green
                            float g_sat = 1.0f;
                            float g_bri = iterations / (float) maxIterations;
                            image.setRGB(x, y, Color.getHSBColor(g_hue, g_sat, g_bri).getRGB());
                            break;

                        case BLUE:
                            float b_hue = 2.0f/3.0f; // blue
                            float b_sat = 1.0f;
                            float b_bri = iterations / (float) maxIterations;
                            image.setRGB(x, y, Color.getHSBColor(b_hue, b_sat, b_bri).getRGB());
                            break;

                        case SPECIAL:
                            // Points outside the set are coloured based on escape time
                            // Map iteration count to a hue (colour)
                            // We use a modulus operator (%) to create repeating colour bands.
                            // '50.0f' controls the width of the bands.
                            // Try changing it to 20.0f or 100.0f to see the effect.
                            float hue = (iterations % 50) / 50.0f;

                            // We use full saturation (1.0f) and brightness (1.0f) for vibrant colours.
                            Color c = Color.getHSBColor(hue, 1.0f, 1.0f);
                            image.setRGB(x, y, c.getRGB());
                            break;

                        default:
                            // Failsafe to Black & White
                            image.setRGB(x, y, Color.WHITE.getRGB());
                            break;
                    }
                }
            }
        }
        repaint();
    }

    /**
     * Overridden paintComponent to draw the pre-rendered image,
     * drag-and-drop visuals, and the magnification text.
     *
     * @param g The Graphics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw the pre-rendered Mandelbrot image
        if (image != null) {
            g2d.drawImage(image, 0, 0, null);
        }

        // Draw visual feedback for dragging
        if (dragStart != null && dragEnd != null) {
            if (currentDragMode == DragMode.ZOOM) {
                // Draw red zoom rectangle
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2));
                Rectangle rect = getZoomRectangle();
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);

            } else if (currentDragMode == DragMode.PAN) {
                // Draw blue pan vector line
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(dragStart.x, dragStart.y, dragEnd.x, dragEnd.y);
            }
        }

        // Draw zoom magnification estimate
        if (showMagnification) {
            String magString = String.format("%.2fx", currentMagnification);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            // Draw a black outline for visibility
            g2d.setColor(Color.BLACK);
            g2d.drawString(magString, 11, 21);
            // Draw the white text
            g2d.setColor(Color.WHITE);
            g2d.drawString(magString, 10, 20);
        }
    }

    /**
     * Calculates the zoom rectangle based on the current drag.
     * Forces the rectangle to be a square to maintain the 1:1 aspect ratio.
     *
     * @return A square Rectangle representing the selected zoom area.
     */
    private Rectangle getZoomRectangle() {
        if (dragStart != null && dragEnd != null) {
            // Get top-left corner of drag
            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);

            // Get raw (potentially non-square) width and height
            int width = Math.abs(dragEnd.x - dragStart.x);
            int height = Math.abs(dragEnd.y - dragEnd.y);

            // To maintain aspect ratio, force selection to be a square
            // by taking the larger of the two dimensions.
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
}
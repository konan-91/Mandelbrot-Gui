package delegate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

/**
 * The class responsible for ... complete this
 *
 * @author 250014506
 * @version 1
 * @since 1
 */
public class MandelbrotRender extends JPanel {
    private BufferedImage image;

    // For mouse dragging
    private Point dragStart;
    private Point dragEnd;
    private boolean dragging;

    // Listener for zoom
    private ZoomListener zoomListener;

    public interface ZoomListener {
        void onZoomSelected(Rectangle rect);
    }

    public MandelbrotRender(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Mouse press
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                dragging = true;
            }

            public void mouseReleased(MouseEvent e) {
                dragEnd = e.getPoint();
                dragging = false;

                Rectangle rect = getZoomRectangle();
                if (rect != null && rect.width > 5 && rect.height > 5 && zoomListener != null) {
                    zoomListener.onZoomSelected(rect);
                }

                clearSelection();
            }
        });

        // Mouse drag
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                dragEnd = e.getPoint();
                repaint();
            }
        });
    }

    public void setZoomListener(ZoomListener listener) {
        this.zoomListener = listener;
    }

    public void renderMandelbrot(int[][] data, int maxIterations) {
        if (data == null) return;

        int height = data.length;
        int width = data[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int iterations = data[y][x];

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

        repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }

        // Draw red rectangle while dragging
        if (dragging && dragStart != null && dragEnd != null) {
            g.setColor(Color.PINK);
            ((Graphics2D) g).setStroke(new BasicStroke(2));

            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);
            int width = Math.abs(dragEnd.x - dragStart.x);
            int height = Math.abs(dragEnd.y - dragStart.y);

            g.drawRect(x, y, width, height);
        }
    }

    private Rectangle getZoomRectangle() {
        if (dragStart != null && dragEnd != null) {
            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);
            int width = Math.abs(dragEnd.x - dragStart.x);
            int height = Math.abs(dragEnd.y - dragStart.y);
            return new Rectangle(x, y, width, height);
        }
        return null;
    }

    private void clearSelection() {
        dragStart = null;
        dragEnd = null;
        repaint();
    }
}
package delegate;

import model.MandelbrotModel;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private MandelbrotModel model;
    private MandelbrotRender panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    public MainFrame(MandelbrotModel model) {
        this.model = model;

        setTitle("Mandelbrot Set Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the drawing panel
        panel = new MandelbrotRender(WIDTH, HEIGHT);

        // Set up auto-zoom when user drags
        panel.setZoomListener(rect -> handleZoom(rect));

        add(panel, BorderLayout.CENTER);

        // Create toolbar (no zoom button needed)
        JToolBar toolbar = new JToolBar();

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> handleUndo());
        toolbar.add(undoButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> handleReset());
        toolbar.add(resetButton);

        add(toolbar, BorderLayout.NORTH);

        pack();
        setVisible(true);

        // Calculate and display initial mandelbrot set
        model.calculate(WIDTH, HEIGHT);
        panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
    }

    private void handleZoom(Rectangle rect) {
        // Convert pixel coordinates to complex plane coordinates
        double realRange = model.getMaxReal() - model.getMinReal();
        double imagRange = model.getMaxImaginary() - model.getMinImaginary();

        double newMinReal = model.getMinReal() + (rect.x * realRange / WIDTH);
        double newMaxReal = model.getMinReal() + ((rect.x + rect.width) * realRange / WIDTH);
        double newMinImag = model.getMinImaginary() + (rect.y * imagRange / HEIGHT);
        double newMaxImag = model.getMinImaginary() + ((rect.y + rect.height) * imagRange / HEIGHT);

        model.zoom(newMinReal, newMaxReal, newMinImag, newMaxImag);
        model.calculate(WIDTH, HEIGHT);
        panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
    }

    private void handleUndo() {
        if (model.canUndo()) {
            model.undo();
            model.calculate(WIDTH, HEIGHT);
            panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
        }
    }

    private void handleReset() {
        model.defaultValues();
        model.calculate(WIDTH, HEIGHT);
        panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
    }
}
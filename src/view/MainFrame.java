package view;

import model.MandelbrotModel;

import javax.swing.*;
import java.awt.*;

/*
 * This is the Delegate class ??, all other GUI elements sit within this scaffold.
*/
public class MainFrame extends JFrame {

    private MandelbrotModel model;
    private MandelbrotRender panel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    // The main GUI holder thing
    public MainFrame(MandelbrotModel model) {
        this.model = model;

        setTitle("Mandelbrot Set Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the drawing panel
        panel = new MandelbrotRender(WIDTH, HEIGHT);
        add(panel, BorderLayout.CENTER);

        // Create toolbar with buttons
        JToolBar toolbar = new JToolBar();

        JButton zoomButton = new JButton("Zoom to Selection");
        zoomButton.addActionListener(e -> handleZoom());
        toolbar.add(zoomButton);

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> handleUndo());
        toolbar.add(undoButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> handleReset());
        toolbar.add(resetButton);

        add(toolbar, BorderLayout.NORTH);

        // Make window fit panel
        pack();
        setVisible(true);

        // Calculate and display mandelbrot set
        model.calculate(WIDTH, HEIGHT);
        panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
    }

    private void handleZoom() {
        Rectangle rect = panel.getZoomRectangle();

        // Check if rectangle is valid
        if (rect == null || rect.width < 5 || rect.height < 5) {
            return;
        }

        // Convert pixel coordinates to complex plane coordinates
        double realRange = model.getMaxReal() - model.getMinReal();
        double imagRange = model.getMaxImaginary() - model.getMinImaginary();

        double newMinReal = model.getMinReal() + (rect.x * realRange / WIDTH);
        double newMaxReal = model.getMinReal() + ((rect.x + rect.width) * realRange / WIDTH);
        double newMinImag = model.getMinImaginary() + (rect.y * imagRange / HEIGHT);
        double newMaxImag = model.getMinImaginary() + ((rect.y + rect.height) * imagRange / HEIGHT);

        // Zoom in model
        model.zoom(newMinReal, newMaxReal, newMinImag, newMaxImag);

        // Recalculate and redraw
        model.calculate(WIDTH, HEIGHT);
        panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());

        // Clear the selection rectangle
        panel.clearDragSelection();
    }

    private void handleUndo() {
        if (model.canUndo()) {
            model.undo();
            model.calculate(WIDTH, HEIGHT);
            panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
        }
    }

    private void handleReset() {
        model.defaultValues(); // !!! verify this is okay
        model.calculate(WIDTH, HEIGHT);
        panel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
    }

}

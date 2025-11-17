package view;

import model.MandelbrotModel;

import javax.swing.*;
import java.awt.*;

/*
 * This is the Delegate class, all other GUI elements sit within this scaffold.
*/
public class MainFrame extends JFrame {

    // ??? Add desc later
    private MandelbrotModel model;
    private MandelbrotRender mandelbrotPanel;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    public MainFrame(MandelbrotModel model) {
        this.model = model;

        setTitle("Mandelbrot Set Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create the drawing panel
        mandelbrotPanel = new MandelbrotRender(WIDTH, HEIGHT);
        add(mandelbrotPanel, BorderLayout.CENTER);

        // Make window fit panel
        pack();
        setVisible(true);

        // Calculate and display mandelbrot set
        model.calculate(WIDTH, HEIGHT);
        mandelbrotPanel.renderMandelbrot(model.getMandelbrotData(), model.getMaxIterations());
    }

}

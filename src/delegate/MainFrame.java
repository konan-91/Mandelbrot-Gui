package delegate;

import model.MandelbrotModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The main application window (JFrame).
 * This class acts as the Controller in the MVC-like pattern.
 * It sets up the UI components (panel, buttons, slider) and listens for
 * updates from the model to refresh the view.
 */
public class MainFrame extends JFrame implements PropertyChangeListener {
    /** The data model containing the Mandelbrot state. */
    private final MandelbrotModel model;
    /** The panel that renders the Mandelbrot set. */
    private final MandelbrotPanel panel;

    /** Button to undo the last action. */
    private final JButton undoButton;
    /** Button to redo the last undone action. */
    private final JButton redoButton;
    /** Slider to control the maximum iterations. */
    private final JSlider iterationSlider;
    /** Selection box for the colour scheme. */
    private final JComboBox<MandelbrotModel.ColourScheme> colourBox;

    /** The fixed width of the rendering panel. */
    private static final int WIDTH = 800;
    /** The fixed height of the rendering panel. */
    private static final int HEIGHT = 800;

    /**
     * Constructs the main application frame.
     *
     * @param model The MandelbrotModel to use for state management.
     */
    public MainFrame(MandelbrotModel model) {
        this.model = model;

        setTitle("Mandelbrot Set Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the drawing panel
        panel = new MandelbrotPanel(WIDTH, HEIGHT);
        add(panel, BorderLayout.CENTER);

        // Set up listeners for zoom (left-drag) and pan (right-drag)
        panel.setZoomListener(this::handleZoom);
        panel.setPanListener(this::handlePan);

        // --- Create Control Panel (Bottom) ---
        // We use a main panel with BorderLayout to hold two rows
        JPanel mainControlPanel = new JPanel(new BorderLayout());

        // Top row: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> model.undo());
        buttonPanel.add(undoButton);

        redoButton = new JButton("Redo");
        redoButton.addActionListener(e -> model.redo());
        buttonPanel.add(redoButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> model.defaultValues());
        buttonPanel.add(resetButton);

        mainControlPanel.add(buttonPanel, BorderLayout.NORTH);

        // Bottom row: Sliders and settings
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        // Max Iterations Slider
        settingsPanel.add(new JLabel("Max Iterations:"));
        // Changed max to 1500 as requested
        iterationSlider = new JSlider(50, 1500, model.getMaxIterations());
        iterationSlider.setMajorTickSpacing(350);
        iterationSlider.setMinorTickSpacing(100);
        iterationSlider.setPaintTicks(true);
        iterationSlider.setPaintLabels(true);
        iterationSlider.addChangeListener(e -> {
            if (!iterationSlider.getValueIsAdjusting()) {
                model.setMaxIterations(iterationSlider.getValue());
            }
        });
        settingsPanel.add(iterationSlider);

        // Colour Scheme Selection Box
        settingsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        settingsPanel.add(new JLabel("Colour Scheme:"));
        colourBox = new JComboBox<>(MandelbrotModel.ColourScheme.values());
        colourBox.setSelectedItem(model.getColourScheme());
        colourBox.addActionListener(e -> {
            // Tell the model to update the scheme
            model.setColourScheme((MandelbrotModel.ColourScheme) colourBox.getSelectedItem());
        });
        settingsPanel.add(colourBox);

        mainControlPanel.add(settingsPanel, BorderLayout.CENTER);

        add(mainControlPanel, BorderLayout.SOUTH);

        // --- Finalise Frame ---
        pack();
        setLocationRelativeTo(null); // Centre on screen
        setVisible(true);

        // Listen for model changes to update the view
        model.addPropertyChangeListener(this);

        // Trigger initial calculation and render
        updateView(model);
    }

    /**
     * Handles the zoom event from the panel by converting pixel coordinates
     * to complex plane coordinates and telling the model to zoom.
     *
     * @param rect The pixel-based rectangle selected by the user.
     */
    private void handleZoom(Rectangle rect) {
        // Convert pixel coordinates to complex plane coordinates
        double realRange = model.getMaxReal() - model.getMinReal();
        double imagRange = model.getMaxImaginary() - model.getMinImaginary();

        double newMinReal = model.getMinReal() + (rect.x * realRange / WIDTH);
        double newMaxReal = model.getMinReal() + ((rect.x + rect.width) * realRange / WIDTH);
        double newMinImag = model.getMinImaginary() + (rect.y * imagRange / HEIGHT);
        double newMaxImag = model.getMinImaginary() + ((rect.y + rect.height) * imagRange / HEIGHT);

        // Tell the model to zoom; the PropertyChangeListener will handle the update
        model.zoom(newMinReal, newMaxReal, newMinImag, newMaxImag);
    }

    /**
     * Handles the pan event from the panel by converting pixel distances
     * to a shift in the complex plane.
     *
     * @param deltaX The horizontal pixel distance dragged.
     * @param deltaY The vertical pixel distance dragged.
     */
    private void handlePan(int deltaX, int deltaY) {
        double realRange = model.getMaxReal() - model.getMinReal();
        double imagRange = model.getMaxImaginary() - model.getMinImaginary();

        // Calculate the shift in the complex plane
        // A drag to the right (positive deltaX) moves the view left (negative shift)
        double realShift = (deltaX * realRange) / WIDTH;
        double imagShift = (deltaY * imagRange) / HEIGHT;

        // Tell the model to pan
        model.pan(realShift, imagShift);
    }

    /**
     * This method is called whenever the model fires a PropertyChangeEvent.
     * It triggers a full update of the view.
     *
     * @param evt The event fired by the model.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("modelUpdated".equals(evt.getPropertyName())) {
            MandelbrotModel updatedModel = (MandelbrotModel) evt.getNewValue();
            updateView(updatedModel);
        }
    }

    /**
     * Centralised method to update all view components based on the model's state.
     * This recalculates, re-renders, and updates all UI controls.
     *
     * @param model The model containing the new state.
     */
    private void updateView(MandelbrotModel model) {
        // 1. Recalculate the set
        model.calculate(WIDTH, HEIGHT);

        // 2. Re-render the image and magnification
        panel.renderMandelbrot(
                model.getMandelbrotData(),
                model.getMaxIterations(),
                model.getMagnification(),
                model.getColourScheme() // Pass the new scheme
        );

        // 3. Update slider position (e.g., after an undo/reset)
        iterationSlider.setValue(model.getMaxIterations());

        // 4. Update button enabled state
        undoButton.setEnabled(model.canUndo());
        redoButton.setEnabled(model.canRedo());

        // 5. Update colour box selection (e.g., after an undo/reset)
        colourBox.setSelectedItem(model.getColourScheme());
    }
}
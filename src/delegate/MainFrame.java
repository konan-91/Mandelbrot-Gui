package delegate;

import model.MandelbrotModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The main application window (JFrame).
 * This class sets up the UI components (panel, buttons, slider) and listens for
 * updates from the model to refresh the view.
 *
 * @author 250014506
 * @version 1
 * @since 1
 */
public class MainFrame extends JFrame implements PropertyChangeListener {
    private final MandelbrotModel model;
    private final MandelbrotPanel panel;

    // Initialise UI elements
    private final JButton undoButton;
    private final JButton redoButton;
    private final JSlider iterationSlider;
    private final JComboBox<MandelbrotModel.ColourScheme> colourBox;

    private static final int WIDTH = 800;
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

        // Create the panel
        panel = new MandelbrotPanel(WIDTH, HEIGHT);
        add(panel, BorderLayout.CENTER);

        // Set up listeners for zooming and panning
        panel.setZoomListener(this::handleZoom);
        panel.setPanListener(this::handlePan);

        // ------ Creating Bottom Panel ------

        // Main panel which holds two rows
        JPanel mainControlPanel = new JPanel(new BorderLayout());
        // Top row for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        undoButton = new JButton("Undo");
        undoButton.addActionListener(_ -> model.undo());
        buttonPanel.add(undoButton);

        redoButton = new JButton("Redo");
        redoButton.addActionListener(_ -> model.redo());
        buttonPanel.add(redoButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(_ -> model.defaultValues());
        buttonPanel.add(resetButton);

        mainControlPanel.add(buttonPanel, BorderLayout.NORTH);

        // Bottom row for sliders and settings
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        // Max iterations slider
        settingsPanel.add(new JLabel("Max Iterations:"));
        // Picked sensible defaults (50 min, 1500 max as processing time becomes quite slow)
        iterationSlider = new JSlider(50, 1500, model.getMaxIterations());
        iterationSlider.setMajorTickSpacing(350);
        iterationSlider.setMinorTickSpacing(100);
        iterationSlider.setPaintTicks(true);
        iterationSlider.setPaintLabels(true);
        iterationSlider.addChangeListener(_ -> {
            if (!iterationSlider.getValueIsAdjusting()) {
                model.setMaxIterations(iterationSlider.getValue());
            }
        });
        settingsPanel.add(iterationSlider);

        // Colour scheme selection box
        settingsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        settingsPanel.add(new JLabel("Colour Scheme:"));
        colourBox = new JComboBox<>(MandelbrotModel.ColourScheme.values());
        colourBox.setSelectedItem(model.getColourScheme());
        colourBox.addActionListener(_ -> {
            model.setColourScheme((MandelbrotModel.ColourScheme) colourBox.getSelectedItem());
        });
        settingsPanel.add(colourBox);

        mainControlPanel.add(settingsPanel, BorderLayout.CENTER);
        add(mainControlPanel, BorderLayout.SOUTH);


        // Finalising GUI
        setLocationRelativeTo(null); // Centre on screen
        setVisible(true);

        // Listen for model changes and trigger initial calculation & render
        model.addPropertyChangeListener(this);
        updateView(model);
    }

    /**
     * Handles the zoom event from the panel by converting pixel coordinates
     * to complex plane coordinates and telling the model to zoom.
     *
     * @param rect The rectangle of pixels selected by the user.
     */
    private void handleZoom(Rectangle rect) {
        // Convert pixel coordinates to complex plane coordinates
        double realRange = model.getMaxReal() - model.getMinReal();
        double imagRange = model.getMaxImaginary() - model.getMinImaginary();

        double newMinReal = model.getMinReal() + (rect.x * realRange / WIDTH);
        double newMaxReal = model.getMinReal() + ((rect.x + rect.width) * realRange / WIDTH);
        double newMinImag = model.getMinImaginary() + (rect.y * imagRange / HEIGHT);
        double newMaxImag = model.getMinImaginary() + ((rect.y + rect.height) * imagRange / HEIGHT);

        // Tell the model to zoom (PropertyChangeListener handles the update)
        model.zoom(newMinReal, newMaxReal, newMinImag, newMaxImag);
    }

    /**
     * Handles panning by converting pixel distances to a shift in the complex plane.
     *
     * @param deltaX The horizontal pixel distance dragged.
     * @param deltaY The vertical pixel distance dragged.
     */
    private void handlePan(int deltaX, int deltaY) {
        double realRange = model.getMaxReal() - model.getMinReal();
        double imagRange = model.getMaxImaginary() - model.getMinImaginary();

        // Calculate the shift
        double realShift = (deltaX * realRange) / WIDTH;
        double imagShift = (deltaY * imagRange) / HEIGHT;

        // Tell the model to pan
        model.pan(realShift, imagShift);
    }

    /**
     * This method is called whenever the model fires a PropertyChangeEvent.
     * A full update of the view is triggered.
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
     * Method for updating all view components based on the model's state.
     * This recalculates, re-renders, and updates UI elements.
     *
     * @param model The model containing the new state.
     */
    private void updateView(MandelbrotModel model) {
        // Recalculate the set
        model.calculate(WIDTH, HEIGHT);

        // Re-render the image and magnification
        panel.renderMandelbrot(
                model.getMandelbrotData(),
                model.getMaxIterations(),
                model.getMagnification(),
                model.getColourScheme()
        );

        // Update slider position
        iterationSlider.setValue(model.getMaxIterations());

        // Update button enabled state
        undoButton.setEnabled(model.canUndo());
        redoButton.setEnabled(model.canRedo());

        // Update colour scheme box selection
        colourBox.setSelectedItem(model.getColourScheme());
    }
}
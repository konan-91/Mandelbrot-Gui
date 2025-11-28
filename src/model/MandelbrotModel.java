package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;

/**
 * The Model for the Mandelbrot viewer.
 * This class manages all data, including complex plane coordinates,
 * iteration count, undo/redo stacks, and the calculated Mandelbrot data.
 * It follows the observer pattern, firing property changes when its state is updated.
 *
 * @author 250014506
 * @version 1
 * @since 1
 */
public class MandelbrotModel {
    private final MandelbrotCalculator mandelbrotCalculator;
    private final PropertyChangeSupport pcs; // Handles adding, removing, and notifying PropertyChangeListeners

    // Enums for colour schemes
    public enum ColourScheme {
        BLACK_AND_WHITE, BW_GRADIENT, RED, GREEN, BLUE, MULTI
    }

    // Parameters that initialise a default view and colour scheme for the Mandelbrot data
    private static final double INITIAL_MIN_REAL = MandelbrotCalculator.INITIAL_MIN_REAL;
    private static final double INITIAL_MAX_REAL = MandelbrotCalculator.INITIAL_MAX_REAL;
    private static final double INITIAL_MIN_IMAGINARY = MandelbrotCalculator.INITIAL_MIN_IMAGINARY;
    private static final double INITIAL_MAX_IMAGINARY = MandelbrotCalculator.INITIAL_MAX_IMAGINARY;
    private static final int INITIAL_MAX_ITERATIONS = MandelbrotCalculator.INITIAL_MAX_ITERATIONS;
    public static final ColourScheme INITIAL_COLOUR_SCHEME = ColourScheme.BLACK_AND_WHITE;

    // Store the initial span of the image in order to calculate the zoom level
    private static final double INITIAL_REAL_RANGE = INITIAL_MAX_REAL - INITIAL_MIN_REAL;

    // Parameters for the current state of the Mandelbrot data
    private double minReal; // Left edge
    private double maxReal; // Right edge
    private double minImaginary; // Top edge
    private double maxImaginary; // Bottom edge
    private int maxIterations;
    private ColourScheme currentScheme;

    // Matrix for holding iteration data for current view
    private int[][] mandelbrotData;

    // Deques for undo/redo operations
    private final ArrayDeque<Parameters> undoStack;
    private final ArrayDeque<Parameters> redoStack;

    /**
     * An inner class / data structure for storing snapshots of the model's parameters
     * for appending to undo/redo stacks.
     */
    private static class Parameters {
        final double minReal, maxReal, minImaginary, maxImaginary;
        final int maxIterations;
        final ColourScheme colourScheme;

        // Constructs snapshot
        Parameters(double minReal, double maxReal, double minImaginary, double maxImaginary, int maxIterations, ColourScheme colourScheme) {
            this.minReal = minReal;
            this.maxReal = maxReal;
            this.minImaginary = minImaginary;
            this.maxImaginary = maxImaginary;
            this.maxIterations = maxIterations;
            this.colourScheme = colourScheme;
        }
    }

    /**
     * Constructor for MandelbrotModel.
     * Initialises the calculator, listeners, and stacks, and sets default values.
     */
    public MandelbrotModel() {
        mandelbrotCalculator = new MandelbrotCalculator();
        pcs = new PropertyChangeSupport(this);
        undoStack = new ArrayDeque<>();
        redoStack = new ArrayDeque<>();
        defaultValues();
    }

    // ------ State Management ------

    /**
     * Resets the model to its initial state.
     * Clears all stacks and notifies listeners.
     */
    public void defaultValues() {
        minReal = INITIAL_MIN_REAL;
        maxReal = INITIAL_MAX_REAL;
        minImaginary = INITIAL_MIN_IMAGINARY;
        maxImaginary = INITIAL_MAX_IMAGINARY;
        maxIterations = INITIAL_MAX_ITERATIONS;
        currentScheme = INITIAL_COLOUR_SCHEME;
        undoStack.clear();
        redoStack.clear();
        update();
    }

    /**
     * Calculates the Mandelbrot set data based on current state parameters
     * and the given resolution.
     *
     * @param width  The pixel width of the view.
     * @param height The pixel height of the view.
     */
    public void calculate(int width, int height) {
        mandelbrotData = mandelbrotCalculator.calcMandelbrotSet(
                width, height,
                minReal, maxReal,
                minImaginary, maxImaginary,
                maxIterations, MandelbrotCalculator.DEFAULT_RADIUS_SQUARED
        );
    }

    /**
     * Zooms to selected area.
     * Appends old state to the undo stack and notifies listeners.
     *
     * @param newMinReal      The new minimum real value.
     * @param newMaxReal      The new maximum real value.
     * @param newMinImaginary The new minimum imaginary value.
     * @param newMaxImaginary The new maximum imaginary value.
     */
    public void zoom(double newMinReal, double newMaxReal, double newMinImaginary, double newMaxImaginary) {
        pushCurrentStateToUndo();
        minReal = newMinReal;
        maxReal = newMaxReal;
        minImaginary = newMinImaginary;
        maxImaginary = newMaxImaginary;
        update();
    }

    /**
     * Pans the view by a given amount in the complex plane.
     * Appends old state to the undo stack and notifies listeners.
     *
     * @param realShift The amount to shift the real values.
     * @param imagShift The amount to shift the imaginary values.
     */
    public void pan(double realShift, double imagShift) {
        pushCurrentStateToUndo();
        // Add the shift amount to the bounds
        minReal = minReal + realShift;
        maxReal = maxReal + realShift;
        minImaginary = minImaginary + imagShift;
        maxImaginary = maxImaginary + imagShift;
        update();
    }

    /**
     * Sets a new maximum iteration count.
     * Appends old state to the undo stack and notifies listeners.
     *
     * @param iterations The new maximum iteration count.
     */
    public void setMaxIterations(int iterations) {
        if (iterations == this.maxIterations) return; // No change, do nothing!
        pushCurrentStateToUndo();
        this.maxIterations = iterations;
        update();
    }

    /**
     * Sets a new colour scheme.
     * Appends old state to the undo stack and notifies listeners.
     *
     * @param scheme The new colour scheme to use.
     */
    public void setColourScheme(ColourScheme scheme) {
        if (scheme == this.currentScheme) return; // No change
        pushCurrentStateToUndo();
        this.currentScheme = scheme;
        update();
    }

    // ------ Undo / Redo Logic ------

    /**
     * Appends current state to the undo stack and clears the redo stack.
     * This method is called before new user actions (zoom, pan, iteration, colour change).
     */
    private void pushCurrentStateToUndo() {
        undoStack.push(new Parameters(minReal, maxReal, minImaginary, maxImaginary, maxIterations, currentScheme));
        redoStack.clear();
    }

    /**
     * Reverts to the previous state from the undo stack.
     * The current state is appended to the redo stack.
     */
    public void undo() {
        if (canUndo()) {
            redoStack.push(new Parameters(minReal, maxReal, minImaginary, maxImaginary, maxIterations, currentScheme));
            loadParameters(undoStack.pop()); // Load previous state by popping from stack
        }
    }

    /**
     * Load recent state from the redo stack.
     * The current state is appended back onto the undo stack.
     */
    public void redo() {
        if (canRedo()) {
            undoStack.push(new Parameters(minReal, maxReal, minImaginary, maxImaginary, maxIterations, currentScheme));
            loadParameters(redoStack.pop()); // Load next state by popping from stack
        }
    }

    /**
     * Helper method for loading state from Parameter objects and firing an update.
     *
     * @param params The Parameters object to load.
     */
    private void loadParameters(Parameters params) {
        minReal = params.minReal;
        maxReal = params.maxReal;
        minImaginary = params.minImaginary;
        maxImaginary = params.maxImaginary;
        maxIterations = params.maxIterations;
        currentScheme = params.colourScheme;
        update(); // Update view
    }

    // --- Property Change Support (Observer Pattern) ---

    /**
     * Notifies all registered listeners that the model state has changed.
     */
    private void update() {
        // Passing 'this' as new value means the listener can get the new state.
        pcs.firePropertyChange("modelUpdated", null, this);
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param listener The listener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    // ------ Getter Methods ------

    /**
     * @return The matrix of iteration data.
     */
    public int[][] getMandelbrotData() {
        return mandelbrotData;
    }

    /**
     * @return The current maximum iteration count.
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * @return The current selected colour scheme.
     */
    public ColourScheme getColourScheme() {
        return currentScheme;
    }

    /**
     * @return True if the undo stack is not empty, false otherwise.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * @return True if the redo stack is not empty, false otherwise.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * @return The current minimum real value.
     */
    public double getMinReal() {
        return minReal;
    }

    /**
     * @return The current maximum real value.
     */
    public double getMaxReal() {
        return maxReal;
    }

    /**
     * @return The current minimum imaginary value.
     */
    public double getMinImaginary() {
        return minImaginary;
    }

    /**
     * @return The current maximum imaginary value.
     */
    public double getMaxImaginary() {
        return maxImaginary;
    }

    /**
     * Calculates the current zoom magnification relative to the initial view.
     *
     * @return The magnification factor (e.g, 10x, 100x, 1000x...).
     */
    public double getMagnification() {
        double currentRealRange = maxReal - minReal;
        return INITIAL_REAL_RANGE / currentRealRange;
    }
}
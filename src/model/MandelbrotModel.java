package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;

/**
 * The Model component for the Mandelbrot viewer.
 * This class manages all state, including the current complex plane coordinates,
 * iteration count, undo/redo stacks, and the calculated Mandelbrot data.
 * It follows the observer pattern, firing property changes when its state is updated.
 */
public class MandelbrotModel {
    /** The utility class for performing the Mandelbrot calculation. */
    private final MandelbrotCalculator mandelbrotCalculator;
    /** Handles adding, removing, and notifying PropertyChangeListeners. */
    private final PropertyChangeSupport pcs;

    /**
     * Enum representing the available colour mapping schemes.
     */
    public enum ColourScheme {
        BLACK_AND_WHITE, BW_GRADIENT, RED, GREEN, BLUE, SPECIAL
    }

    // Constants for default state
    private static final double INITIAL_MIN_REAL = MandelbrotCalculator.INITIAL_MIN_REAL;
    private static final double INITIAL_MAX_REAL = MandelbrotCalculator.INITIAL_MAX_REAL;
    private static final double INITIAL_MIN_IMAGINARY = MandelbrotCalculator.INITIAL_MIN_IMAGINARY;
    private static final double INITIAL_MAX_IMAGINARY = MandelbrotCalculator.INITIAL_MAX_IMAGINARY;
    private static final int INITIAL_MAX_ITERATIONS = MandelbrotCalculator.INITIAL_MAX_ITERATIONS;
    /** The default colour scheme to use on startup and reset. */
    public static final ColourScheme INITIAL_COLOUR_SCHEME = ColourScheme.BLACK_AND_WHITE;

    /** The real-number range of the initial, default view. */
    private static final double INITIAL_REAL_RANGE = INITIAL_MAX_REAL - INITIAL_MIN_REAL;

    // Current state parameters
    /** The minimum real value (left edge) of the complex plane being viewed. */
    private double minReal;
    /** The maximum real value (right edge) of the complex plane being viewed. */
    private double maxReal;
    /** The minimum imaginary value (top edge) of the complex plane being viewed. */
    private double minImaginary;
    /** The maximum imaginary value (bottom edge) of the complex plane being viewed. */
    private double maxImaginary;
    /** The current maximum iteration count. */
    private int maxIterations;
    /** The current selected colour scheme. */
    private ColourScheme currentScheme;

    /** The 2D array holding the calculated iteration data for the current view. */
    private int[][] mandelbrotData;

    // Deques for undo/redo
    /** A stack holding previous parameter states for the undo operation. */
    private final ArrayDeque<Parameters> undoStack;
    /** A stack holding undone parameter states for the redo operation. */
    private final ArrayDeque<Parameters> redoStack;

    /**
     * An immutable inner class to store a complete snapshot of the model's
     * parameters for the undo/redo stacks.
     */
    private static class Parameters {
        final double minReal, maxReal, minImaginary, maxImaginary;
        final int maxIterations;
        final ColourScheme colourScheme;

        /**
         * Constructs a new Parameters snapshot.
         */
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
     * Constructs the MandelbrotModel.
     * Initialises the calculator, listeners, and stacks, and sets default values.
     */
    public MandelbrotModel() {
        mandelbrotCalculator = new MandelbrotCalculator();
        pcs = new PropertyChangeSupport(this);
        undoStack = new ArrayDeque<>();
        redoStack = new ArrayDeque<>();
        defaultValues();
    }

    // --- State Management ---

    /**
     * Resets the model to its initial, default state.
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
        fireUpdate();
    }

    /**
     * Calculates the Mandelbrot set data based on the current state parameters
     * and the given screen resolution.
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
     * Zooms to a new selected area.
     * Pushes the old state to the undo stack and notifies listeners.
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
        fireUpdate();
    }

    /**
     * Pans the view by a given shift in the complex plane.
     * Pushes the old state to the undo stack and notifies listeners.
     *
     * @param realShift The amount to shift the real values.
     * @param imagShift The amount to shift the imaginary values.
     */
    public void pan(double realShift, double imagShift) {
        pushCurrentStateToUndo();
        // A pan is a translation, so we subtract the shift from the bounds
        minReal = minReal - realShift;
        maxReal = maxReal - realShift;
        minImaginary = minImaginary - imagShift;
        maxImaginary = maxImaginary - imagShift;
        fireUpdate();
    }

    /**
     * Sets a new maximum iteration count.
     * Pushes the old state to the undo stack and notifies listeners.
     *
     * @param iterations The new maximum iteration count.
     */
    public void setMaxIterations(int iterations) {
        if (iterations == this.maxIterations) return; // No change
        pushCurrentStateToUndo();
        this.maxIterations = iterations;
        fireUpdate();
    }

    /**
     * Sets a new colour scheme.
     * Pushes the old state to the undo stack and notifies listeners.
     *
     * @param scheme The new colour scheme to use.
     */
    public void setColourScheme(ColourScheme scheme) {
        if (scheme == this.currentScheme) return; // No change
        pushCurrentStateToUndo();
        this.currentScheme = scheme;
        fireUpdate();
    }

    // --- Undo / Redo Logic ---

    /**
     * Pushes the *current* state to the undo stack and clears the redo stack.
     * This is called before any new user action (zoom, pan, iteration, colour change).
     */
    private void pushCurrentStateToUndo() {
        undoStack.push(new Parameters(minReal, maxReal, minImaginary, maxImaginary, maxIterations, currentScheme));
        redoStack.clear(); // A new action invalidates the old redo history
    }

    /**
     * Reverts to the previous state from the undo stack.
     * The current state is pushed to the redo stack.
     */
    public void undo() {
        if (canUndo()) {
            // Push current state to redo stack
            redoStack.push(new Parameters(minReal, maxReal, minImaginary, maxImaginary, maxIterations, currentScheme));
            // Pop and load previous state from undo stack
            loadParameters(undoStack.pop());
        }
    }

    /**
     * Re-applies a state from the redo stack.
     * The current state is pushed back onto the undo stack.
     */
    public void redo() {
        if (canRedo()) {
            // Push current state back to undo stack
            undoStack.push(new Parameters(minReal, maxReal, minImaginary, maxImaginary, maxIterations, currentScheme));
            // Pop and load next state from redo stack
            loadParameters(redoStack.pop());
        }
    }

    /**
     * Helper method to load a state from a Parameters object and fire an update.
     *
     * @param p The Parameters object to load.
     */
    private void loadParameters(Parameters p) {
        minReal = p.minReal;
        maxReal = p.maxReal;
        minImaginary = p.minImaginary;
        maxImaginary = p.maxImaginary;
        maxIterations = p.maxIterations;
        currentScheme = p.colourScheme;
        fireUpdate(); // Notify listeners to update the view
    }

    // --- Property Change Support (Observer Pattern) ---

    /**
     * Notifies all registered listeners that the model state has changed.
     */
    private void fireUpdate() {
        // "modelUpdated" is the event name.
        // We pass 'this' as the new value so the listener can get the new state.
        pcs.firePropertyChange("modelUpdated", null, this);
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param listener The listener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    // --- Getters ---

    /**
     * @return The 2D array of calculated iteration data.
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
     * @return The magnification factor (e.g., 1.0, 10.0, 1000.0).
     */
    public double getMagnification() {
        double currentRealRange = maxReal - minReal;
        return INITIAL_REAL_RANGE / currentRealRange;
    }
}
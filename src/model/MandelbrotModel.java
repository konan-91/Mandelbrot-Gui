package model;

import java.util.ArrayDeque;

public class MandelbrotModel {
    private MandelbrotCalculator mandelbrotCalculator;

    // INITIAL PARAMETERS
    // Defining the boundaries for the image (x=real, y=imaginary !!!verify this)
    private double minReal, maxReal, minImaginary, maxImaginary;

    // Number of times Mandelbrot iteration formula is applied to a complex number before its decided it will not escape
    // More iterations means more values will escape, which means a higher-resolution image
    private int maxIterations;

    // A matrix representing pixels in a grid, storing the number of iterations it took the value corresponding to each
    // pixel to escape (if it escaped at all)
    private int[][] mandelbrotData;

    // Deques support last-in-first-out stack behaviour, meaning parameters can be stored
    private ArrayDeque<Parameters> undoStack; // !!! Redo can be implemented by ???

    // Class to store parameters for undo / redo stack
    private static class Parameters {
        double minReal, maxReal, minImaginary, maxImaginary;

        Parameters(double minReal, double maxReal, double minImaginary, double maxImaginary) {
            this.minReal = minReal;
            this.maxReal = maxReal;
            this.minImaginary = minImaginary;
            this.maxImaginary = maxImaginary;
        }
    }

    // A constructor method for the mandelbrot set
    public MandelbrotModel() {
        mandelbrotCalculator = new MandelbrotCalculator();
        undoStack = new ArrayDeque<>();
        defaultValues();
    }


    // Initialise with default values (fully zoomed-out mandelbrot), clear stack
    public void defaultValues() {
        minReal = MandelbrotCalculator.INITIAL_MIN_REAL;
        maxReal = MandelbrotCalculator.INITIAL_MAX_REAL;
        minImaginary = MandelbrotCalculator.INITIAL_MIN_IMAGINARY;
        maxImaginary = MandelbrotCalculator.INITIAL_MAX_IMAGINARY;
        maxIterations = MandelbrotCalculator.INITIAL_MAX_ITERATIONS;
        undoStack.clear();
    }

    // Calculate the mandelbrot set for given width and height and update mandelbrotData[][]
    // For now, do not change other parameters, just render the default image
    public void calculate(int width, int height) {
        mandelbrotData = mandelbrotCalculator.calcMandelbrotSet(
                width, height,
                minReal, maxReal,
                minImaginary, maxImaginary,
                maxIterations, mandelbrotCalculator.DEFAULT_RADIUS_SQUARED // !!! what is default radius squared?
        );
    }

    // Zooms on selected area...
    public void zoom(double newMinReal, double newMaxReal, double newMinImaginary, double newMaxImaginary) {
        // Add current parameters to undo stack
        undoStack.push(new Parameters(minReal, maxReal, minImaginary, maxImaginary));

        // Set new parameters
        minReal = newMinReal;
        maxReal = newMaxReal;
        minImaginary = newMinImaginary;
        maxImaginary = newMaxImaginary;
    }

    // Revert to previous parameters
    public void undo() {
        if (!undoStack.isEmpty()) {
            Parameters prev = undoStack.pop();
            minReal = prev.minReal;
            maxReal = prev.maxReal;
            minImaginary = prev.minImaginary;
            maxImaginary = prev.maxImaginary;
        }
    }

    // Getter method for the calculated data (matrix of pixels)
    public int[][] getMandelbrotData() {
        return mandelbrotData;
    }

    // Getter method for max iterations (needed for colouring)
    public int getMaxIterations() {
        return maxIterations;
    }

    // Getter method for checking whether undo deque is empty
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    // Getter method for getMinReal
    public double getMinReal() {
        return minReal;
    }

    // Getter method for getMaxReal
    public double getMaxReal() {
        return maxReal;
    }

    // Getter method for getMinImaginary
    public double getMinImaginary() {
        return minImaginary;
    }

    // Getter method for getMaxImaginary
    public double getMaxImaginary() {
        return maxImaginary;
    }

}
package model;

public class MandelbrotModel {
    private MandelbrotCalculator mandelbrotCalculator;

    // INITIAL PARAMETERS
    // Defining the boundaries for the image (x=real, y=imaginary !!!verify this)
    private double minReal;
    private double maxReal;
    private double minImaginary;
    private double maxImaginary;
    // Number of times Mandelbrot iteration formula is applied to a complex number before its decided it will not escape
    // More iterations means more values will escape, which means a higher-resolution image
    private int maxIterations;
    // A matrix representing pixels in a grid, storing the number of iterations it took the value corresponding to each
    // pixel to escape (if it escaped at all)
    private int[][] mandelbrotData;

    // A constructor method for the mandelbrot set
    public MandelbrotModel() {
        mandelbrotCalculator = new MandelbrotCalculator();

        // Initialise parameters to default values
        minReal = MandelbrotCalculator.INITIAL_MIN_REAL;
        maxReal = MandelbrotCalculator.INITIAL_MAX_REAL;
        minImaginary = MandelbrotCalculator.INITIAL_MIN_IMAGINARY;
        maxImaginary = MandelbrotCalculator.INITIAL_MAX_IMAGINARY;
        maxIterations = MandelbrotCalculator.INITIAL_MAX_ITERATIONS;
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

    // Getter method for the calculated data (matrix of pixels)
    public int[][] getMandelbrotData() {
        return mandelbrotData;
    }

    // Getter method for max iterations (needed for colouring)
    public int getMaxIterations() {
        return maxIterations;
    }

}
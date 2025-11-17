import model.MandelbrotModel;
import delegate.MainFrame;

/*
 * Author: Konan-91
 *
 * Launches the Mandelbrot viewer application.
 * This class contains the main method and is the entry point for the program.
 */
public class Launcher {

    /**
     * Main entry point for the application.
     * Creates the MandelbrotModel (Model) and the MainFrame (View/Delegate).
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create model
        MandelbrotModel model = new MandelbrotModel();

        // Create delegate
        new MainFrame(model);
    }

}
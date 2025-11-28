import model.MandelbrotModel;
import delegate.MainFrame;

// TODO: Fix comments. Write tests. Write report.

/*
 * Launches the Mandelbrot viewer application.
 * This class is the entry point for the program.
 *
 * @author 250014506
 * @version 1
 * @since 1
 */
public class Launcher {

    /**
     * Main entry point for the application.
     * Creates the MandelbrotModel (Model) and the MainFrame (Delegate).
     */
    public static void main() {
        MandelbrotModel model = new MandelbrotModel();
        new MainFrame(model);
    }

}
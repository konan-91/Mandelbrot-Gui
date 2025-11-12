import model.MandelbrotModel;
import view.MainFrame;

/*
 * Author: konan-91
*/
public class Launcher {

    /*
     * Using Model-Delegate structure (there is no controller object so it's not MVC).
     */
    public static void main(String[] args) {
        // Create model
        MandelbrotModel model = new MandelbrotModel();

        // Create delegate (view & controller)
        MainFrame mainFrame = new MainFrame(); // Will be MainFrame(model); at some point
    }

}

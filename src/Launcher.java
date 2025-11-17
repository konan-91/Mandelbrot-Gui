import model.MandelbrotModel;
import delegate.MainFrame;

/*
 * Author: Konan-91
*/
public class Launcher {

    /*
     * Using Model-Delegate structure
     */
    public static void main(String[] args) {
        // Create model
        MandelbrotModel model = new MandelbrotModel();

        // Create delegate
        new MainFrame(model);
    }

}

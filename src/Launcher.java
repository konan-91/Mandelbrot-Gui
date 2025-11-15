import model.MandelbrotModel;
import view.MainFrame;

/*
 * Author: konan-91
*/
public class Launcher {

    /*
     * Using Model-Delegate structure (there is no controller object so it's not MVC - actually, verify if true).
     */
    public static void main(String[] args) {
        // Create model MandelbrotModel model = new MandelbrotModel();

        // Create delegate (view & controller)
        new MainFrame(); // may have to pass model: MainFrame(model)
    }

}

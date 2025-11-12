package view;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * This is the Delegate class, all other GUI elements sit within this scaffold.
*/
public class MainFrame extends JFrame {

    // Constructor, which initialises the GUI, starting the program
    public MainFrame() {
        JButton button = new JButton ("Press Me!");
        button.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println ("Yippee!");
            }
        });
        getContentPane ().add (button);
        setSize (200, 200);
        setVisible (true);
    }

}

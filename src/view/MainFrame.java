package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * This is the Delegate class, all other GUI elements sit within this scaffold.
*/
public class MainFrame extends JFrame {

    // Constructor, which initialises the GUI, starting the program
    public MainFrame() {
        // Set title and ensure program halts on exit
        setTitle("Mandelbrot Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Define clickable button
        JButton button = new JButton("Press Me!");
        button.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Yippee!");
            }
        });
        getContentPane().add(button, BorderLayout.CENTER);

        // Make window visible and adjust aspect ratio / size
        setSize(600, 600);
        setVisible(true);
    }

}

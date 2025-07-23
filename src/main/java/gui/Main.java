// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)
// July 23, 2025

package gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Main class for the Paid Time Off Calculator GUI.
 */
public class Main extends JFrame {

    /**
     * Main method to run the application.
     * 
     * @param args Unused command-line arguments.
     */
    public static void main(String[] args) {
        new Main();
    }

    /**
     * Constructor for the Main class.
     */
    public Main() {
        // Create the main panel with spacing
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add main panel to the main frame
        add(mainPanel);

        // Set the main frame properties
        setTitle("Paid Time Off Calculator GUI");
        setSize(1800, 900);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}

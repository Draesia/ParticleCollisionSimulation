package ramsden.ryan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ramsden.ryan.GUI.DisplayPanel;

@SuppressWarnings("serial")
public class KineticSim extends JFrame {
	
    public KineticSim() {
        this.setTitle("2D Collision Simulation");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(kineticModelPanel());
        this.setMinimumSize(new Dimension(Control.width, Control.height));
        this.pack();
        this.setVisible(true);
        this.setResizable(true);
        
        Control.manager.runGameLoop();
    }
   
    
    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() { public void run() { new KineticSim(); }});
        
    }
    
    public static JPanel kineticModelPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        Engine model = new Engine(Control.width, Control.height);
        panel.setMaximumSize(new Dimension(Control.width, Control.height));
        DisplayPanel view = new DisplayPanel(model);
        panel.add(view, BorderLayout.CENTER);
        Control.manager = new Control(view, model);
        return panel;
    }
}

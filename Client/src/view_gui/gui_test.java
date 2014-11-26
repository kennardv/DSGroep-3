package view_gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class gui_test extends JPanel {
	// override the paintComponent method
	// THE MAIN DEMO OF THIS EXAMPLE:

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Font f = new Font("Helvetica, SansSerif, Serif, Monospaced, Dialog, DialogInput", Font.PLAIN, 15);
		FontMetrics fm = g.getFontMetrics(f);
		int cx = 10;
		int cy = 10;
		g.setFont(f);
		g.drawString("Client GUI, ", cx, cy);
		cx += fm.stringWidth("Hello, ");
	} // paintComponent

	public static void main(String[] args) {
		JFrame f = new MyFrame ("Client GUI");
		f.show();
	} // main

} // class TextPanel

class MyFrame  extends JFrame {
	public MyFrame (String s) {
		// Frame Parameters
		Toolkit tk = Toolkit.getDefaultToolkit();  
		setTitle(s);
		int xSize = ((int) tk.getScreenSize().getWidth());  
		int ySize = ((int) tk.getScreenSize().getHeight());  
		setSize(xSize,ySize);  
		setLocation(0, 0); // default is 0,0 (top left corner)

		// Window Listeners
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // windowClosing
		}); // addWindowLister

		// Add Panels
		Container contentPane = getContentPane();
		contentPane.add(new gui_test());

	} // constructor MyFrame
} // class MyFrame

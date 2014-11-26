package view_gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;

public class Test_Builder {

	private JFrame frmClientGui;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test_Builder window = new Test_Builder();
					window.frmClientGui.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Test_Builder() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmClientGui = new JFrame();
		frmClientGui.setTitle("Client GUI");
		frmClientGui.setBounds(100, 100, 437, 475);
		frmClientGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClientGui.getContentPane().setLayout(null);
		
		JLabel lbl1 = new JLabel("Client gui");
		lbl1.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lbl1.setBounds(137, 11, 226, 65);
		frmClientGui.getContentPane().add(lbl1);
		
		String label[] = { "Zero", "One", "Two", "Three", "Four", "Five", "Six",
			      "Seven", "Eight", "Nine", "Ten", "Eleven" };
		String label2[] = { "Zero", "Eleven" };
		
		JList list1 = new JList(label);
		list1.setBounds(24, 87, 103, 192);
		frmClientGui.getContentPane().add(list1);

		
		JButton btnNewButton = new JButton("Download =>");
		btnNewButton.setBackground(Color.GREEN);
		btnNewButton.setBounds(137, 157, 108, 36);
		frmClientGui.getContentPane().add(btnNewButton);
		
		JLabel lblFilesInNetwork = new JLabel("Files in Network:");
		lblFilesInNetwork.setBounds(24, 62, 108, 14);
		frmClientGui.getContentPane().add(lblFilesInNetwork);
		
		JLabel lblMyFiles = new JLabel("My files:");
		lblMyFiles.setBounds(260, 62, 108, 14);
		frmClientGui.getContentPane().add(lblMyFiles);
		
		JList list2 = new JList(label2);
		list2.setBounds(260, 87, 103, 192);
		frmClientGui.getContentPane().add(list2);
		
		JButton btnShutdown = new JButton("Shutdown");
		btnShutdown.setBackground(Color.ORANGE);
		btnShutdown.setBounds(287, 376, 108, 36);
		frmClientGui.getContentPane().add(btnShutdown);
		

	}
}

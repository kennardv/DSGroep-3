package view_gui;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import javax.swing.JList;

public class client_gui {
	private DefaultListModel listModel;
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					client_gui window = new client_gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public client_gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 555, 507);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnLogOut = new JButton("Log out");
		btnLogOut.setBounds(440, 11, 89, 23);
		frame.getContentPane().add(btnLogOut);
		
		JButton btnNewButton = new JButton("OPEN");
		btnNewButton.setBounds(122, 70, 89, 23);
		frame.getContentPane().add(btnNewButton);
		
		JLabel lblFile = new JLabel("File 1");
		lblFile.setBounds(42, 74, 46, 14);
		frame.getContentPane().add(lblFile);
		
		JButton btnDelete = new JButton("DELETE");
		btnDelete.setBounds(236, 70, 89, 23);
		frame.getContentPane().add(btnDelete);
		
		JButton btnDeleteLocal = new JButton("DELETE LOCAL");
		btnDeleteLocal.setBounds(354, 70, 134, 23);
		frame.getContentPane().add(btnDeleteLocal);

		listModel = new DefaultListModel();
		listModel.addElement("Jane Doe");
		listModel.addElement("John Smith");
		listModel.addElement("Kathy Green");

		JList list = new JList(listModel);
		list.setBounds(42, 122, 239, 207);
		frame.getContentPane().add(list);
		
		

	}
}

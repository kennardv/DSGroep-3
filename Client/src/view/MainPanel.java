package view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.*;

public class MainPanel extends JFrame {
	
	private JTextField txtUserName = new JTextField("Username");
	private JButton btnStart = new JButton("Start");
	private JLabel lblConnectionStatus = new JLabel("Not connected");
    
    public MainPanel() {
    	JPanel mainPanel = new JPanel();
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.setSize(500,500);
    	
    	mainPanel.add(btnStart,BorderLayout.NORTH);
    	txtUserName.setSize(100, 60);
    	mainPanel.add(txtUserName, BorderLayout.CENTER);
    	mainPanel.add(lblConnectionStatus, BorderLayout.SOUTH);
    	
    	this.add(mainPanel);
    }
    
    public void addStartListener(ActionListener listenForStartButton) {
    	btnStart.addActionListener(listenForStartButton);
    }
    
    public void setConnectionStatus(String message) {
    	this.lblConnectionStatus.setText(message);
    }
    
    public String getUserName() {
    	return this.txtUserName.getText();
    }
}

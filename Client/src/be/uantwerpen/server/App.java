package be.uantwerpen.server;

import java.io.IOException;

import javax.swing.SwingUtilities;

import model.Client;
import controller.MainController;
import view.MainPanel;

public class App {

	public static void main(String[] args) {
		System.out.println("Starting App");
		MainPanel view = new MainPanel();
		Client model = null;
		try {
			model = new Client();
		} catch (ClassNotFoundException | InterruptedException | IOException e) {
			e.printStackTrace();
		}
		MainController controller = new MainController(model, view);
		
		view.setVisible(true);
	}

}

package be.uantwerpen.server;

import javax.xml.bind.annotation.*;


public class Files {

	private String[] files;
	
	public Files() {
		
	}
	
	public Files(String[] files) {
		this.setFiles(files);
	}
	
	public String[] getFiles() {
		return files;
	}

	public void setFiles(String[] files) {
		this.files = files;
	}
	
}

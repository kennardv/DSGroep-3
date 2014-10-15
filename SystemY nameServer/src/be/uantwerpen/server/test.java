package be.uantwerpen.server;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] filenames = {"Java", "Scala", "C++", "Ruby", "Python", "Perl"};
    	XMLParser parser = new XMLParser("joske", "192.168.1.1",filenames );
    	parser.main(null);
    	
	}

}

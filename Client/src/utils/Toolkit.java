package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Toolkit {
	
	/**
     * List all the files under a directory
     * @param directoryName to be listed - RELATIVE PATH
     */
    public static List<File> listFilesInDir(String directoryName){
    	String path = null;
		path = new File(directoryName).getAbsolutePath();
    	File[] f = new File(path).listFiles();
    	List<File> files = new ArrayList<File>();
    	for (int i = 0; i < f.length; i++) {
    		if (f[i].isFile()) {
    			files.add(f[i]);
			}
    	}
        return files;
    }
    
    /**
     * Helper function to convert the contents of a file to a byte array
     * @param path
     * path to the file
     * @return bFile
     * byte array of the contents of the file
     */
    public static byte[] fileToByteArr(File f) {
    	FileInputStream fis = null;
    	 
        File file = f;
 
        byte[] bFile = new byte[(int) file.length()];
 
        try {
            //convert file into array of bytes
		    fis = new FileInputStream(file);
		    fis.read(bFile);
		    fis.close();
		    
		    System.out.println("Contents of byte array.");
		    for (int i = 0; i < bFile.length; i++) {
		    	System.out.print((char)bFile[i]);
            }
 
		    System.out.println("Done");
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        return bFile;
    }
    
    /**
     * Helper method to convert a string to a hash. Range goes from 0 to 32768.
     * @param name
     * String to be hashed
     * @return Returns the hashed inputted string.
     */
    public static int hashString(String name) {
		return Math.abs(name.hashCode()) % 32768; // berekening van de hash
	}
    
    /**
     * Create a correctly formated location string
     * @param name
     * @param suffix
     * "ntn" for example
     * @return formated location string
     */
    public static String createBindLocation(String name, String suffix) {
    	return "//" + name + "/" + suffix;
    }
    
    public static byte[] intToBytes(int my_int) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeInt(my_int);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();
        return int_bytes;
    }
    
    /**
     * Helper function to convert a byte array to an object
     * @param b
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object byteArrToObject(byte[] b) throws IOException, ClassNotFoundException {
    	ByteArrayInputStream bis = new ByteArrayInputStream(b);
    	ObjectInput in = null;
    	Object obj = null;
    	try {
    	  in = new ObjectInputStream(bis);
    	  obj = in.readObject(); 
    	} finally {
    	  try {
    	    bis.close();
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	  try {
    	    if (in != null) {
    	      in.close();
    	    }
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	}
    	return obj;
    }
    
    /***
     * Helper function to convert an object to a byte array
     * @param path
     * path to the file
     * @return bFile
     * byte array of the contents of the file
     * @throws IOException 
     */
    public static byte[] objectToByteArr(Object obj) throws IOException {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutput out = null;
    	byte[] b = null;
    	try {
    	  out = new ObjectOutputStream(bos);   
    	  out.writeObject(obj);
    	  b = bos.toByteArray();
    	} finally {
    	  try {
    	    if (out != null) {
    	      out.close();
    	    }
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	  try {
    	    bos.close();
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	}
    	return b;
    }
}

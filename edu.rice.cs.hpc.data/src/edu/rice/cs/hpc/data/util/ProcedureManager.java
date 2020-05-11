package edu.rice.cs.hpc.data.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/****
 * 
 * class to manage maps of procedure names
 *
 */
public class ProcedureManager<ValueType> {

	
	protected HashMap<String, ValueType> map;
	
	/***
	 * open the 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public ProcedureManager(Class<?> location, final String file) throws IOException, ClassNotFoundException {
		map = new HashMap<String, ValueType>();
				
		final InputStream is = location.getResourceAsStream(file);
		if (is != null) {
			addToMap(is);
			is.close();
		}
	}
	
	/**
	 * @param filename : absolute file path
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public void open(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(filename);
		addToMap(fis);
		fis.close();
	}

	public void save(String filename) throws IOException {
		final FileOutputStream fos = new FileOutputStream(filename);
		final ObjectOutputStream oos = new ObjectOutputStream( fos );
		oos.writeObject(map);
		oos.close();
	}
	
	/***
	 * add to the map the list of pair <proc, val> from a file
	 * 
	 * @param is
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void addToMap(InputStream is) throws IOException, ClassNotFoundException {
		final ObjectInputStream in = new ObjectInputStream(is);
		Object o = in.readObject();
		
		if (o instanceof HashMap<?,?>) {
			@SuppressWarnings("unchecked")
			final HashMap<String, ValueType> mapin = (HashMap<String, ValueType>) o;
			map.putAll(mapin);		
		} else {
			// the file is not the type of hash map file
			throw new RuntimeException("Incorrect file format " );
		}
		in.close();
	}

	/***
	 * return the value of the procedure
	 * @param proc
	 * @return
	 */
	protected ValueType get(String proc) {
		return this.map.get(proc);
	}

	/***
	 * strore the pair procedure and its value
	 * @param proc
	 * @param val
	 */
	protected void put(String proc, ValueType val) {
		map.put(proc, val);
	}
	
}

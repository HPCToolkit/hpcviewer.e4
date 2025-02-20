// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.util.IUserData;

/***
 * 
 * Mapping a name to another alias
 * This is useful when we want to change a name of a procedure X to Y (for display only) 
 *
 */
public abstract class AliasMap<K,V> implements IUserData<K, V> 
{
	
	protected HashMap<K, V> data;
	
	
	/***
	 * read map or create file if it doesn't exist
	 */
	protected AliasMap() {

	}
		
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpcdata.util.IUserData#get(java.lang.String)
	 */
	public V get(K key) {
		
		checkData();
		return data.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpcdata.util.IUserData#put(java.lang.String, java.lang.String)
	 */
	public void put(K key, V val) {
		
		checkData();
		data.put(key, val);
	}

	/***
	 * Removes the mapping for the specified key from this map if present.
	 * @param key
	 * @return the value if the key exists
	 */
	public V remove(K key) {
		checkData();
		return  data.remove(key);
	}

	/****
	 * Removes all of the mappings from this map. The map will be empty after this call 
	 */
	public void clear() {
		if (data != null)
			data.clear();
	}

	/***
	 * clear and remove resources
	 */
	public void dispose() {
		clear();
		data = null;
	}
	
	/***
	 * save the changes permanently into the storage
	 * Note: once it is stored, there is no way to restore back
	 */
	public void save() {
		
		final String filename = getFilename();
		final File file = new File(filename);

		try (var out = new ObjectOutputStream( new FileOutputStream(file.getAbsoluteFile()) )) {			
			out.writeObject(data);
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error(filename + ": fail to write the file", e);
		} 
	}	
	
	/***
	 * check if we have loaded the data or not. If not, we need to read the file,
	 * and initialize the data. If the file doesn't exist, it will be created and
	 * initialized by the default data (implemented by the children)
	 */
	protected void checkData() {
		if (data == null) {
			data = new HashMap<>();
			final String filename = getFilename();
			File file = new File( filename );
			
			if (file.canRead()) {
				// old format, we need to remove the file
				if (! readData(file.getAbsolutePath())) {
					deleteFile(filename);
					initDefault();
					readData(file.getAbsolutePath());
				}
			} else if (!file.exists()) {
				// file doesn't exist, but we can create
				//
				// fill the data with the default values
				initDefault();
				
				save();
			}
		}
	}

	/***
	 * read data from a given file
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private boolean readData(String filename) {
		boolean result = false;

		try (var in = new ObjectInputStream(new FileInputStream(filename))) {
			Object o = in.readObject();
			if (o instanceof HashMap<?,?> map) {
				// issue #117: check the correctness of the data				
				for(var entry: map.entrySet()) {
					if (!checkData((Entry<K, V>) entry))
						return false;
				}
				data = (HashMap<K, V>) map;
				result =  true;
			}
		} catch (FileNotFoundException e) {
			LoggerFactory.getLogger(getClass()).error(filename + ": file doesn't exist.", e);
		} catch (IOException | ClassNotFoundException e) {
			LoggerFactory.getLogger(getClass()).error(filename + ": corrupt or incompatible file. It will be removed.", e);
			deleteFile(filename);
		} 
		return result;
	}
	

	/***
	 * Check if the entry from the file is correct or not.
	 * 
	 * @apiNote I don't know why using instance of is always correct in this case
	 * 
	 * @param entry
	 * @return
	 */
	protected boolean checkData(Entry<K, V> entry) {
		// damn Java, this will always return true
		// ask the implementer to correct this
		return (entry.getKey() instanceof K &&
				entry.getValue() instanceof V);
	}

	
	private void deleteFile(String filename) {
		try {
			Files.delete(Path.of(filename));
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Fail to remove the file: "+ filename, e);
		}
	}
	
	public abstract String getFilename();
	public abstract void initDefault();

}

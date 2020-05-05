package edu.rice.cs.hpc.data.util;

/***
 * 
 * interface to retrieve or set user data
 * 
 * methods to implement:
 * - get: to retrieve a data
 * - put: to store a data
 *
 */
public interface IUserData<K,V> {

	/***
	 * get the value of the key
	 * if the key doesn't exist, it return null
	 * 
	 * @param key
	 * @return
	 */
	public V get(K key);
	
	/***
	 * store the pair key, value
	 * key cannot be null, and has to be unique
	 * 
	 * @param key
	 * @param val
	 */
	public void put(K key, V val);
	
}

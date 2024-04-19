package edu.rice.cs.hpcremote.data;


/****
 * Interface to store the content of a remote directory.
 * 
 */
public interface IRemoteDirectoryContent 
{
	/***
	 * Get the path of the current directory
	 * 
	 * @return {@code String}
	 */
	String getDirectory();
	
	
	/***
	 * Get the array of the content of this directory.
	 * @return
	 */
	IFileContent[] getContent();
	
	
	/****
	 * Interface representation of the content of this directory.
	 * 
	 */
	public interface IFileContent
	{
		/**
		 * Get the name of the file or directory
		 * @return
		 */
		String getName();
		
		/***
		 * Check if this is a directory
		 * @return
		 */
		boolean isDirectory();
		
		/***
		 * Check if this is a database directory.
		 * @return
		 */
		boolean isDatabase();
	}
}

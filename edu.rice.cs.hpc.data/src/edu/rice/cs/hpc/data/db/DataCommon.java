package edu.rice.cs.hpc.data.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/***************************************************
 * 
 * Class to read and store common header of a database
 * This class only works with hpcprof format version 3.0 
 *
 * Header format:
 * - 18 bytes magic 
 * - 1  byte  version
 * - 4  bytes number of items
 * 
 ***************************************************/
abstract public class DataCommon 
{
	private final static int HEADER_COMMON_SIZE = 23;
	private final static int MESSAGE_SIZE 		= 18;
	
	protected long numItems;
	protected int  version;
	protected String filename;
	
	private FileInputStream fis;
	private FileChannel channel;
	
	/*******
	 * Open a file and read the first HEADER_COMMON_SIZE bytes
	 * 
	 * @param file : a string of the filename
	 * @throws IOException
	 */
	public void open(final String file) 
			throws IOException
	{
		filename = file;
		
		fis = new FileInputStream(filename);
		channel  = fis.getChannel();
		
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_COMMON_SIZE);
		int numBytes      = channel.read(buffer);
		if (numBytes > 0) 
		{
			// read the common header
			readHeader(file, fis, buffer);
			
			channel.position(HEADER_COMMON_SIZE);
			// -------------------------------------------------------------
			// Read the next header (implemented by derived class)
			// -------------------------------------------------------------
			if ( readNextHeader(channel) ) 
			{
				// the implementer can perform other operations
			}
		}
	}
	
	/******
	 * print out the information of the content of the file
	 * @param out : the output stream (can be the standard output)
	 */
	public void printInfo( PrintStream out)
	{
		out.println("Filename: "  + filename);
		out.println("Version: "   + version);
		out.println("Num items: " + numItems);
	}
	
	/***
	 * Free the allocated resources. To be called by the caller at the end.
	 * @throws IOException 
	 */
	public void dispose() throws IOException
	{
		channel.close();
		fis.close();
	}
	
	
	protected FileChannel getChannel() {
		return channel;
	}
	
	/******
	 * Default implementation to read the first HEADER_COMMON_SIZE bytes
	 * 
	 * @param file 	: filename
	 * @param fis  	: input file stread (to be closed in case of exception) 
	 * @param buffer : the current buffer containing the first 256 bytes
	 * 
	 * @throws IOException
	 */
	protected void readHeader(String file, FileInputStream fis, ByteBuffer buffer) 
			throws IOException
	{
		// -------------------------------------------------------------
		// get the message header file
		// -------------------------------------------------------------
		final byte []bytes = new byte[MESSAGE_SIZE]; 
		buffer.rewind();
		buffer.get(bytes, 0, MESSAGE_SIZE);
		String header = new String(bytes);
		if (!isFileHeaderCorrect(header)) {
			String msg = "Incorrect header: " + header;
			throw new IOException(msg);
		}
		
		// -------------------------------------------------------------
		// read the version
		// -------------------------------------------------------------
		version = buffer.get();

		// -------------------------------------------------------------
		// read the number of items
		// -------------------------------------------------------------
		final byte []itemBytes = new byte[4];
		buffer.get(itemBytes, 0, 4);
		ByteBuffer byteBuffer = ByteBuffer.wrap(itemBytes);
		numItems = byteBuffer.getInt();
	}
	
	protected abstract boolean isTypeFormatCorrect(long type);
	protected abstract boolean isFileHeaderCorrect(String header);
	protected abstract boolean readNextHeader(FileChannel input) throws IOException;
}

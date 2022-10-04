package edu.rice.cs.hpcdata.db.version4;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
	public  final static int HEADER_COMMON_SIZE = 16;
	private final static int MESSAGE_SIZE 		= 14;
	
	protected byte versionMajor;
	protected byte versionMinor;
	
	protected DataSection []sections;
	
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
		
		var size = channel.size();
		
		ByteBuffer footerBuff = ByteBuffer.allocate(8);
		int res = channel.read(footerBuff, size - 8);
		if (res < 8)
			throw new IOException(file);
		

		String footer = new String(footerBuff.array());
		if (!isFileFooterCorrect(footer))
			throw new IOException(file + ": File footer is invalid\n" + footerBuff.toString());
		
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_COMMON_SIZE);
		int numBytes      = channel.read(buffer);
		if (numBytes > 0) 
		{
			// read the common header
			readHeader(file, fis, buffer);
			
			// read the section headers
			int numSections = getNumSections();
			buffer.clear();
			buffer = ByteBuffer.allocate(numSections * 16);
			channel.position(HEADER_COMMON_SIZE);
			numBytes = channel.read(buffer);
			buffer.rewind();
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			
			sections = new DataSection[numSections];
			for(int i=0; i<numSections; i++) {
				sections[i] = new DataSection();
				sections[i].size   = buffer.getLong();
				sections[i].offset = buffer.getLong();
			}
			
			// -------------------------------------------------------------
			// Read the next header (implemented by derived class)
			// -------------------------------------------------------------
			if ( readNextHeader(channel, sections) ) 
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
		out.println("Version: "   + versionMajor);
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
			String msg = file + " has incorrect header: " + header;
			throw new IOException(msg);
		}
		
		// -------------------------------------------------------------
		// read the version
		// -------------------------------------------------------------
		versionMajor = buffer.get();
		versionMinor = buffer.get();
	}
	
	protected static long getMultiplyOf(long v, int mask) {
		return (v + mask) & ~mask;
	}
	
	protected static long getMultiplyOf8(long v) {
		return getMultiplyOf(v, 7);
	}
	
	
	protected abstract int getNumSections();
	protected abstract boolean isFileHeaderCorrect(String header);
	protected abstract boolean isFileFooterCorrect(String header);
	protected abstract boolean readNextHeader(FileChannel input, DataSection []sections) throws IOException;
}

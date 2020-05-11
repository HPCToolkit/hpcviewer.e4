package edu.rice.cs.hpc.data.util;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**The idea for this class is credited to Stu Thompson.
 * stackoverflow.com/questions/736556/binary-search-in-a-sorted-memory-mapped-file-in-java
 * 
 * The implementation is credited to
 * @author Michael
 * @author Reed
 */
public class LargeByteBuffer
{
	
	/**The masterBuffer holds a vector of all bytebuffers*/
	private MappedByteBuffer[] masterBuffer;
	
	private long length;
	
	final private FileChannel fcInput;
	
	// The page size has to be the multiple of record size (24) and header size (can be 24 or 32)
	// originally: Integer.MAX_VALUE;
	private long pageSize; 
	
	/***
	 * Construct direct/indirect mapped byte buffer
	 * 
	 * @param in : file channel
	 * @param headerSz : the size of the header file
	 * 
	 * @throws IOException
	 */
	public LargeByteBuffer(FileChannel in, int headerSz, int recordSz)
		throws IOException
	{
		fcInput = in;
		length = in.size();
		
		// --------------------------------------------------------------------
		// compute the page size:
		// the page size has to be multiple of the least common multiple of 
		//	the header size and the record size
		// --------------------------------------------------------------------
		pageSize = lcm (headerSz, recordSz) * (1 << 20);
		
		int numPages = 1+(int) (in.size() / pageSize);
		masterBuffer = new MappedByteBuffer[numPages];		
	}
	
	private long getCurrentSize(long index) throws IOException 
	{
		long currentSize = pageSize;
		if (length/pageSize == index) {
			currentSize = length - (index * pageSize);
		}
		return currentSize;
	}
	
	/****
	 * Set a buffer map to a specified page
	 *  
	 * @param page
	 * @return
	 * @throws IOException
	 */
	private MappedByteBuffer setBuffer(int page) throws IOException
	{
		long start = page * pageSize;
		MappedByteBuffer buffer = fcInput.map(FileChannel.MapMode.READ_ONLY, start, 
				getCurrentSize(page));
		masterBuffer[page] = buffer;
		
		return buffer;
	}
	
	private MappedByteBuffer getBuffer(int page) throws IOException
	{
		MappedByteBuffer buffer = masterBuffer[page];
		if (buffer == null) {
			buffer = setBuffer(page);
		}
		return buffer;
	}
	
	public byte get(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		
		return getBuffer(page).get(loc);
	}
	
	public int getInt(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		return getBuffer(page).getInt(loc);
	}
	
	public long getLong(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		if (loc < pageSize - 4) {
			return getBuffer(page).getLong(loc);
		} else {
			return (((long) getBuffer(page).getInt(loc)) << 32) + getBuffer(page+1).getInt(0);
		}
	}
	
	public double getDouble(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		if (loc < pageSize - 4) {
			return getBuffer(page).getDouble(loc);
		} else {
			return (double)(((long) getBuffer(page).getInt(loc)) << 32) + (long) getBuffer(page+1).getInt(0);
		}
	}
	
	public char getChar(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		return getBuffer(page).getChar(loc);
	}

	public String getString(long position, long length) throws IOException
	{
		String str = "";
		for (long i = 0; i < length; ++i) {
			char c = (char)get(position + i);
			str += c;
		}
		return str;
	}

	public float getFloat(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		return getBuffer(page).getFloat(loc);
	}
	
	public short getShort(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		return getBuffer(page).getShort(loc);
	}
	
	public long size()
	{
		return length;
	}
	
	/***
	 * Disposing resources manually to avoid memory leak
	 */
	public void dispose() 
	{
		masterBuffer = null;
		try {
			fcInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/***
	 * compute the gcd of two numbers
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static long gcd(long a, long b)
	{
	    while (b > 0)
	    {
	        long temp = b;
	        b = a % b; 
	        a = temp;
	    }
	    return a;
	}
	
	/****
	 * compute the lcm of two numbers
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static long lcm(long a, long b)
	{
	    return a * (b / gcd(a, b));
	}
}
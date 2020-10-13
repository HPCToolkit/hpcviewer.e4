package edu.rice.cs.hpc.data.util;

import java.io.IOException;
import java.nio.ByteBuffer;
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
		long remainder = pageSize - loc;

		if (remainder >= Integer.BYTES) {
			return getBuffer(page).getInt(loc);
		}
		ByteBuffer bb = combineBytes(page, loc, Integer.BYTES);
		return bb.getInt();
	}
	
	public long getLong(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);

		long remainder = pageSize - loc;
		if (remainder >= Long.BYTES) {
			return getBuffer(page).getLong(loc);
		}
		ByteBuffer bb = combineBytes(page, loc, Long.BYTES);
		long result = bb.getLong();
		
		return result;
	}
	
	
	/****
	 * This method combines last bytes of the "first" page and the first bytes of the "second" page.
	 * In some cases, we need to read more bytes than the allocated bytes of a page. 
	 * For example the number of bytes in a page is 1024, and at location 1022 we want to read 
	 * 8 bytes. This causes we need to read 2 bytes in the current page, and another 6 bytes in the next
	 * page.
	 * 
	 * @param page int the current page
	 * @param loc long the current location
	 * @param numBytes int the number of bytes we need to read
	 * @return {@code ByteBuffer} byte buffer of the read bytes. The size is equal to numBytes.
	 * @throws IOException
	 */
	private ByteBuffer combineBytes(int page, int loc, int numBytes) throws IOException {
		long remainder = pageSize - loc;

		// graph the higher bytes
		byte []byteLeft = new byte[(int) remainder];
		for (int i=0; i<remainder; i++) {
			byteLeft[i] = getBuffer(page).get(loc+i);
		}

		// grab the lower bytes
		int remRight = (int) (numBytes - remainder);
		byte []byteRight = new byte[remRight];
		getBuffer(page+1).get(byteRight);

		// combine the higher and lower bytes, convert them to a long
		
		assert(byteLeft.length + byteLeft.length == numBytes);
		
		byte []resultByte = new byte[Long.BYTES];
		for (int i=0; i<byteLeft.length; i++) {
			resultByte[i] = byteLeft[i];
		}
		for (int i=0; i<byteRight.length; i++) {
			resultByte[i+byteLeft.length] = byteRight[i];
		}
		
		return ByteBuffer.wrap(resultByte);
	}
	
	
	public double getDouble(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);

		long remainder = pageSize - loc; 
		if (remainder >= Double.BYTES) {
			return getBuffer(page).getDouble(loc);
		}
		ByteBuffer bb = combineBytes(page, loc, Double.BYTES);
		double result = bb.getDouble();
		return result;
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
		
		long remainder = pageSize - loc; 
		if (remainder >= Float.BYTES) {
			return getBuffer(page).getFloat(loc);
		}
		ByteBuffer bb = combineBytes(page, loc, Float.BYTES);
		float result = bb.getFloat();
		return result;
	}
	
	public short getShort(long position) throws IOException
	{
		int page = (int) (position / pageSize);
		int loc = (int) (position % pageSize);
		
		long remainder = pageSize - loc; 
		if (remainder >= Short.BYTES) {
			return getBuffer(page).getShort(loc);
		}
		ByteBuffer bb = combineBytes(page, loc, Float.BYTES);
		short result = bb.getShort();
		return result;
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
package edu.rice.cs.hpcremote.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;

import edu.rice.cs.hpctraceviewer.data.ImageTraceAttributes;
import edu.rice.cs.hpcdata.db.version3.DataRecord;
import edu.rice.cs.hpcdata.util.CallPath;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpctraceviewer.data.util.Constants;


//Perhaps this would all be more suited to a ThreadPool 

/*
 * Philip 5/29/13 Moved rendering code to the canvases to align the remote
 * version with changes made to the local version. This used to be responsible
 * for rendering and decompressing, but now is not. I'm keeping the WorkItemToDo
 * structure just in case this expands again. 
 * Philip 7/23/13 Got rid of WorkItemToDo. This thread looks like it's only going
 * to be for decompression now that we have a decent way to do the rendering in
 * parallel without waiting for all threads to be decompressed.
 */

public class DecompressionThread extends Thread {

	final private ConcurrentLinkedQueue<DecompressionItemToDo> workToDo;
	final private ConcurrentLinkedQueue<Integer> timelinesAvailableForRendering;

	// Variables for decompression

	final Map<Integer, CallPath> scopeMap;

	final ImageTraceAttributes attributes;
	
	private final IThreadListener listener;
	
	public final static int COMPRESSION_TYPE_MASK = 0xFFFF;//Save two bytes for formatting versions
	public final static short ZLIB_COMPRESSSED  = 1;
	
	static boolean first = true;

	final private AtomicInteger ranksRemainingToDecompress;

	/********
	 * Constructor for decompression thread. 
	 * Despite its name, this class is not for decompressing data from the server,
	 * but mainly for accepting trace data. If the data is compressed, it will then
	 * automatically decompress.
	 *  
	 * @param map
	 * @param attributes
	 * @param queue work to do
	 * @param timelinesAvailableForRendering
	 * @param ranksRemainingToDecompress
	 * @param listener
	 */
	public DecompressionThread(
			Map<Integer, CallPath> map,
			ImageTraceAttributes attributes,
			ConcurrentLinkedQueue<DecompressionItemToDo> workToDo, 
			ConcurrentLinkedQueue<Integer> timelinesAvailableForRendering,
			AtomicInteger ranksRemainingToDecompress,
			IThreadListener listener) {

		scopeMap 		= map;

		this.attributes = attributes;
		this.workToDo   = workToDo;
		this.timelinesAvailableForRendering = timelinesAvailableForRendering;
		this.ranksRemainingToDecompress     = ranksRemainingToDecompress;
		this.listener 	= listener;
	}
	
	
	
	/**
	 * @return The index of a {@link ProcessTimeline} that has been uncompressed
	 * and is now ready for rendering. Returns null if there aren't any yet for
	 * any reason. The unavailability may be a temporary condition that will be
	 * resolved in a moment, or it may mean that all available timelines have 
	 * been processed. Unless this method returns null, it will not return a
	 * value that it has returned before.
	 */
/*	public static Integer getNextTimelineToRender() {
		return timelinesAvailableForRendering.poll();
	}*/
	
	@Override
	public void run() {
		int i = 0;
		while (ranksRemainingToDecompress.get() > 0)
		{
			DecompressionItemToDo wi = workToDo.poll();
			if (wi == null)
			{
				if ( i++ > RemoteDataRetriever.getTimeOut() ) {
					// time out
					break;
				}
				//There is still work that needs to get done, but it is not available to be worked on at the moment.
				//Wait a little and try again
				try {
					Thread.sleep( RemoteDataRetriever.getTimeSleep() );

				} catch (InterruptedException e) {
					// error in I/O
					e.printStackTrace();
					break;
				}
			} else {
				i = 0;
				if (first){
					first = false;
				}
				ranksRemainingToDecompress.getAndDecrement();
				DecompressionItemToDo toDecomp = (DecompressionItemToDo)wi;
				try {
					decompress(toDecomp);
				} catch (IOException e) {
					// error in decompression
					e.printStackTrace();
					break;
				}
			}
		}
		if (ranksRemainingToDecompress.get() > 0) {
			listener.notify("Decompression error due to time out");
		}
	}

	private void decompress(DecompressionItemToDo toDecomp) throws IOException
	{
		//	DataRecord[] ranksData = readTimeCPIDArray(toDecomp.packet, toDecomp.itemCount, toDecomp.startTime, toDecomp.endTime, toDecomp.compressed);
		//TraceDataByRank dataAsTraceDBR = new TraceDataByRank(ranksData);

		int lineNumber = toDecomp.rankNumber;

		// laks attempts to fix, 2015.02.06: I think the process time line class expect 
		// the number of horizontal pixels in the 4th parameter instead of number of processors
		// TODO: need to check
		
		//ProcessTimeline ptl = new ProcessTimeline(dataAsTraceDBR, scopeMap, lineNumber, 
		//		attributes.getPixelHorizontal(), attributes.getTimeInterval(), attributes.getTimeBegin());
		
		//timelineServ.setProcessTimeline(lineNumber, ptl);
		timelinesAvailableForRendering.add(lineNumber);
	}

	/**
	 * Reads from the stream and creates an array of Timestamp-CPID pairs containing the data for this rank
	 * @param packedTraceLine 
	 * @param length The number of Timestamp-CPID pairs in this rank (not the length in bytes)
	 * @param t0 The start time
	 * @param tn The end time
	 * @param compressed 
	 * @return The array of data for this rank
	 * @throws IOException
	 */
	private DataRecord[] readTimeCPIDArray(byte[] packedTraceLine, int length, long t0, long tn, int compressed) throws IOException {

		DataInputStream decompressor;
		if ((compressed & COMPRESSION_TYPE_MASK) == ZLIB_COMPRESSSED)
			decompressor= new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(packedTraceLine)));
		else
			decompressor = new DataInputStream(new ByteArrayInputStream(packedTraceLine));
		DataRecord[] toReturn = new DataRecord[length];
		long currentTime = t0;
		for (int i = 0; i < toReturn.length; i++) {
			// There are more efficient ways to send the timestamps. Namely,
			// instead of sending t_n - t_(n-1), we can send (t_n - t_(n-1))-T,
			// where T is the expected delta T, calculated by
			// (t_n-t_0)/(length-1). These will fit in three bytes for certain
			// and often will fit in two. Because of the gzip layer on top,
			// though, the actual savings may be marginal, which is why it is
			// implemented more simply right now. This is left as a possible
			// extension with the compression type flag.
			int deltaT = decompressor.readInt();
			currentTime += deltaT;
			int CPID = decompressor.readInt();
			/*if (CPID <= 0)
				System.out.println("CPID too small");*/
			toReturn[i] = new DataRecord(currentTime, CPID, Constants.dataIdxNULL);
		}
		decompressor.close();
		return toReturn;
	}




public static class DecompressionItemToDo {
	final byte[] packet;
	final int itemCount;//The number of Time-CPID pairs
	final long startTime, endTime;
	final int rankNumber;
	final int compressed;
	public DecompressionItemToDo(byte[] _packet, int _itemCount, long _startTime, long _endTime, int _rankNumber, int _compressionType) {
		packet = _packet;
		itemCount = _itemCount;
		startTime = _startTime;
		endTime = _endTime;
		rankNumber = _rankNumber;
		compressed = _compressionType;
	}
}
}

package edu.rice.cs.hpcremote.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.util.CallPath;
import edu.rice.cs.hpcremote.data.DecompressionThread.DecompressionItemToDo;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.util.Constants;


/**
 * Handles communication with the remote server, including asking for data and
 * parsing data, but not opening the connection or closing the connection. It
 * assumes the connection has already been opened by RemoteDBOpener and can be
 * retrieved from SpaceTimeDataControllerRemote.
 * See protocol documentation at the end of this file.
 * 
 * @author Philip Taffet
 * 
 */
public class RemoteDataRetriever {
	
	// -------------------------------------
	// Constants
	// -------------------------------------
	
	//For more information on message structure, see protocol documentation at the end of this file. 
	private static final int DATA = 0x44415441;
	private static final int HERE = 0x48455245;
	
	/****
	 * time out counter is based on TIME_SLEEP ms unit
	 */
	private static final int TIME_OUT = 2000;
	
	private static final int TIME_SLEEP = 50;
	
	// -------------------------------------
	// Variables
	// -------------------------------------

	private final Socket socket;
	DataInputStream receiver;
	BufferedInputStream rcvBacking;
	DataOutputStream sender;
	
	final int compressionType;

	/******
	 * Constructor for communicating with remote data server
	 * 
	 * @param _serverConnection : connection socket
	 * @param _statusMgr : line manager
	 * @param _shell : window shell
	 * @param _compressionType : type of compression, see {@link DecompressionThread.COMPRESSION_TYPE_MASK}
	 * 
	 * @throws IOException
	 */
	public RemoteDataRetriever(Socket _serverConnection, Shell _shell, int _compressionType) throws IOException {
		socket = _serverConnection;
		
		compressionType = _compressionType;
		
		rcvBacking 		= new BufferedInputStream(socket.getInputStream());
		receiver 		= new DataInputStream(rcvBacking);
		
		sender 			= new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	//TODO: I think these are all inclusive, but check.
	/**
	 * Issues a command to the remote server for the data requested, and waits for a response.
	 * @param P0 The lower bound of the ranks to get
	 * @param Pn The upper bound of the ranks to get
	 * @param t0 The lower bound for the time to get
	 * @param tn The upper bound for the time to get
	 * @param vertRes The number of pixels in the vertical direction (process axis). This is used to compute a stride so that not every rank is included
	 * @param horizRes The number of pixels in the horizontal direction (time axis). This is used to compute a delta_t that controls how many samples are returned per rank
	 * @return
	 * @throws IOException 
	 */
	public void getData( TraceDisplayAttribute attributes, 
			Map<Integer, CallPath> map, 
			final ConcurrentLinkedQueue<DecompressionItemToDo> workToDo) throws IOException
	{
		//Make the call
		//Check to make sure the server is sending back data
		//Wait/Receive/Parse:
				//			Make into TimeCPID[]
				//			Make into DataByRank
				//			Make into ProcessTimeline
				//			Put into appropriate place in array
		//When all are done, return the array
		// int P0, int Pn, long t0, long tn, int vertRes, int horizRes,
		int P0 = attributes.getProcessBegin();
		int Pn = attributes.getProcessEnd();
		long t0 = attributes.getTimeBegin();
		long tn = attributes.getTimeEnd();
		int vertRes = attributes.getPixelVertical();
		int horizRes = attributes.getPixelHorizontal();
		
		requestData(P0, Pn, t0, tn, vertRes, horizRes);
		
		int responseCommand = waitAndReadInt(receiver);		
		if (responseCommand != HERE)//"HERE" in ASCII
			throw new IOException("The server did not send back data");
	
		final int ranksExpected = Math.min(Pn-P0, vertRes);

		Job unpacker = new Job("Receiving data") {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				DataInputStream dataReader;
				int ranksReceived = 0;
				dataReader = receiver;
				boolean first = true;
				
				try {
					while (ranksReceived < ranksExpected) {

						int rankNumber = dataReader.readInt();
						if (first){
							first = false;
						}
						int length = dataReader.readInt();// Number of CPID's

						long startTimeForThisTimeline = dataReader.readLong();
						long endTimeForThisTimeline = dataReader.readLong();
						int compressedSize = dataReader.readInt();
						
						// when there's network issue, the value of compressedSize can be negative
						// this has happened when the server process was suspended and the user keeps
						//	asking data from the client to the server
						if (compressedSize >0) {
							byte[] compressedTraceLine = new byte[compressedSize];

							int numRead = 0;
							while (numRead < compressedSize) {
								numRead += dataReader.read(compressedTraceLine,
										numRead, compressedSize - numRead);

							}

							workToDo.add(
								new DecompressionThread.DecompressionItemToDo(
											compressedTraceLine, length,
											startTimeForThisTimeline,
											endTimeForThisTimeline, rankNumber,
											compressionType));

							ranksReceived++;
							monitor.worked(1);
						}
					}
				} catch (IOException e) {
					//Should we provide some UI notification to the user?
					e.printStackTrace();
				}
				monitor.done();
				//Updating the progress doesn't work anyways and will throw
				//an exception because this is a different thread
				//monitor.endProgress();

				return Status.OK_STATUS;
			}
		};
		unpacker.setUser(true);
		unpacker.schedule();
		
	}

	
	private void requestData(int P0, int Pn, long t0, long tn, int vertRes,
			int horizRes) throws IOException {
		sender.writeInt(DATA);
		sender.writeInt(P0);
		sender.writeInt(Pn);
		sender.writeLong(t0);
		sender.writeLong(tn);
		sender.writeInt(vertRes);
		sender.writeInt(horizRes);
		//That's it for the message
		sender.flush();
	}


	static int waitAndReadInt(DataInputStream receiver)
			throws IOException {
		int nextCommand;
		int timeout = 0;
		// Sometime the buffer is filled with 0s for some reason. This flushes
		// them out. This is awful, but otherwise we just get 0s

		while (receiver.available() <= 4
				|| ((nextCommand = receiver.readInt()) == 0)) {

			if (timeout++ > TIME_OUT) {
				throw new IOException("Timeout: no response from the server.");
			}
			try {
				Thread.sleep(TIME_SLEEP);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		if (receiver.available() < 4)// There certainly isn't a message
										// available, since every message is at
										// least 4 bytes, but the next time the
										// buffer has anything there will be a
										// message
		{
			timeout = 0;
			if (timeout++ > TIME_OUT) {
				throw new IOException("Timeout while waiting for command: no response from the server.");
			}

			receiver.read(new byte[receiver.available()]);// Flush the rest of
															// the buffer
			while (receiver.available() <= 0) {

				try {
					Thread.sleep(TIME_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			nextCommand = receiver.readInt();
		}
		return nextCommand;
	}
	public void closeConnection() throws IOException {
		sender.writeInt(Constants.DONE);
		sender.flush();
		sender.close();
		receiver.close();
		socket.close();
	}
	
	
	static public int getTimeSleep() {
		return TIME_SLEEP;
	}
	
	static public int getTimeOut() {
		return TIME_OUT;
	}
} 
/**
 ******* PROTOCOL DOCUMENTATION *******
 * 
 * Global note: Big-endian encoding is used throughout. Client indicates the computer with the front end, while server indicates the supercomputer doing the processing.
 * 
 * Message OPEN  Client -> Server
 * Notes: This should be the first message sent. It tells the remote server to open the database file. It also gives the server a little additional information to help it process the database.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x4F50454E (OPEN in ASCII)
 * 0x04		Protocol Versionint-4				Currently unused and set to 0x00010001 (Major version = 1, minor version = 1). Behavior is currently undefined if server and client disagree on the version, but the server might want to fall back if possible
 * 0x08		Path length		short-2				The length (in bytes) of the string that follows.
 * 0x0A 	Database path	string-m			UTF-8 encoded path to the database. Should end in the folder that contains the actual trace file. If the path contains strange characters that don't fit in 8 bits, it is not considered a valid path.
 * 
 * The server can then reply with DBOK or NODB
 * 
 * Message DBOK  Server -> Client
 * Notes: This indicates that the server could find the specified database and opened it. It also contains a little additional information to help the client in later rendering.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x44424F4B (DBOK in ASCII)
 * 0x04		XML Port		int-4				The port to which the client should connect to receive the XML file
 * 0x08		Trace count		int-4				The number of traces contained in the database/trace file
 * 0x0C		Compression		int-4				The compression type and algorithm used. Right now the only values are 0 = uncompressed and 1 = zlib compressed, but this could be extended. The higher-order word (mask 0xFFFF0000) is currently reserved.
 * 0x10+6n	Process ID		int-4				The process number for rank n. This is used only to label the location of the cursor. n goes from 0 to (Traces count-1)
 * 0x14+6n	Thread ID		short-2				The thread number for rank n. If this has a value of -1, then neither it nor the period between the process and thread numbers should be displayed
 * 
 * Message NODB	Server -> Client
 * Notes: This indicates that the server could not find the database or could not open it for some reason. The user should be notified and the next message the client sends should be another OPEN command.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x4E4F4442 (NODB in ASCII)
 * 0x04		Error Code		int-4				Currently unused, but the server could specify a code to make diagnosing the error easier. Set to 0 for right now.
 * 
 * 
 * Message EXML Server -> Client
 * Notes: The XML file should be sent as soon as the client connects to the port specified in DBOK. If the specified port is the same as the main data port,
 * this message must be sent on the main data port, without opening or closing the existing connection. In that case, this message should be treated like any other message.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x45584D4C (EXML in ASCII)
 * 0x04		Size			int-4				The number of bytes that follow that make up the compressed xml bytes. This is the compressed size, not the uncompressed size.
 * 0x08		Compressed XML	bytes-s				The GZIP-compressed Experiment.xml file
 * 
 * 
 * Message INFO Client -> Server
 * Notes: This contains information derived from the XML data that the server needs in order to understand the later data requests
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x494E464F (INFO in ASCII)
 * 0x04		Global Min Time long-8				The lowest starting time for all the traces. This is mapped to 0 at some point during the execution. This value comes from the experiment.xml file
 * 0x0C		Global Max Time long-8				The highest ending time for all the traces. This can also be found in experiment.xml
 * 0x12		DB Header Size	int-4				The size of the header in the DB file. TraceDataByRank uses it to pinpoint the location of each trace.
 *  
 * Message DATA Client -> Server
 * Notes: This message represents a request for the server to retrieve data from the file and return it to the client
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x44415441 (DATA in ASCII)
 * 0x04		First Process	int-4				The lower bound on the processes to be retrieved
 * 0x08		Last Process	int-4				The upper bound on the processes to be retrieved
 * 0x0C		Time Start		long-8				The lower bound on the time of the traces to be retrieved. This is the absolute time, not the time since Global Min Time.
 * 0x14		Time End		long-8				The upper bound on the time of the traces to be retrieved. Again, the absolute time.
 * 0x1C		Vertical Res	int-4				The vertical resolution of the detail view. The server uses this to determine which processes should be returned from the range [First Process, Last Process]
 * 0x20		Horizontal Res	int-4				The horizontal resolution of the detail view. The server will return approximately this many CPIDs for each trace
 * 
 * Message HERE Server -> Client
 * Notes: This is a response to the DATA request. After this message, the client may send another DATA request or a DONE shutdown command. After each rank is received, k should be incremented by (28+c). The client should expect the message to contain min(Last Process-First Process, Vertical Resolution) tracelines.
 * The raw trace data is a pair of 4-byte ints. The first int is the difference between the timestamp for this Record and the previous Record. For the first Record in the message, it should be zero, as that record will have Begin Time as its timestamp. The second int in the pair is the CPID.
 * In the notation below, increment k by (0x1C + c) every line. 
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x48455245 (HERE in ASCII)
 * 0x04+k	Line Number		int-4				The rank number whose data follows. Should be unique in the message
 * 0x08+k	Entry Count		int-4				The number of CPID's that follow
 * 0x0C+k	Begin Time		long-8				The start time of this rank, calculated by taking the timestamp of the first TimeCPID in the line
 * 0x14+k	End Time		long-8				The end time of this rank, calculated by taking the timestamp of the last TimeCPID in the line
 * 0x1C+k	Compressed Size	int-4				The size of the data, c, that follows. If compression is disabled, this should be equal to 4*(Entry Count)
 * 0x20+k	Trace Data		ints or bytes		The raw trace data. If compression is disabled, this is an array of 2x(4 bytes), one after the other. If compression is enabled, this is a compressed array of 2x(4 bytes). See the message notes for more information.
 * 
 * Message FLTR Client -> Server
 * Notes: For example, if the user wants to exclude processes 10, 14, 18, 22 ... 46, set Process Minimum to 10, Process Maximum to 46, Process Stride to 4, Exclude Matches to 1
 * After this message, the client should send a DATA message, but it is not required.
 * In the notation below, increment k by 0x18 every filter
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x464C5452 (FLTR in ASCII)
 * 0x04		Padding			byte-1				Set to 0 or any other value. Must be ignored.
 * 0x05		Exclude Matches	byte-1				Set to 0 to include only the traces that match the patterns. Set to 1 to include all traces except the ones that match the patterns
 * 0x06		Filters count	short-2				The number of filters that will follow
 * 0x08+k	Process Minimum	int-4				The lower inclusive bound of processes that match this filter
 * 0x0C+k	Process Maximum	int-4				The upper inclusive bound of processes that match this filter
 * 0x10+k	Process Stride	int-4				The interval in between the processes that match this filter. Do not set to zero.
 * 0x14+k	Thread Minimum	int-4				The lower inclusive bound of the threads that match this filter
 * 0x18+k	Thread Maximum	int-4				The upper inclusive bound of the threads that match this filter
 * 0x1C+k	Thread Stride	int-4				The interval in between the threads that match this filter. Don't set to zero.		
 * 
 * Message DONE Client -> Server
 * Notes: After receiving this message, the server should close. The client cannot send any messages after this without opening a new connection
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x444F4E45 (DONE in ASCII)
 */

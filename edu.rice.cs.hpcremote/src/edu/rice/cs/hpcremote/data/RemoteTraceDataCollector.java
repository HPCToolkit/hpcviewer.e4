package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import edu.rice.cs.hpcbase.AbstractTraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcdata.db.IdTuple;

public class RemoteTraceDataCollector extends AbstractTraceDataCollector 
{
	private final IdTuple idtuple;
	private final int line;

	public RemoteTraceDataCollector(int line, IdTuple idtuple, int numPixelH, TraceOption option) {
		super(option, numPixelH);
		
		this.line = line;
		this.idtuple   = idtuple;
	}
	

	@Override
	public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException {
		// nothing to do
	}
	

	@Override
	public void duplicate(ITraceDataCollector traceData) {
		var obj = new RemoteTraceDataCollector(line, idtuple, getNumPixelHorizontal(), getTraceOption());
		obj.setListOfCallpathId(getListOfCallpathId());
	}
}

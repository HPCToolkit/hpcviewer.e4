package edu.rice.cs.hpcdata.tld.plot;

public class DataPlotEntry 
{
	public int tid;		// thread (or rank) id
	public double metval;	// metric value
	
	public DataPlotEntry()
	{
		this(0, 0.0d);
	}
	public DataPlotEntry(int tid, double metval)
	{
		this.tid = tid;
		this.metval = metval;
	}
	
	public String toString()
	{
		return String.format("(%d, %.2f)", tid, metval);
	}
}

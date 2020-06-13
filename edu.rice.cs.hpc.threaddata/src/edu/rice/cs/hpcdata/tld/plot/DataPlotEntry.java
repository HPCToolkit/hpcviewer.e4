package edu.rice.cs.hpcdata.tld.plot;

public class DataPlotEntry 
{
	public int tid;		// thread (or rank) id
	public float metval;	// metric value
	
	public DataPlotEntry()
	{
		this(0,0);
	}
	public DataPlotEntry(int tid, int metval)
	{
		this.tid = tid;
		this.metval = metval;
	}
	
	public String toString()
	{
		return String.format("(%d, %.2f)", tid, metval);
	}
}

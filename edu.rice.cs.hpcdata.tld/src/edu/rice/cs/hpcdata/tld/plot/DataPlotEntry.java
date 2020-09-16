package edu.rice.cs.hpcdata.tld.plot;


/*****
 * 
 * Class containing a plot data entry
 * <ul>
 * <li>a thread id (int)
 * <li>a metric value (double)
 * </ul>
 *
 */
public class DataPlotEntry 
{
	public static final int SIZE = 4 + 8; 
			
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

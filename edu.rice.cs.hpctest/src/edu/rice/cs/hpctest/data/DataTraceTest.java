package edu.rice.cs.hpctest.data;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.version4.DataTrace;

public class DataTraceTest 
{

	/***************************
	 * unit test 
	 * 
	 * @param argv
	 ***************************/
	public static void main(String []argv)
	{
		final DataTrace trace_data = new DataTrace();
		final String filename;
		if (argv != null && argv.length>0) 
		{
			filename = argv[0];
		} else {
			filename = "/Users/la5/Data/prof2/d.none.cu_call_path/trace.db";
		}
		try {
			trace_data.open(filename);			
			trace_data.printInfo(System.out);
			trace_data.dispose();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

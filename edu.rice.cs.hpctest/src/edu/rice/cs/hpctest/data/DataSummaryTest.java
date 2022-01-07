package edu.rice.cs.hpctest.data;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataSummary;

public class DataSummaryTest 
{

	/***************************
	 * unit test 
	 * 
	 * @param argv
	 ***************************/
	public static void main(String []argv)
	{
		final String DEFAULT_FILE = "/Users/la5/Data/prof2/ht2.sdt.8n/profile.db";
		final String filename;
		if (argv != null && argv.length>0)
			filename = argv[0];
		else
			filename = DEFAULT_FILE;
		IdTupleType type = IdTupleType.createTypeWithOldFormat();
		
		final DataSummary summary_data = new DataSummary(type);
		try {
			summary_data.open(filename);			
			summary_data.printInfo(System.out);
			summary_data.dispose();	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

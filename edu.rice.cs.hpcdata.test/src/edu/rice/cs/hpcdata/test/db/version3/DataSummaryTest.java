package edu.rice.cs.hpcdata.test.db.version3;

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
		final String DEFAULT_FILE = "/home/la5/data/prof2/dpr-torch/profile.db";
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

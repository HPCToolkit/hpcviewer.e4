package edu.rice.cs.hpctest.data;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.version4.DataPlot;

public class DataPlotTest {

	
	
	/////////////////////////////////////////////////////////////////////////////
	// unit test
	/////////////////////////////////////////////////////////////////////////////
	static public void main(String []argv)
	{
		final DataPlot data = new DataPlot();
		String filename;
		if (argv != null && argv.length>0) {
			filename = argv[0];
		} else {
			filename = "/Users/la5/Data/prof2/ht2.sdt.8n/cct.db"; 
		}
		try {
			data.open(filename);
			data.printInfo(System.out);
			data.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

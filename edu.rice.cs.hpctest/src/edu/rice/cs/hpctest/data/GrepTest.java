package edu.rice.cs.hpctest.data;

import java.io.IOException;

import edu.rice.cs.hpcdata.util.Grep;

public class GrepTest 
{

	/**
	 * for unit test only
	 * @param Argvs
	 */
	static public void main(String args[])
	{
		if (args.length>1)
		{
			final String file = args[0];
			final String fout = args[1];
			try {
				Grep.grep(file, fout, "<M", false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

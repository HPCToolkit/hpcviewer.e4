package edu.rice.cs.hpcdata.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * 
 * grep class
 *
 */
public class Grep 
{

	/**
	 * 
	 * @param fin
	 * @param fout
	 * @param pattern
	 * @param shouldMatch
	 * @throws IOException 
	 */
	static public void grep(String fin, String fout, String pattern, boolean shouldMatch)
			throws IOException
	{
		// using specific grep to remove metric tag
		removeMetrics(fin, fout);
	}
	
	
	/***
	 * Simplified grep specifically against metric tags in an xml file
	 * This method is not a generic grep, it assumes that all tags are fit in one line
	 * 
	 * @param fin
	 * @param fout
	 * @throws IOException
	 */
	private static void removeMetrics(String fin, String fout) 
			throws IOException 
	{
		BufferedReader inputFile  = null;
		BufferedWriter outputFile = null;
		try {
			inputFile  = new BufferedReader(new InputStreamReader(new FileInputStream(fin)));
			outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout))); 
			
			Pattern metricTag = Pattern.compile("<M ");
			Matcher matcher = metricTag.matcher("");
			
			String inputLine;
			while ((inputLine = inputFile.readLine()) != null) {
				matcher.reset(inputLine);
		        if (!matcher.lookingAt()) {
		        	outputFile.write(inputLine);
		        	outputFile.newLine();
		        }
			}
		} finally {
			inputFile.close();
			outputFile.close();
		}
	}
}

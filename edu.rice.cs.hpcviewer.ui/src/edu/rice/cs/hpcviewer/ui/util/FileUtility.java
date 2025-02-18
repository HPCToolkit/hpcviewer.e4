// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtility 
{
	/***
	 * Read the content of the file
	 * @param filename
	 * @return String
	 * @throws IOException
	 */
	public static String getFileContent(String filename) throws IOException {
		File file = new File(filename);
		if (!file.canRead())
			return "";
		
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		
		String content = new String(data, "UTF-8");				
		fis.close();
		
		return content;
	}
	
	
	/****
	 * Remove the content of the file without deleting it.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public static void clearFileContent(String filename) throws IOException {
		new PrintWriter(filename).close();
	}
}

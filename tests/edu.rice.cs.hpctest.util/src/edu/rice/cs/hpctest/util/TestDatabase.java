package edu.rice.cs.hpctest.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;

public class TestDatabase 
{
	private static final String DIR_XML    = "xml"; 
	private static final String DIR_METADB = "metadb"; 
	
	private static String []xmlPaths = new String[] {DIR_XML + File.separator + "bug-no-gpu-trace", 
													 DIR_XML + File.separator + "bug-empty", 
													 DIR_XML + File.separator + "bug-nometric", 
													 DIR_XML + File.separator + "inline.xml.db"};
	
	private static String []metadbPaths = new String[] {
												  DIR_METADB + File.separator + "loop-fork-cycles",
												  DIR_METADB + File.separator + "lmp-openmp",
												  DIR_METADB + File.separator + "inline.meta.db"};	
	
	public static File[] getXMLDatabases() {
		return getDatabases(xmlPaths);
	}

	
	public static File[] getMetaDatabases() {
		return getDatabases(metadbPaths);
	}
	
	
	public static File[] getDatabases() {
		String []paths = Arrays.copyOf(xmlPaths, xmlPaths.length + metadbPaths.length);
		for(int i=0; i<metadbPaths.length; i++) {
			paths[i + xmlPaths.length] = metadbPaths[i];
		}
		return getDatabases(paths);
	}
	
	private static File[] getDatabases(String []paths) {
		File []dirs = new File[paths.length];
		int i=0;
		for(var path: paths) {
			Path resource = Paths.get("..", "resources", path);
			dirs[i] = resource.toFile();
			i++;
		}
		return dirs;
	}
	
	
	@Test
	public void testAll() {
		File []xmls = getXMLDatabases();
		checkDirectory(xmls, xmlPaths.length);
		
		File []metadb = getMetaDatabases();
		checkDirectory(metadb, metadbPaths.length);
		
		File []all = getDatabases();
		checkDirectory(all, xmls.length + metadb.length);
	}
	
	private boolean checkDirectory(File[] dirs, int length) {
		assertNotNull(dirs);
		assertTrue(dirs.length == length);
		
		for(File d: dirs) {
			if (!d.canRead() || !d.isDirectory())
				return false;
		}
		return true;
	}
} 

// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctest.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.version2.TraceDB2;
import edu.rice.cs.hpcdata.db.version4.FileDB4;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection4;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IProgressReport;

public class TestDatabase 
{
	private static final String []FILES_EXPERIMENT  = {"experiment.xml", "meta.db"};	
	
	
	public static File[] getDatabases() {
		return getDatabases(getDatabasePath());
	}
	
	public static File[] getDatabases(String prefix) {
		var dirs  = new File(prefix);
		var paths = Stream.of(dirs.listFiles())
			  .filter(File::isDirectory)
			  .map(File::getAbsolutePath)
			  .collect(Collectors.toSet());
		
		return getDatabases(paths);
	}


	public static File[] getDatabases(Set<String> paths) {
		File []dirs = new File[paths.size()];
		int i=0;
		for(var path: paths) {
			Path resource = Paths.get(path);
			dirs[i] = resource.toFile();
			assertTrue("Directory is not readable: " + resource.toString(), dirs[i].canRead());
			i++;
		}
		return dirs;
	}

	
	public static List<Experiment> getExperiments() throws Exception {
		File[]dirs = getDatabases();
		List<Experiment> listExp = new ArrayList<>(dirs.length);
		for(var dir: dirs) {
			Experiment e = new Experiment();
			var localDb = new LocalDatabaseRepresentation(dir, null, IProgressReport.dummy());
			e.open(localDb);
			listExp.add(e);
		}
		return listExp;
	}
	
	
	public static List<IFileDB> getFileDbs() throws Exception {
		var directories = TestDatabase.getDatabases();
		
		var listFileDB = new ArrayList<IFileDB>(directories.length);
		
		for(var dir: directories) {
			Experiment exp = new Experiment();
			exp.open(new LocalDatabaseRepresentation(dir, null, IProgressReport.dummy()));
			
			// Assume all test database should include traces
			// In the future this assumption may not be correct
			assertTrue(exp.getTraceDataVersion() >= 0);

			IFileDB fileDb = null;
			
			if (exp.getMajorVersion() == Constants.EXPERIMENT_SPARSE_VERSION) {
				var root = exp.getRootScope(RootScopeType.CallingContextTree);
				MetricValueCollection4 mvc = (MetricValueCollection4) root.getMetricValueCollection();
				var dataSummary = mvc.getDataSummary();
				fileDb = new FileDB4(exp, dataSummary);
			} else if (exp.getMajorVersion() == Constants.EXPERIMENT_DENSED_VERSION) {
				fileDb = new TraceDB2(exp);
			} else {
				fail();
				continue; // just to avoid warning 
			}
			assertNotNull(fileDb);
			
			fileDb.open(dir.getAbsolutePath());
			
			listFileDB.add(fileDb);
		}
		return listFileDB;
	}

	@Test
	public void testAll() {
		File []all = getDatabases(getDatabasePath());
		checkDirectory(all);
	}
	

	private boolean checkDirectory(File[] dirs) {
		assertNotNull(dirs);
		assertTrue(dirs.length > 0);

		for(File d: dirs) {
			assertTrue(d.canRead() && d.isDirectory());
			var files = d.listFiles();
			for(var file: files) {
				final var name = file.getName();
				var found = Stream.of(FILES_EXPERIMENT)
								  .filter(fileName -> fileName.equals(name))
								  .findAny();
				if (found.isPresent())
					return true;

			}
			fail("No database in " + d.getAbsolutePath());
		}
		fail("No database");
		return false;
	}
	
	
	private static String getDatabasePath() {		
		var dir = Paths.get("..", "resources", "metadb").toFile();
		return dir.getAbsolutePath();
	}

}

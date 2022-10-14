//////////////////////////////////////////////////////////////////////////
//									//
//	ExperimentFileXML.java						//
//									//
//	experiment.ExperimentFileXML -- a file containing an experiment	//
//	$LastChangedDate: 2011-11-29 17:10:53 -0600 (Tue, 29 Nov 2011) $			//
//									//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.xml;


import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.*;
import edu.rice.cs.hpcdata.util.IUserData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;




//////////////////////////////////////////////////////////////////////////
//	CLASS EXPERIMENT-FILE-XML											//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A file containing an HPCView experiment in XML representation.
 *
 */


public class ExperimentFileXML extends ExperimentFile
{
	
	
//////////////////////////////////////////////////////////////////////////
//	XML PARSING															//
//////////////////////////////////////////////////////////////////////////

/**
 * Parses the file and returns the contained experiment subparts. The
 * subparts are returned by adding them to given lists.
 * 
 * This version is able to work with the file not being located on physical
 * disk, but rather a reference to an in-memory stream from a socket
 * (potentially compressed).
 * 
 * Note: The GREP code has been moved to the <code>SpaceTimeDataControllerLocal</code>
 * because it is only needed on the local version, and that is the last
 * place where the actual file (as opposed to a stream referring to the
 * file) is available.
 * 
 * @param stream : input stream
 * @param name
 *            The name of the file. For now, this is hard coded, but it
 *            should be obvious from the file chosen in the remote browser
 *            UI
 * @param experiment
 *            Experiment object to own the parsed sub parts.
 * @param needMetrics 
 * 			  flag whether the app needs metrics (hpcviewer) or not 
 * 				(hpctraceviewer)
 * @param userData
 *            I don't know why this is here, since it apparently isn't used.
 *            I'm leaving it for maximum compatibility.
 * @throws Exception
 */
public void parse(InputStream stream, String name,
		IExperiment experiment, boolean needMetrics, IUserData<String, String> userData)
		throws Exception {
	
	final Builder builder = new ExperimentBuilder2(experiment, name, userData);
	// We assume it has already been GREP-ed by the server if it needs to be

	IParser parser = new Parser(name, stream, builder);
	parser.parse(name);

	if (builder.getParseOK() != Builder.PARSER_OK) {
		throw new InvalExperimentException(
				"Parse error in Experiment XML at line " + 
				builder.getParseErrorLineNumber());
	}
}


/*************************************************************************
 *	Parses the file and returns the contained experiment subparts.
 *	The subparts are returned by adding them to given lists.
 *
 * @param  	location 				location of the database
 * @param  	BaseExperiment		Experiment object to own the parsed subparts.
 * @param	need_metrics		whether to read metrics or not
 * @param	userData			user preference data
 * 
 * @throws 	Exception 
 *
 ************************************************************************/
	
public File parse(File location, IExperiment experiment, boolean need_metrics, IUserData<String, String> userData)
		throws	Exception
		{
	// get an appropriate input stream
	InputStream stream;
	String name = location.toString();

	// check if the argument "file" is really a file (old version) or a directory (new version)
	String directory;
	String xmlFilePath;
	if (location.isDirectory()) {
		directory = location.getAbsolutePath(); // it's a database directory
		xmlFilePath = directory + File.separatorChar + DatabaseManager.getDatabaseFilename("xml").orElse("");
	} else {
		directory = location.getParent(); // it's experiment.xml file
		xmlFilePath = location.getAbsolutePath();
	}

	File XMLfile = new File(xmlFilePath);
	
	if (!XMLfile.canRead()) {
		throw new IOException("File does not exist or not readable: " + XMLfile.getAbsolutePath());
	}
	
	// parse the stream
	stream = new FileInputStream(XMLfile);
	final Builder builder = new ExperimentBuilder2(experiment, name, userData);
	
	IParser parser = new Parser(name, stream, builder);
	parser.parse(name);

	if ( builder.getParseOK() != Builder.PARSER_OK ) {
		throw new InvalExperimentException(builder.getParseErrorLineNumber());        	
	}
	return XMLfile;
}
}

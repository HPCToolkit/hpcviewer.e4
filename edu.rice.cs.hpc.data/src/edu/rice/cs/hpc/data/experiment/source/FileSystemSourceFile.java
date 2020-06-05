//////////////////////////////////////////////////////////////////////////
//																		//
//	FileSystemSourceFile.java											//
//																		//
//	experiment.source.SourceFile -- a source file contained in a file	//
//	Last edited: January 29, 2001 at 12:21								//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.source;


import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;

import edu.rice.cs.hpc.data.util.*;

import java.io.*;





//////////////////////////////////////////////////////////////////////////
//	CLASS SOURCE-FILE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A reference to a source file in an HPCView experiment.
 *
 */


public class FileSystemSourceFile implements SourceFile
{
/** The type ID of the Source file. */
public static final int STATICID = 1111;
/** The experiment owning this source file. */
protected BaseExperiment experiment;

/** The filename of this source file. */
protected File filename;

/** Whether an attempt has been made to locate this source file's contents. */
protected boolean hasBeenSought;

/** Whether this source file's contents can be located and read. */
protected boolean contentsAvailable;

/** The actual path to this source file's contents, lazily computed.
    Should not be accessed unless <code>this.contentsAvailable</code>. */
protected File resolvedPath;

/** The number of lines in this source file, lazily computed.
    Should not be accessed unless <code>this.contentsAvailable</code>. */
protected int lineCount;

protected boolean istext;

protected String longName;

/**
 * The ID of the file, to be looked in the experiment's hashtable
 */
protected int id;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a SourceFile.
 ************************************************************************/
	
public FileSystemSourceFile(BaseExperiment experiment, File filename, int idFile)
{
	super();

	// creation arguments
	this.experiment = experiment;
	this.filename = filename;

	// lazily computed file properties
	this.hasBeenSought     = false;
	this.contentsAvailable = true;
	
	this.longName = null;
	//  laks: bug: default is a text file
	this.istext = true;
	// Note: 'this.resolvedPath' and 'this.lineCount' should not be accessed
	//       unless 'this.contentsAvailable'.
	this.id = idFile;
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO CONTENTS													//
//////////////////////////////////////////////////////////////////////////



public int getFileID () {
	return this.id;
}

/*************************************************************************
 *	Returns the source file's user visible name.
 ************************************************************************/
	
public String getName()
{
	if (longName == null) {
		if (istext) {
			longName = filename.getName();
		}
		else {
			longName = "binary file " + filename.getName();
		}
	}
	return this.longName;
}




/*************************************************************************
 *	Returns the source file's filename.
 ************************************************************************/
	
public File getFilename()
{
	return this.filename;
}




/*************************************************************************
 *	Returns the number of lines in the source file.
 *
 *	<p>
 *	This method should only be called if <code>this.isAvailable()</code>.
 *
 ************************************************************************/
	
public int getLineCount()
{
	this.requireAvailable();
	if( this.lineCount == -1 )
		this.computeLineCount();
	return this.lineCount;
}




/*************************************************************************
 *	Counts and stores the number of lines in the source file.
 *
 *	<p>
 *	This method is only called if <code>this.isAvailable()</code>.
 *
 ************************************************************************/

protected void computeLineCount()
{
	Dialogs.Assert(this.contentsAvailable, "contents not available FileSystemSourceFile::Compute Line Count");

	InputStream inputStream = this.getStream();
	LineNumberReader reader = new LineNumberReader(new InputStreamReader(inputStream));
	try
	{
		boolean done = false;
		while( ! done)
		{
			String line = reader.readLine();
			done = (line == null);
		}
	}
	catch( IOException e)
	{
		Dialogs.fatalException(Strings.CANT_READ_SOURCEFILE, this.getName(), e);
	}

	this.lineCount = 1 + reader.getLineNumber();
	try {
		reader.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}




/*************************************************************************
 *	Returns an open input stream for reading the source file's contents.
 *
 *	The stream will close itself on finalization.
 *	<p>
 *	This method should only be called if <code>this.isAvailable()</code>.
 *
 ************************************************************************/
	
public InputStream getStream()
{
	this.requireAvailable();
	InputStream stream;

	try
	{
		stream = new FileInputStream(this.resolvedPath);
	}
	catch( IOException e)
	{
		Dialogs.fatalException(Strings.CANT_OPEN_SOURCEFILE, this.getName(), e);
		stream = null;	// compiler can't see 'stream' is returned iff initialized
	}

	return stream;
}




//////////////////////////////////////////////////////////////////////////
//	AVAILABILITY OF SOURCE FILE CONTENTS								//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns whether the source file can be located and read.
 ************************************************************************/
	
public boolean isAvailable()
{
	if( ! this.hasBeenSought )
		this.searchForContents();

	return this.contentsAvailable;
}



public boolean isText()
 {
   return this.istext;
 }


public void setIsText(boolean bi)
  { 
     this.istext=bi;
  }

/*************************************************************************
 *	Returns whether this source file has a line with the given line number.
 ************************************************************************/
	
public boolean hasLine(int lineNumber)
{
	Dialogs.Assert(this.contentsAvailable, "contents not available FileSystemSourceFile::Compute Line Count");

	return (lineNumber >= 0) && (lineNumber <= this.getLineCount());
}




/*************************************************************************
 *	Demands that the source file be available.
 *
 *	Fails if it isn't.
 *
 ************************************************************************/
	
protected void requireAvailable()
{
	if( ! this.isAvailable() )
		Dialogs.fail2("Attempt to open an unavailable SourceFile", this.getName());
}




/*************************************************************************
 *	Searches for the source file's contents.
 *
 *	If the file's path is relative, it is sought at each path in the
 *	experiment's configuration and in the experiment's default directory.
 *	Once the file has been found in a directory the search ends, even if
 *	that file is not readable. Even if the file is found and readable,
 *	reading it to count its lines is deferred until the first time the line
 *	count is needed.
 *	<p>
 *	The search is performed even if <code>this.hasBeenSought</code>, so the
 *	search can be repeated under different conditions if necessary.
 *
 ************************************************************************/

protected void searchForContents()
{
	assert (this.experiment instanceof BaseExperimentWithMetrics);
	
	File resolved;
	boolean found;

	// first use empty search path -- handles absolute and default-dir-relative names
	resolved = this.makeSearchFile(null);
	found    = resolved.exists();

	// next look in each search path
	if( ! found )
	{
		final BaseExperimentWithMetrics exp = (BaseExperimentWithMetrics) this.experiment;
		int count = exp.getSearchPathCount();
		for( int k = 0;  (k < count) && (! found);  k++ )
		{
			File search = exp.getSearchPath(k);
			resolved    = this.makeSearchFile(search);
			found       = resolved.exists();
		}
	}

	// set instance variables to record search result
	this.hasBeenSought     = true;
	this.contentsAvailable = (found && resolved.canRead());
	this.resolvedPath      = resolved;
	this.lineCount         = -1;		// counting lines is deferred
}




/*************************************************************************
 *	Returns a <code>File</code> object for this source file's contents
 *	assuming it were at a given search path.
 *
 *	A relative search path is taken to be relative to the experiment's
 *	default directory.
 *
 ************************************************************************/

protected File makeSearchFile(File search)
{
	assert (this.experiment instanceof BaseExperimentWithMetrics);
	
	String filenamePath = this.filename.getPath();				// there is no 'File(File, File)' constructor, sigh!
	File file;

		file = new File(search, filenamePath);
		if( ! file.isAbsolute() )
		{
			File defaultDir = this.experiment.getDefaultDirectory();
			file = new File(defaultDir, file.getPath());		// there is no 'File(File, File)' constructor, sigh!
		}

	return file;
}


/**
 * Laks: need this function for Eclipse editor !
 * Get the complete filename when it is available based on resolvedPath
 * @return the complete absolute file path
 */
public String getCompleteFilename() {
	if(this.contentsAvailable) {
		if(this.resolvedPath != null)
			return this.resolvedPath.getAbsolutePath();
	} else
		return this.getFilename().getAbsolutePath();
	return null;
}


}









//////////////////////////////////////////////////////////////////////////
//																		//
//	AbstractSourceFile.java												//
//																		//
//	experiment.source.AbstractSourceFile -- generic source file class	//
//	Last edited: October 10, 2001 at 4:24 pm							//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////
package edu.rice.cs.hpc.data.experiment.source;
import java.io.InputStream;
//////////////////////////////////////////////////////////////////////////
//	CLASS ABSTRACT-SOURCE-FILE											//
//////////////////////////////////////////////////////////////////////////
 /**
 *
 *	An abstract superclass for all source file classes.
 *
 *	This class defines the required protocol and supplies some default
 *	implementations as a convenience. Actually naming and accessing
 *	a source file are subclass responsibilities.
 *
 */
public abstract class AbstractSourceFile
{
//////////////////////////////////////////////////////////////////////////
//	ACCESS TO CONTENTS													//
//////////////////////////////////////////////////////////////////////////
/*************************************************************************
 *	Returns the source file's user visible name.
 ************************************************************************/
	
public abstract String getName();
/*************************************************************************
 *	Returns the number of lines in the source file.
 ************************************************************************/
	
public abstract int getLineCount();
/*************************************************************************
 *	Returns an open input stream for reading the file's contents.
 ************************************************************************/
	
public abstract InputStream getStream();
//////////////////////////////////////////////////////////////////////////
//	AVAILABILITY OF SOURCE FILE CONTENTS								//
//////////////////////////////////////////////////////////////////////////
/*************************************************************************
 *	Returns whether this source file can be located and read.
 ************************************************************************/
	
public abstract boolean isAvailable();
}

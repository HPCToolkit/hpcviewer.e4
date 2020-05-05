//////////////////////////////////////////////////////////////////////////
//																		//
//	SourceFile.java														//
//																		//
//	experiment.source.SourceFile -- interface of all source file classes//
//	Last edited: January 29, 2001 at 12:21								//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.source;




import java.io.File;
import java.io.InputStream;




//////////////////////////////////////////////////////////////////////////
//	INTERFACE SOURCE-FILE												//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 *	The interface implemented by all source file classes.
 *
 */


public interface SourceFile
{

//////////////////////////////////////////////////////////////////////////
//	PUBLIC CONSTANTS													//
//////////////////////////////////////////////////////////////////////////

/** Empty source file for use by scopes which logically lack a source file. */
public static final SourceFile NONE = new EmptySourceFile();




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO CONTENTS													//
//////////////////////////////////////////////////////////////////////////

public abstract int getFileID();



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



/*************************************************************************
 *	Returns whether this source file has a line with the given line number.
 ************************************************************************/
	
public abstract boolean hasLine(int lineNumber);




public abstract File getFilename();


public abstract boolean isText();

public abstract void  setIsText(boolean bi);

}









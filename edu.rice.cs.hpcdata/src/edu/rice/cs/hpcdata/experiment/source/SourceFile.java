//////////////////////////////////////////////////////////////////////////
//																		//
//	SourceFile.java														//
//																		//
//	experiment.source.SourceFile -- interface of all source file classes//
//	Last edited: January 29, 2001 at 12:21								//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.source;




import java.io.File;




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


//////////////////////////////////////////////////////////////////////////
//	AVAILABILITY OF SOURCE FILE CONTENTS								//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns whether this source file can be located and read.
 ************************************************************************/
	
public abstract boolean isAvailable();


public abstract File getFilename();
}









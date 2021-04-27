//////////////////////////////////////////////////////////////////////////
//																		//
//	EmptySourceFile.java												//
//																		//
//	experiment.source.EmptySourceFile -- an empty source file			//
//	Last edited: January 29, 2001 at 12:21								//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.source;




import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;




//////////////////////////////////////////////////////////////////////////
//	CLASS EMPTY-SOURCE-FILE												//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A reference to an empty source file.
 *
 *	Such a file is useful as a placeholder to avoid special cases in code
 *	when there may sometimes be no logically appropriate source file. An
 *	empty source file has no associated experiment, its name is the empty
 *	string, and its text stream is empty. It is always "available".
 *
 *	@see edu.rice.cs.hpcview.view.source.SourceView#Scope()
 *
 */


public class EmptySourceFile implements SourceFile
{


//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates an empty source file.
 ************************************************************************/
	
public EmptySourceFile()
{
	super();
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO CONTENTS													//
//////////////////////////////////////////////////////////////////////////


/*************************************************************************
 *	Returns the source file's filename.
 ************************************************************************/
	
public File getFilename()
{
	return null;
}

/*************************************************************************
 *	Returns the source file's user visible name.
 ************************************************************************/
	
public String getName()
{
	return "";
}




/*************************************************************************
 *	Returns the number of lines in the source file.
 ************************************************************************/
	
public int getLineCount()
{
	return 0;
}




/*************************************************************************
 *	Returns an open input stream for reading the source file's contents.
 *
 *	In this class an empty stream is returned.
 *
 ************************************************************************/
	
public InputStream getStream()
{
	return new ByteArrayInputStream(new byte[0]);
}




//////////////////////////////////////////////////////////////////////////
//	AVAILABILITY OF SOURCE FILE CONTENTS								//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns whether the source file can be located and read.
 ************************************************************************/
	
public boolean isAvailable()
{
	return true;
}

@Deprecated
public void setIsText(boolean bi)
{
}

/*************************************************************************
 *	Returns whether this source file has a line with the given line number.
 ************************************************************************/
	
public boolean hasLine(int lineNumber)
{
	return false;
}


/*************************************************************************
 *	Returns whether the source file is text (not load module/binary)
 ************************************************************************/
@Deprecated
public boolean isText()
{
        return true;
}




public int getFileID() {
	// TODO Auto-generated method stub
	return 0;
}

}









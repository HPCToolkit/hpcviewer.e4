//////////////////////////////////////////////////////////////////////////
//																		//
//	Builder.java														//
//																		//
//	experiment.xml.Builder -- interface for SAX2 state machines			//
//	Last edited: January 16, 2002 at 12:41 pm							//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.xml;







//////////////////////////////////////////////////////////////////////////
//	CLASS BUILDER														//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * Abstract superclass of all XML builder classes.
 *
 */


public abstract class Builder
{

	//Constant
	static public int PARSER_OK = 0;
	static public int PARSER_FAIL = 1;
	static public int PARSER_OLDXML = 2;

/** The parser which owns this builder. */
protected Parser parser;

/** Whether parsing was successful. */
protected int parseOK;

/** The line number of the first parse error in the file. */
protected int parseErrorLineNumber;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a ExperimentBuilder.
 ************************************************************************/
	
public Builder()
{
	this.parser = null;		// must be set non-null by 'setParser'
	
	this.parseErrorLineNumber = -1;
}




/*************************************************************************
 *	Sets the parser owning an ExperimentBuilder.
 *
 *	This is necessary because circularity prevents passing the parser
 *	as a constructor argument.
 *
 ************************************************************************/
	
public void setParser(Parser parser)
{
	this.parser = parser;
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO RESULTS OF PARSING										//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns whether parsing was successful.
 ************************************************************************/
	
public int getParseOK()
{
	return this.parseOK;
}




/*************************************************************************
 *	Returns the line number of the first parse error in the file.
 ************************************************************************/
	
public int getParseErrorLineNumber()
{
	return this.parseErrorLineNumber;
}




//////////////////////////////////////////////////////////////////////////
//	PARSING SEMANTICS													//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Initializes the build process.
 ************************************************************************/
	
public abstract void begin();




/*************************************************************************
 *	Takes notice of the beginning of an element.
 * @throws OldXMLFormatException 
 ************************************************************************/
	
public abstract void beginElement(String element, String[] attributes, String[] values);




/*************************************************************************
 *	Takes notice of content characters within an element.
 ************************************************************************/
	
public abstract void content(String s);




/*************************************************************************
 *	Takes notice of the ending of an element.
 ************************************************************************/
	
public abstract void endElement(String element);




/*************************************************************************
 *	Finalizes the build process.
 ************************************************************************/
	
public abstract void end();




/*************************************************************************
 *	Takes notice of a parsing error.
 ************************************************************************/
	
public void error(int lineNumber)
{
	if( this.parseOK == Builder.PARSER_OK)
	{
		this.parseOK = Builder.PARSER_FAIL;
		this.parseErrorLineNumber = lineNumber;
	}
}




/*************************************************************************
 *	Takes notice of a parsing error with an implicit line number.
 ************************************************************************/
	
public void error()
{
	this.error(this.parser.getLineNumber());
}




/*************************************************************************
 *	Causes a parse error if a predicate is not true.
 ************************************************************************/
	
public void Assert(boolean predicate)
{
	if( ! predicate )  this.error();
}




}









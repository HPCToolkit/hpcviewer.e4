//////////////////////////////////////////////////////////////////////////
//																		//
//	InvalExperimentException.java										//
//																		//
//	experiment.InvalExperimentException -- bad experiment file  		//
//	Last edited: January 16, 2002 at 12:49 pm							//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment;




//////////////////////////////////////////////////////////////////////////
//	CLASS EXPERIMENT-INVALID-EXCEPTION									//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * Exception thrown when trying to parse an invalid experiment file.
 *
 */


// @SuppressWarnings("serial")
public class InvalExperimentException extends java.lang.Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int errorLineNumber;

	public InvalExperimentException(int errorLineNumber)
	{
		this.errorLineNumber = errorLineNumber;
	}

	
	public InvalExperimentException(String sMsg)
	{
		super(sMsg);
	}
	
	
	public int getLineNumber()
	{
		return this.errorLineNumber;
	}
};





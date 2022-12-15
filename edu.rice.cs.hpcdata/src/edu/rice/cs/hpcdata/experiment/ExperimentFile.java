//////////////////////////////////////////////////////////////////////////
//																		//
//	ExperimentFile.java													//
//																		//
//	experiment.ExperimentFile -- interface for experiment files  		//
//	Last edited: June 10, 2001 at 11:59 pm								//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment;


import java.io.File;

import edu.rice.cs.hpcdata.util.IUserData;




//////////////////////////////////////////////////////////////////////////
//	INTERFACE EXPERIMENT-FILE											//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * Interface that HPCView experiment files must implement.
 *
 */


public abstract class ExperimentFile
{




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO FILE CONTENTS												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Parses the file and returns the contained experiment subparts.
 *	The subparts are returned by adding them to given lists.
 *
 *	@param	experiment		Experiment object to own the parsed subparts.
 * @throws Exception 
 *
 ************************************************************************/
	
public abstract File parse(File file, IExperiment experiment, 
		boolean needMetrics, IUserData<String, String> userData)
throws
	Exception;




}









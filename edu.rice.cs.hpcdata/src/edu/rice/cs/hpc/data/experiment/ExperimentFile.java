//////////////////////////////////////////////////////////////////////////
//																		//
//	ExperimentFile.java													//
//																		//
//	experiment.ExperimentFile -- interface for experiment files  		//
//	Last edited: June 10, 2001 at 11:59 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment;


import java.io.File;

import edu.rice.cs.hpc.data.util.IUserData;




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
	
public abstract File parse(File file, BaseExperiment experiment, 
		boolean need_metrics, IUserData<String, String> userData)
throws
	Exception;




}









/**
 * 
 */
package edu.rice.cs.hpcdata.experiment.metric;

import com.graphbuilder.math.FuncMap;
import com.graphbuilder.math.func.Function;

/*************************************************
 *
 * Special function class for hpcviewer.
 *
 *************************************************/
public class ExtFuncMap extends FuncMap 
{
	private static ExtFuncMap instance;
	
	/*****
	 * Retrieve the singleton of hpcviewer's default function map
	 * 
	 * @return ExtFuncMap
	 */
	public static ExtFuncMap getInstance() {
		if (instance == null) {
			instance = new ExtFuncMap();
		}
		return instance;
	}
	
	/**
	 * Constructor with custom case sensitivity
	 * 
	 * @param caseSensitive
	 * 			boolean {@code true} if the variables in the expression is case sensitive
	 */
	protected ExtFuncMap(boolean caseSensitive) {
		super(caseSensitive);
	}

	/***
	 * construct list of function specifically for hpcdata
	 */
	protected ExtFuncMap() {
		super(false);
		this.init();
	}

	
	/***
	 * Initialize the object by adding special functions
	 */
	public void init() {
		StdDevFunction fctStdDev = new StdDevFunction();

		this.setFunction("stdev", fctStdDev);
		this.loadDefaultFunctions();
	}
	
	
	public String []getFunctionNamesWithType() {
		Function []list = getFunctions();
		String []names  = new String[list.length];
		
		for(int i=0; i<list.length; i++) {
			names[i] = list[i].toString();
		}
		return names;
	}
}

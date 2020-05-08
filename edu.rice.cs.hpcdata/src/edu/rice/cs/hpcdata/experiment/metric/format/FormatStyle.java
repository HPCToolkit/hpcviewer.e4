package edu.rice.cs.hpcdata.experiment.metric.format;

import java.text.DecimalFormat;

/*****************************************************************************
 * 
 * Style of the metric value or annotation
 *
 *****************************************************************************/
public class FormatStyle 
{
	//////////////////////////////////////////////////////////////////////////
	//PUBLIC CONSTANTS						//
	//////////////////////////////////////////////////////////////////////////

	/** Indicates that a number should be displayed in fixed point format. */
	final public static int FIXED = 1;

	/** Indicates that a number should be displayed in floating point ("scientific") format. */
	final public static int FLOAT = 2;


	//////////////////////////////////////////////////////////////////////////
	//PUBLIC ATTRIBUTES						//
	//////////////////////////////////////////////////////////////////////////

	/** The kind of numeric display to be used, either FIXED or FLOAT. */
	public int kind;

	/** The number of characters to be used for the number. */
	public int fieldWidth;

	/** The number of digits to be used for the fractional part. */
	public int fractionDigits;

	/** Whether to show the actual value. */
	public boolean show;

	/** A Java formatter implementing the format specified for actual values. */
	public DecimalFormat formatter;
	
	/** A user-defined pattern **/
	public String pattern;
}

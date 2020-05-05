/////////////////////////////////////////////////////////////////////////////
//									   //
//	MetricValueFormat.java						   //
//									   //
//	experiment.MetricValueFormat -- a value of a metric at some scope  //
//	Last edited: January 15, 2002 at 12:37 am			   //
//									   //
//	(c) Copyright 2002 Rice University. All rights reserved.	   //
//									   //
/////////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.metric.format;


import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.util.*;
import edu.rice.cs.hpc.data.util.string.StringUtil;

import java.text.DecimalFormat;



//////////////////////////////////////////////////////////////////////////
//	CLASS METRIC-VALUE-FORMAT					//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * The format used to display values of a metric.
 *
 */


public class MetricValueFormat implements IMetricValueFormat
{
/** The number format to be used for the actual value. */
protected FormatStyle valueStyle;

/** The number format to be used for the annotation value. */
protected FormatStyle annotationStyle;

/** The pattern to use when formatting annotation values. */
//protected String annotationFormatPattern;

/** How many space characters separate the metric value and its annotation. */
protected int separatorWidth;

/** A sequence of spaces used to separate the metric value and its annotation. */
protected String separator;





//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a fully specified format.
 ************************************************************************/
	
public MetricValueFormat(boolean showValue,
						 int valueKind,
						 int valueFieldWidth,
						 int valueFractionDigits,
						 boolean showAnnotation,
						 int annotationKind,
						 int annotationFieldWidth,
						 int annotationFractionDigits,
						 String annotationFormatPattern,
						 int separatorWidth)
{
	// creation arguments
	this.valueStyle = new FormatStyle();
	this.valueStyle.show = showValue;
	this.valueStyle.kind = valueKind;
	this.valueStyle.fieldWidth = valueFieldWidth;
	this.valueStyle.fractionDigits = valueFractionDigits;
	
	this.annotationStyle = new FormatStyle();
	this.annotationStyle.show = showAnnotation;
	this.annotationStyle.kind = annotationKind;
	this.annotationStyle.fieldWidth = annotationFieldWidth;
	this.annotationStyle.fractionDigits = annotationFractionDigits;
	if (annotationFormatPattern == null) {
		annotationFormatPattern = "#0.0%";			// need to have something so default to what is used for percent values.
	}
	this.annotationStyle.pattern = annotationFormatPattern;
	//this.annotationFormatPattern = annotationFormatPattern;
	
	this.separatorWidth = separatorWidth;
	
	// Java formatters are initialized lazily
	this.clearFormatters();
}

public MetricValueFormat(FormatStyle value, FormatStyle annotation, int separatorWidth)
{
	this.valueStyle = value;
	this.annotationStyle = annotation;
	this.separatorWidth = separatorWidth;
	this.clearFormatters();
}

public MetricValueFormat(FormatStyle value)
{
	this(value, null, 0);
}

//////////////////////////////////////////////////////////////////////////
//	ACCESS TO FORMAT													//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Sets whether to show the actual value.
 ************************************************************************/
	
public void setShowValue(boolean showValue)
{
	this.valueStyle.show = showValue;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns whether to show the actual value.
 ************************************************************************/
	
public boolean getShowValue()
{
	return this.valueStyle.show;
}




/*************************************************************************
 *	Sets the kind of numeric display to be used for the actual value.
 *	
 *	@param kind		either <code>MetricValueFormat.FIXED</code> or
 *					<code>MetricValueFormat.FLOAT</code>
 *
 ************************************************************************/
	
public void setValueKind(int kind)
{
	this.valueStyle.kind = kind;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns the kind of numeric display to be used for the actual value.
 *	
 *	@return		either <code>MetricValueFormat.FIXED</code> or
 *				<code>MetricValueFormat.FLOAT</code>
 *
 ************************************************************************/
	
public int getValueKind()
{
	return this.valueStyle.kind;
}




/*************************************************************************
 *	Sets the total number of characters to be used for the actual value.
 ************************************************************************/
	
public void setValueFieldWidth(int fieldWidth)
{
	this.valueStyle.fieldWidth = fieldWidth;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns the total number of characters to be used for the actual value.
 ************************************************************************/
	
public int getValueFieldWidth()
{
	return this.valueStyle.fieldWidth;
}




/*************************************************************************
 *	Sets the number of digits to be used for the fractional part of the
 *	actual value.
 ************************************************************************/
	
public void setValueFractionDigits(int fractionDigits)
{
	this.valueStyle.fractionDigits = fractionDigits;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns the number of digits to be used for the fractional part of the
 *	actual value.
 ************************************************************************/
	
public int getValueFractionDigits()
{
	return this.valueStyle.fractionDigits;
}




/*************************************************************************
 *	Sets whether to show the metrics annotation.
 ************************************************************************/
	
public void setShowAnnotation(boolean showAnnotation)
{
	this.annotationStyle.show = showAnnotation;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns whether to show the metrics annotation.
 ************************************************************************/
	
public boolean getShowAnnotation()
{
	return this.annotationStyle.show;
}




/*************************************************************************
 *	Sets the kind of numeric display to be used with a metrics annotation.
 *	
 *	@param kind		either <code>MetricValueFormat.FIXED</code> or
 *					<code>MetricValueFormat.FLOAT</code>
 *
 ************************************************************************/
	
public void setAnnotationKind(int kind)
{
	this.annotationStyle.kind = kind;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns the kind of numeric display to be used with a metrics annotation.
 *	
 *	@return		either <code>MetricValueFormat.FIXED</code> or
 *				<code>MetricValueFormat.FLOAT</code>
 *
 ************************************************************************/
	
public int getAnnotationKind()
{
	return this.annotationStyle.kind;
}




/*************************************************************************
 *	Sets the total number of characters to be used for the metrics annotation.
 ************************************************************************/
	
public void setAnnotationFieldWidth(int fieldWidth)
{
	this.annotationStyle.fieldWidth = fieldWidth;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns the total number of characters to be used for the metrics annotation.
 ************************************************************************/
	
public int getAnnotationFieldWidth()
{
	return this.annotationStyle.fieldWidth;
}




/*************************************************************************
 *	Sets the number of digits to be used for the fractional part of the
 *	metrics annotation.
 ************************************************************************/
	
public void setAnnotationFractionDigits(int fractionDigits)
{
	this.annotationStyle.fractionDigits = fractionDigits;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns the number of digits to be used for the fractional part of the
 *	metrics annotation.
 ************************************************************************/
	
public int getAnnotationFractionDigits()
{
	return this.annotationStyle.fractionDigits;
}




/*************************************************************************
 *	Sets the number of space characters to separate the metric value and
 *	its annotation.
 ************************************************************************/
	
public void setSeparatorWidth(int separatorWidth)
{
	this.separatorWidth = separatorWidth;
	this.clearFormatters();
}




/*************************************************************************
 *	Returns the number of space characters to separate the metric
 *	value and its annotation.
 ************************************************************************/
	
public int getSeparatorWidth()
{
	return this.separatorWidth;
}




//////////////////////////////////////////////////////////////////////////
//	FORMATTING															//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the number of characters in a metric value formatted by this format.
 ************************************************************************/
	
public int getFormattedLength()
{
	int width1 = (this.valueStyle.show ? this.valueStyle.fieldWidth : 0);
	int width2 = (this.annotationStyle.show ? this.annotationStyle.fieldWidth : 0);
	int width3 = (this.valueStyle.show && this.annotationStyle.show ? this.separatorWidth : 0);
	return width1 + width2 + width3 + 1;	// +1 for trailing space
}


/**
 * format the value without the information from MetricValue. This
 * method is need to compute the derived metrics on the fly without
 * instantiating or creating new class which will consume more memory
 * (I guess ---laks).
 * @param value
 * @return <code>String</code> the text format.
 *//*
public String format(double value) {
	StringBuffer formatted = new StringBuffer();
	String string = this.formatDouble(value, this.valueFormatter, this.valueStyle);
	formatted.append(string);
	formatted.append(Util.spaces(this.annotationStyle.fieldWidth));
	return formatted.toString();
}*/
/*************************************************************************
 *	Returns a <code>String</code> showing a given <code>MetricValue</code>
 *	according to this format.
 ************************************************************************/
	
public String format(MetricValue value)
{
	this.ensureFormatters();
	StringBuffer formatted = new StringBuffer();
	
	// append formatted actual value if wanted
	if( this.valueStyle.show )
	{
		double number = MetricValue.getValue(value);
		String string = this.formatDouble(number, this.valueStyle.formatter, this.valueStyle);
		formatted.append(string);
	}
	
	// append separating spaces if needed
	if( this.valueStyle.show && this.annotationStyle.show )
		formatted.append(this.separator);
	
	// append formatted annotation if wanted
	if( this.annotationStyle.show )
	{
		if( MetricValue.isAnnotationAvailable(value) )
		{
			float number = MetricValue.getAnnotationValue(value);

			// if the formatter pattern is set for percent values, we need to handle special values differently
			if (annotationStyle.pattern.contains("%")) {
			//if (this.annotationFormatPattern.contains("%")) {
				// if value shows this used all of it, show that without decimal places
				if (Float.compare(number, 1.0f)==0) {
					formatted.append("100 %");
					return formatted.toString();
				}

				// if value shows a small negative number, show a percent of zero
				if ( (number > -0.0001) && (number < 0.0) ) {
					// Laks 2009.02.12: dirty hack to solve the problem when a small negative percentage occurs
					// instead of displaying -0.0% we force to display 0.0%
					// a better solution is by defining the proper pattern. But so far I don't see any good solution
					// 	this hack should be a temporary fix !
					formatted.append(" 0.0%");
					return formatted.toString();
				}
			}

			// not a value that needs special treatment, just format with the specified pattern
			String string = this.formatDouble(number, this.annotationStyle.formatter, this.annotationStyle);
			formatted.append(string);
			return formatted.toString();
		}

		formatted.append(StringUtil.spaces(this.annotationStyle.fieldWidth));
	}
	
	return formatted.toString();
}




/*************************************************************************
 *	Returns a <code>String</code> showing a given <code>MetricValue</code>
 *	according to this format.
 ************************************************************************/
	
protected String formatDouble(double d, DecimalFormat formatter, FormatStyle style)
{
	int kind = style.kind;
	int fieldWidth = style.fieldWidth;
	String s;
	
	if( kind == FormatStyle.FLOAT )
	{
		// hpcrun can generate incorrect metrics which are incredibly huge 
		// converted in Java it becomes infinity
		
		if (Double.compare(d, Float.POSITIVE_INFINITY) == 0.0)
			return "" ;
					
		int exponent = 0;
		// laks: if d = 9.999, the formatter will force to round it to 10.00
		// 	since I don't know how to prevent the rounding, let make a dirty solution here
		// Laks 2009.02.12: turn it back to the original format. Previously: > 9.5
		// Laks 2009.02.13: bug fix for displaying 9.9 into 1.0e+01 instead of 10.0
		while ( Math.abs(d) > 9.5 )//Laks 2008.09.03 fix previously ( Math.abs(d) >= 10.0 )
		{
			d /= 10.0;
			exponent += 1;
		}
		if (Double.compare(d, 0.0d) != 0) {
			// Laks 2009.02.12: turn it back to the original format. Previously: < 9.5
			// Laks 2009.02.13: bug fix for displaying .999x into 1.0e00 and .99x into 9.9x
			// FIXME this is an ugly bug fix, but since the formatter is handled by jvm, we have to hack from here
			while( Math.abs(d) <= 0.999 )//laks 2008.09.03 fix, previously ( Math.abs(d) < 1.0 )
			{
				d *= 10.0;
				exponent -= 1;
			}
		}
		String e = Integer.toString(Math.abs(exponent));
		if( e.length() == 1 ) e = "0" + e;
		s = formatter.format(d) + "e";
		s = s + ((exponent < 0) ? "-" : "+") + e;
	}
	else
		s = StringUtil.formatDouble(d, formatter, fieldWidth);

	return s;
}




/*************************************************************************
 *	Removes outdated Java <code>DecimalFormat</code> objects.
 *
 *	New ones will be created when needed.
 ************************************************************************/
	
protected void clearFormatters()
{
	this.valueStyle.formatter   = null;
	this.annotationStyle.formatter = null;
	this.separator        = null;
}




/*************************************************************************
 *	Creates Java <code>DecimalFormat</code> objects if necessary.
 ************************************************************************/
	
protected void ensureFormatters()
{
	// value formatter
	if( this.valueStyle.formatter == null )
	{
		String pattern = "0.";

		// use the number of fractional digits to craft the pattern
		int decdigits = getValueFractionDigits();
		while (decdigits-- > 0) {
		    pattern = pattern + "0";
		}

		this.valueStyle.formatter = Util.makeDecimalFormatter(pattern);
	}
	
	// annotation formatter
	if( this.annotationStyle.formatter == null )
	{
		this.annotationStyle.formatter = Util.makeDecimalFormatter(annotationStyle.pattern); 
				// Util.makeDecimalFormatter(this.annotationFormatPattern);
	}
	
	// separation between values
	this.separator = StringUtil.spaces(this.separatorWidth);
}




}

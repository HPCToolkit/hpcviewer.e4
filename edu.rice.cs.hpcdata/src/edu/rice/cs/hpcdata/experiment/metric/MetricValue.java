//////////////////////////////////////////////////////////////////////////
//																		//
//	MetricValue.java													//
//																		//
//	experiment.MetricValue -- a value of a metric at some scope			//
//	Last edited: September 14, 2001 at 4:47 pm							//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.metric;


import edu.rice.cs.hpcdata.util.*;




//////////////////////////////////////////////////////////////////////////
//	CLASS METRIC-VALUE													//
//////////////////////////////////////////////////////////////////////////

/**
 *
 * A value of a metric at some scope.
 *
 */


public final class MetricValue 
{

	/** The actual value if available. */
	protected float value;

	protected byte flags;

	protected static final byte VALUE_IS_AVAILABLE = 1;

	/** The distinguished metric value indicating no data. */
	public static final MetricValue NONE = new MetricValue(-1);




	//////////////////////////////////////////////////////////////////////////
	//	INITIALIZATION														//
	//////////////////////////////////////////////////////////////////////////




	/*************************************************************************
	 *	Creates an unavailable MetricValue.
	 ************************************************************************/

	public MetricValue()
	{
		setAvailable(this, false);
	}


	/*************************************************************************
	 *	Creates an available MetricValue with a given value.
	 ************************************************************************/

	public MetricValue(double value)
	{
		setValue(value);
		setAvailable(this, true);
	}



	//////////////////////////////////////////////////////////////////////////
	//	ACCESS TO VALUE														//
	//////////////////////////////////////////////////////////////////////////

	/*************************************************************************
	 *	Returns whether the actual value is available.
	 ************************************************************************/

	private static boolean getAvailable(MetricValue m)
	{
		boolean available = (m.flags & VALUE_IS_AVAILABLE) == VALUE_IS_AVAILABLE;
		return available;
	}

	private static void setAvailable(MetricValue m, boolean status)
	{
		if (status) {
			m.flags |= VALUE_IS_AVAILABLE;
		} else {
			m.flags &= ~VALUE_IS_AVAILABLE;
		}	
	}



	public float getValue()
	{
		boolean available = (flags & VALUE_IS_AVAILABLE) == VALUE_IS_AVAILABLE;
		Dialogs.Assert(available, "MetricValue::getValue");
		return this.value;
	}

	/*************************************************************************
	 *	Returns whether the metric value is available.
	 ************************************************************************/

	public static boolean isAvailable(MetricValue m)
	{
		return ( (m != MetricValue.NONE) && getAvailable(m) && 
				!Float.isNaN(m.value)    && Float.compare(m.value, 0)!=0 );
	}




	/*************************************************************************
	 *	Returns the actual value if available.
	 ************************************************************************/

	public static float getValue(MetricValue m)
	{
		return m.getValue();
	}

	
	/*************************************************************************
	 *	Makes the given actual value available.
	 ************************************************************************/

	public void setValue(double value) 
	{
		setAvailable(this, true);
		this.value = (float) value;
	}

	
	public static boolean isZero(MetricValue m) 
	{
		if (m != MetricValue.NONE) {
			return ( Double.compare(0.0, m.value) == 0 );
		}
		return true;
	}

	/*************************************************************************
	 *	Compares the metric value to another one.
	 *
	 *	Unavailable or nonexistent values are treated as less than available values.
	 *	Annotation values are ignored.
	 *
	 ************************************************************************/

	public static int compareTo(MetricValue left, MetricValue right)
	{
		int result;

		if( MetricValue.isAvailable(left) && MetricValue.isAvailable(right) )
		{
			if( left.value > right.value )
				result = +1;
			else if( left.value < right.value )
				result = -1;
			else
				result = 0;
		}
		else if( MetricValue.isAvailable(left) )
			result = +1;
		else if( MetricValue.isAvailable(right) )
			result = -1;
		else
			result = 0;

		return result;
	}


	public String toString()
	{
		return String.valueOf(value);
	}

	/*****
	 * return a new copy of metric value 
	 * 
	 * @return
	 */
	public MetricValue duplicate()
	{
		MetricValue mv = new MetricValue(value);
		mv.flags = flags;
		return mv;
	}
}

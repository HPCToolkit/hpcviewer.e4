package edu.rice.cs.hpc.data.util.string;

import java.text.DecimalFormat;
import org.apache.commons.text.WordUtils;


import edu.rice.cs.hpc.data.util.Dialogs;

public class StringUtil 
{

	/** An array of space characters used to left-pad the output of <code>DecimalFormat</code>. */
	protected static final String SPACES = "                                        ";

	
	// Wrap a Scope name to provide a more pleasing string for a tool tip. Specifically, 
	// whenever a tool tip line exceeds the specified target length, wrap it to a new
	// line following the next embedded space.
	static public String wrapScopeName(String s, int desiredLineLength) {
		return WordUtils.wrap(s, desiredLineLength, "\n", true, "");
	}
	

	//////////////////////////////////////////////////////////////////////////
	//TEXT FORMATTING														//
	//////////////////////////////////////////////////////////////////////////





	/*************************************************************************
	 *	Fits a string into a field of given width, right adjusted.
	 ************************************************************************/

	public static String rightJustifiedField(String s, int fieldWidth)
	{
		String field;

		int padLeft = fieldWidth - s.length();
		if( padLeft > 0 )
			field = spaces(padLeft) + s;
		else
			field = s.substring(0, fieldWidth);

		return field;
	}



	/*************************************************************************
	 *	Returns a <code>String</code> of a given number of space characters.
	 ************************************************************************/

	public static String spaces(int count)
	{
		Dialogs.Assert(count <= SPACES.length(), "request too long Util::spaces");

		return SPACES.substring(0, count);
	}


	/*************************************************************************
	 *	Formats an <code>int</code> in a given format and field width.
	 *
	 *	TODO: It might be possible to improve this method's implementation by
	 *	using class <code>java.text.FieldPosition</code>.
	 *
	 ************************************************************************/

	public static String formatInt(int n, DecimalFormat formatter, int fieldWidth)
	{
		return 	rightJustifiedField(formatter.format(n), fieldWidth);
	}




	/*************************************************************************
	 *	Formats a <code>double</code> in a given format and field width.
	 *
	 *	TODO: It might be possible to improve this method's implementation by
	 *	using class <code>java.text.FieldPosition</code>.
	 *
	 ************************************************************************/

	public static String formatDouble(double d, DecimalFormat formatter, int fieldWidth)
	{
		return rightJustifiedField(formatter.format(d), fieldWidth);
	}



}

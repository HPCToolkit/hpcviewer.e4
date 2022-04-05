package edu.rice.cs.hpcdata.util.string;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtil 
{

	/** An array of space characters used to left-pad the output of <code>DecimalFormat</code>. */
	protected static final String SPACES = "                                        ";
	private static final Pattern patternToWrapOn = Pattern.compile(" ");

	
	// Wrap a Scope name to provide a more pleasing string for a tool tip. Specifically, 
	// whenever a tool tip line exceeds the specified target length, wrap it to a new
	// line following the next embedded space.
	static public String wrapScopeName(String s, int desiredLineLength) {
		return wrap(s, desiredLineLength);
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
		assert count <= SPACES.length() : "request too long Util::spaces";

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


	/*************************************************************************
	 * Wrapping a long text or word into multiple lines
	 * Inspired from Apache WordUtils.wrap() method.
	 * 
	 * @param str 
	 * 			text to wrap
	 * @param wrapLength
	 * 			maximum length of text allowed. Anything more than {@code wrapLength} will be wrapped.
	 * @return {@code String}
	 * 			Wrapped text
	 *************************************************************************/
    private static String wrap(final String str,
            				   int wrapLength) {
    	
    	if (str == null) {
    		return null;
    	}
    	String newLineStr = System.lineSeparator();

    	if (wrapLength < 1) {
    		wrapLength = 1;
    	}

    	final int inputLineLength = str.length();
    	int offset = 0;
    	final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);
    	int matcherSize = -1;

    	while (offset < inputLineLength) {
    		int spaceToWrapAt = -1;
    		Matcher matcher = patternToWrapOn.matcher(str.substring(offset,
    				Math.min((int) Math.min(Integer.MAX_VALUE, offset + wrapLength + 1L), inputLineLength)));
    		if (matcher.find()) {
    			if (matcher.start() == 0) {
    				matcherSize = matcher.end();
    				if (matcherSize != 0) {
    					offset += matcher.end();
    					continue;
    				}
    				offset += 1;
    			}
    			spaceToWrapAt = matcher.start() + offset;
    		}

    		// only last line without leading spaces is left
    		if (inputLineLength - offset <= wrapLength) {
    			break;
    		}

    		while (matcher.find()) {
    			spaceToWrapAt = matcher.start() + offset;
    		}

    		if (spaceToWrapAt >= offset) {
    			// normal case
    			wrappedLine.append(str, offset, spaceToWrapAt);
    			wrappedLine.append(newLineStr);
    			offset = spaceToWrapAt + 1;

    		} else {
    			// really long word or URL
    			if (matcherSize == 0) {
    				offset--;
    			}
    			// wrap really long word one line at a time
    			wrappedLine.append(str, offset, wrapLength + offset);
    			wrappedLine.append(newLineStr);
    			offset += wrapLength;
    			matcherSize = -1;
    		}
    	}

    	if (matcherSize == 0 && offset < inputLineLength) {
    		offset--;
    	}

    	// Whatever is left in line is short enough to just pass through
    	wrappedLine.append(str, offset, str.length());

    	return wrappedLine.toString();
    }


}

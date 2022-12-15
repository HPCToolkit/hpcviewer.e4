package edu.rice.cs.hpcdata.experiment.metric;

// Java 1.4 Compatible enumeration type
public class MetricType 
{
	public static final String SUFFIX_EXCLUSIVE = "(E)";
	public static final String SUFFIX_INCLUSIVE = "(I)";
	public static final String SUFFIX_POINT_EXC = "(X)";

	public static final MetricType UNKNOWN        = new MetricType("UNKNOWN");
	public static final MetricType EXCLUSIVE      = new MetricType("EXCLUSIVE");
	public static final MetricType INCLUSIVE      = new MetricType("INCLUSIVE");
	public static final MetricType POINT_EXCL     = new MetricType("XCLUSIVE");
	public static final MetricType LEXICAL_AWARE  = new MetricType("LEXICAL_AWARE");
	
	public String toString() { return value; }
	
	/****
	 * Convert from the name of the propagation scope to a metric type
	 * 
	 * @param scopePropagationName
	 * 			The name of scope propagation
	 * 
	 * @return MetricType
	 * 
	 * @see INCLUSIVE
	 * @see EXCLUSIVE
	 * @see POINT_EXCL
	 * @see UNKNOWN
	 */
	public static MetricType convertFromPropagationScope(String scopePropagationName) {
		if (scopePropagationName.equalsIgnoreCase("execution")) 
			return MetricType.INCLUSIVE;
		else if (scopePropagationName.equalsIgnoreCase("function")) 				 
			return MetricType.EXCLUSIVE;
		else if (scopePropagationName.equalsIgnoreCase("lex_aware"))
			return MetricType.LEXICAL_AWARE;
		else if (scopePropagationName.equalsIgnoreCase("point")) 
			return MetricType.POINT_EXCL;
		else
			return MetricType.UNKNOWN;		
	}
	
	
	public static MetricType convertFromPropagationScope(int scopePropagationType) {
		switch(scopePropagationType) {
		case 1:
			return MetricType.POINT_EXCL;
		case 2:
			return MetricType.INCLUSIVE;
		case 3:
			return MetricType.EXCLUSIVE;
		default:
			return MetricType.UNKNOWN;
		}
	}
	
	public static MetricType convertFromName(String formulaType) {
		if (formulaType.equalsIgnoreCase("inclusive")) {
			return MetricType.INCLUSIVE;
		} else if (formulaType.equalsIgnoreCase("exclusive")) {
			return MetricType.EXCLUSIVE;
		}
		throw new IllegalArgumentException("unknown formula type: " + formulaType);
	}
	
	
	public String getSuffix() {
		if (this == MetricType.EXCLUSIVE || 
			this == MetricType.LEXICAL_AWARE)
			return(SUFFIX_EXCLUSIVE);
			
		if (this == MetricType.INCLUSIVE)
			return(SUFFIX_INCLUSIVE);
			
		if (this == MetricType.POINT_EXCL)
			return(SUFFIX_POINT_EXC);

		return "";
	}
	
	public boolean isExclusive() {
		return this == EXCLUSIVE || this == LEXICAL_AWARE;
	}
	
	private String value;
	private MetricType(String value) { this.value = value; }
}
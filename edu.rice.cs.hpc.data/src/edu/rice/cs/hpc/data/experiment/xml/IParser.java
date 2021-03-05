package edu.rice.cs.hpc.data.experiment.xml;

public interface IParser 
{
	/*************************************************************************
	 *	Parses the XML file sending parse actions to the builder.
	 * @throws Exception 
	 ************************************************************************/
	public void parse(String filename) throws Exception;
	
	/*************************************************************************
	 *	Returns the 1-based line number of the most recently read input line.
	 *
	 *	<p>
	 *	We'd like this to be the line at which a syntax error was detected,
	 *	but the lean parser we're using does not keep track of that. If it
	 *	did this method should return <code>this.locator.getLineNumber()</code>.
	 *
	 ************************************************************************/
	public int getLineNumber();
}

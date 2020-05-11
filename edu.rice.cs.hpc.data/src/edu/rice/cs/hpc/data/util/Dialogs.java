//////////////////////////////////////////////////////////////////////////
//																		//
//	Dialogs.java														//
//																		//
//	util.Dialogs -- generally useful stuff								//
//	Last edited: January 14, 2002 at 6:31 pm							//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.util;





//////////////////////////////////////////////////////////////////////////
//	CLASS DIALOGS														//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * Convenience class for running common dialogs.
 *
 */


public class Dialogs
{


/** The frame to be used as parent for dialogs. */
//private static JFrame parent;

/** Whether to display "temporary method" warning dialogs. */
private static boolean warnTempMethods = false;




/*************************************************************************
 *	Sets the frame to be used as parent for dialogs.
 ************************************************************************/
/*
public static void setParent(JFrame p)
{
	Dialogs.parent = p;
}

*/


/*************************************************************************
 *	Runs an error dialog if an Assertion fails.
 *
 *	@param	predicate	The result of evaluating the Asserted predicate.
 *
 ************************************************************************/

public static void Assert(boolean predicate)
{
	if( ! predicate )  Dialogs.fail("Assertion failed");
}

public static void Assert(boolean predicate, String msg)
{
	if( ! predicate )  Dialogs.fail("Assertion failed: " + msg );
}



/*************************************************************************
 *	Runs a standard file chooser dialog.
 *
 *	@return		The chosen file, or <code>null</code> if the user cancels.
 *
 ************************************************************************/
/*
public static File chooseFile()
{
	File file;

	JFileChooser chooser = new JFileChooser();
	chooser.setCurrentDirectory(Dialogs.lastDirectory);
	
	int result = chooser.showOpenDialog(null);
	Dialogs.lastDirectory = chooser.getCurrentDirectory();

	if( result == JFileChooser.APPROVE_OPTION )
		file = chooser.getSelectedFile();
	else
		file = null;

	return file;
}

*/


/*************************************************************************
 *	Runs a dialog for a notification message.
 *
 *	@param	message		The notification message text.
 *
 ************************************************************************/

public static void notify(String message)
{
	String[] lines   = new String[] { message };
	Dialogs.message(lines, false, false);
}




/*************************************************************************
 *	Runs a dialog for a notification message.
 *
 *	@param	message		The notification message text.
 *	@param	what		A string to be appended to the message text.
 *
 ************************************************************************/

public static void notify(String message, String what)
{
	String[] lines   = new String[] { message, what };
	Dialogs.message(lines, false, false);
}




/*************************************************************************
 *	Runs a dialog for a nonfatal exception.
 *
 *	@param	message		The error message text.
 *	@param	what		A string to be appended to the message text.
 *	@param	ex			The exception to be described by the dialog.
 *
 ************************************************************************/

public static void exception(String message, String what, Exception e)
{
	Dialogs.exceptionMessage(message, what, e, false);
}




/*************************************************************************
 *	Runs a dialog for a fatal exception.
 *
 *	@param	message		The error message text.
 *	@param	what		A string to be appended to the message text.
 *	@param	ex			The exception to be described by the dialog.
 *
 ************************************************************************/

public static void fatalException(String message, String what, Exception e)
{
	Dialogs.exceptionMessage(message, what, e, true);
}




/*************************************************************************
 *	Runs an info dialog for a method whose implementation is temporary.
 *	<p>
 *
 *	The dialog is only shown if debugging variable <code>warnTempMethods</code>
 *	is set to true.
 *
 *	@param	method		The name of the unimplemented method.
 *
 ************************************************************************/

public static void temporary(String method)
{
	if( Dialogs.warnTempMethods )
	{
		String[] lines = new String[] { Strings.TEMPORARY_METHOD + ":", method };
		Dialogs.message(lines, false, true);
	}
}




/*************************************************************************
 *	Runs an error dialog for an unimplemented method.
 *
 *	@param	method		The name of the unimplemented method.
 *
 ************************************************************************/

public static void notImplemented(String method)
{
	String[] lines = new String[] { Strings.NOT_IMPLEMENTED + ":", method };
	Dialogs.message(lines, true, true);
}




/*************************************************************************
 *	Runs a warning-only dialog for an unimplemented method.
 *
 *	@param	method		The name of the unimplemented method.
 *
 ************************************************************************/

public static void notImplementedWarning(String method)
{
	String[] lines = new String[] { Strings.NOT_IMPLEMENTED + ":", method };
	Dialogs.message(lines, false, true);
}




/*************************************************************************
 *	Runs an error dialog for a method that must be overridden by subclasses.
 *
 *	@param	method		The name of the method that should have been overridden.
 *
 ************************************************************************/

public static void subclassResponsibility(String method)
{
	String[] lines = new String[] { Strings.SUBCLASS_RESPONSIBILITY + ":", method };
	Dialogs.message(lines, true, true);
}




/*************************************************************************
 *	Runs an error dialog for a method that must not be called.
 *
 *	@param	method		The name of the method that should not have been called.
 *
 ************************************************************************/

public static void notCalled(String method)
{
	String[] lines = new String[] { Strings.NOT_CALLED + ":", method };
	Dialogs.message(lines, true, true);
}




/*************************************************************************
 *	Runs a fatal error dialog and aborts the application.
 *
 *	@param	why			The explanation of what failure has occurred.
 *
 ************************************************************************/

public static void fail(String why)
{
	String[] lines = new String[] { Strings.FATAL_ERROR + ":", why };
	Dialogs.message(lines, true, true);
}




/*************************************************************************
 *	Runs a fatal error dialog and aborts the application.
 *
 *	@param	why			The explanation of what failure has occurred.
 *	@param	what		A string to be appended to the message text.
 *
 ************************************************************************/

public static void fail2(String why, String what)
{
	String[] lines = new String[] { Strings.FATAL_ERROR + ":", why + " (" + what + ")" };
	Dialogs.message(lines, true, true);
}




/*************************************************************************
 *	Runs a dialog for an exception.
 *
 *	@param	message		The error message text.
 *	@param	what		A string to be appended to the message text.
 *	@param	ex			The exception to be described by the dialog.
 *	@param	fatal		Whether execution should be aborted after this dialog.
 *
 ************************************************************************/

private static void exceptionMessage(String message, String what, Exception e, boolean fatal)
{
	e.printStackTrace();

	String errorKind = e.getClass() + "";
	String errorWhat = e.getMessage();
	String[] lines   = new String[] { message, what, " ", errorKind, errorWhat };
	Dialogs.message(lines, true, fatal);
}




/*************************************************************************
 *	Runs a warning or fatal dialog and aborts if requested.
 ************************************************************************/

private static void message(String[] lines, boolean error, boolean bad)
{
	StringBuffer sb = new StringBuffer();
	for (int i=0; i<lines.length; i++) {
		sb.append(lines[i]);
		sb.append(" ");
	}
	System.err.println(sb.toString());
	// Laks: it is better not to have any "costly" statement here 
//	String title = Strings.APPNAME + " " + (error ? Strings.ERROR : (bad ? Strings.WARNING : Strings.MESSAGE));
//	int kind = (error ? JOptionPane.ERROR_MESSAGE : (bad ? JOptionPane.WARNING_MESSAGE : JOptionPane.PLAIN_MESSAGE));

//	JOptionPane.showMessageDialog(Dialogs.parent, lines, title, kind);

//	if( (error & bad) )
//		HPCViewerApplication.getCurrentApp().abort();
}




}

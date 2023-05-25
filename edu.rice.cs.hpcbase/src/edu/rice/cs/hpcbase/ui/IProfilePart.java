package edu.rice.cs.hpcbase.ui;


public interface IProfilePart extends IMainPart 
{

	/***
	 * Display an editor in the top folder
	 * 
	 * @param input 
	 * 			The object input. Warning: its value can be anything.
	 * 
	 * @return {@code IUpperPart}
	 * 			The editor object if successful, {@code null} otherwise.
	 */
	IUpperPart addEditor(Object input);
	
		
	/***
	 * Create a new view part in the lower part of the program.
	 *  
	 * @param input
	 * 			The object input. Warning: its value can be anything.
	 * 		
	 * @return {@code ILowerPart}
	 * 			The view object if successful, {@code null} otherwise
	 */
	ILowerPart addView(Object input); 
}

package edu.rice.cs.hpcremote.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GenericInteractiveDialog extends TitleAreaDialog 
{
	private final String name;
	private final String instruction;
	private final String []prompts;
	private final int    []types;
	private Text []answers;
	
	
	/*** output variable based on user's input. 
	 *   The caller needs to retrieve this variable once the user clicks OK button
	 *   **/
	private String []inputs;
	
	public GenericInteractiveDialog(Shell parentShell, 
									String name,
									String instruction, 
									String[] prompt,
									int[] types) {
		super(parentShell);
		this.name 		 = name;
		this.instruction = instruction;
		this.prompts 	 = prompt;
		this.types  	 = types;
	}
	
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	public void create()
	{
		super.create();
		setTitle(name);
		setMessage(instruction);
	}
	
	
	public String[] getInputs() 
	{
		return inputs;
	}
	
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
		
		answers = new Text[prompts.length];
		
		for(int i=0; i<prompts.length; i++) 
		{
			Label lbl = new Label(container, SWT.WRAP | SWT.LEFT);
			lbl.setText(prompts[i]);
			
			answers[i] = new Text(container, types[i]);
			answers[i].setText("");
			
			GridDataFactory.fillDefaults().grab(true, false).applyTo(answers[i]);
		}
		return area;
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed()
	{
		inputs = new String[answers.length];
		
		for(int i=0; i<answers.length; i++)
		{
			inputs[i] = answers[i].getText();
		}
		super.okPressed();
	}
}

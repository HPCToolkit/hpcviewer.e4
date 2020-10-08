package edu.rice.cs.hpctraceviewer.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.map.UserInputHistory;


/**********************************************
 * 
 * Generic class to query several inputs from a user
 *
 **********************************************/
public class MultiInputDialog extends Dialog 
{
	private final String title;
	private final String message;
	private final String []prompts;
	private UserInputHistory []histories;

	private Combo []entries;
	private String []values;

	/****
	 * Constructor
	 * 
	 * @param parentShell
	 * @param dialogTitle
	 * @param message
	 * @param prompts
	 * @param histories
	 */
	public MultiInputDialog(Shell parentShell, String dialogTitle,
			String message, String []prompts, String []histories) {
		
		super(parentShell);
		this.message = message;
		this.prompts = prompts;
		this.title   = dialogTitle;
		
		this.histories = new UserInputHistory[histories.length];
		
		for (int i=0; i<histories.length; i++) {
			String history = histories[i];
			this.histories[i] = new UserInputHistory(history);
		}
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(10, 10, 10, 10).generateLayout(composite);

		Label lblMessage = new Label(composite, SWT.NONE);
		lblMessage.setText(message);
		
		Group fieldArea = new Group(composite, SWT.SHADOW_IN);
		
		entries = new Combo[prompts.length];
		
		for(int i=0; i<prompts.length; i++) {
			
			String text = prompts[i];
			Label firstLabel = new Label(fieldArea, SWT.NONE);
			firstLabel.setText(text);
			entries[i] = new Combo(fieldArea, SWT.SINGLE | SWT.BORDER);
			
			if (histories !=null && i<histories.length)
				entries[i].setItems(histories[i].getHistory());
		}
		GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(2, 4, 3, 5).generateLayout(fieldArea);
		
		getShell().setText(title);
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		
		values = new String[entries.length];
		
		for(int i=0; i<entries.length; i++) {
			values[i] = entries[i].getText();
			
			if (histories != null && i<histories.length)
				histories[i].addLine(values[i]);
		}
		super.okPressed();
	}
	
	/****
	 * Get the user's multiple values
	 * @return
	 */
	public String[] getValue() {
		return values;
	}
	
	
	/***
	 * Unit test
	 * @param argv
	 */
	static public void main(String []argv) {
		Shell shell = new Shell();
		
		final int numPrompts = 5;
		String []prompts = new String[numPrompts];
		
		for (int i=0; i< numPrompts; i++) {
			prompts[i] = "Prompt #" + i;
		}
		
		MultiInputDialog dlg = new MultiInputDialog(shell, "Test dialog multi", "My message to the world", prompts, new String[0]);
		
		if (dlg.open() == Dialog.OK) {
			
			String []values = dlg.getValue();
			
			for(int i=0; i<values.length; i++) {
				System.out.println(prompts[i] + " : " + values[i]);
			}
		}
	}
}

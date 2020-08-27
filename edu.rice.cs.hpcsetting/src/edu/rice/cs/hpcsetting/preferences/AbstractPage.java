package edu.rice.cs.hpcsetting.preferences;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;



/*******************************************************
 * 
 * Abstract preference page
 *
 *******************************************************/
public abstract class AbstractPage extends PreferencePage 
{

    public AbstractPage(String title) {
    	setTitle(title);
    }
    

    /**
     * Creates the group control which contains two columns for controls.
     * 
     * @param parent
     *            the parent to create the group control
     * @param text
     *            the group name
     * @param equal
     *            true if making columns equal width
     * @return the group
     */
    protected Group createGroupControl(Composite parent, String text,
            boolean equal) {
        Group group = new Group(parent, SWT.NULL);
        group.setText(text);

        group.setLayout(new GridLayout(2, equal));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return group;
    }

    /**
     * Creates the label.
     * 
     * @param parent
     *            the parent to create the label
     * @param text
     *            the label text
     * @return the label
     */
    protected Label createLabelControl(Composite parent, String text) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(text);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        return label;
    }

    /**
     * Creates the color selector.
     * 
     * @param parent
     *            the parent to create the color selector
     * @return the color selector
     */
    protected ColorSelector createColorButtonControl(Composite parent) {
        return new ColorSelector(parent);
    }

    /**
     * Creates a generic button. User needs to specify a style
     * @see SWT.CHECK SWT.RADIO
     * 
     * @param parent
     *            the parent to create the check box
     * @param label
     *            the label text
     * @param style
     * 			  the style of the button           
     *            
     * @return {@link Button} control
     */
    protected Button createButtonControl(Composite parent, String label, int style) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        composite.setLayoutData(gridData);
        composite.setLayout(new GridLayout(2, false));

        Button button = new Button(composite, style);
        GridData gridData1 = new GridData();
        gridData1.horizontalSpan = 1;
        button.setLayoutData(gridData1);

        createLabelControl(composite, label);

        return button;
    }

    /**
     * Creates the check box.
     * 
     * @param parent
     *            the parent to create the box
     * @param label
     *            the label text
     * @return {@link Button} control
     */
    protected Button[] createRadioButtonControl(Composite parent, String []labels) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        composite.setLayoutData(gridData);
        composite.setLayout(new GridLayout(2, false));
    	
    	Button []buttons = new Button[labels.length];
    	
    	for(int i=0; i<labels.length; i++) {
    		String label = labels[i];
    		
            buttons[i] = new Button(composite, SWT.RADIO);
            GridData gridData1 = new GridData();
            gridData1.horizontalSpan = 1;
            buttons[i].setLayoutData(gridData1);

            createLabelControl(composite, label);
    	}
    	return buttons;
    }


    /**
     * Creates the check box.
     * 
     * @param parent
     *            the parent to create the check box
     * @param label
     *            the label text
     * @return {@link Button} control
     */
    protected Button createCheckBoxControl(Composite parent, String label) {
        return createButtonControl(parent, label, SWT.CHECK);
    }

    /**
     * Creates the text field.
     * 
     * @param parent
     *            the parent to create the text field
     * @return the text
     */
    protected Text createTextControl(Composite parent) {
        Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.WRAP);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 1;
        text.setLayoutData(gridData);

        return text;
    }

    /**
     * Creates the combo control.
     * 
     * @param parent
     *            the parent to create the combo
     * @param items
     *            the combo items
     * @return the combo
     */
    protected Combo createComboControl(Composite parent, String[] items) {
        Combo combo = new Combo(parent, SWT.BORDER | SWT.SINGLE);
        combo.setItems(items);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 1;
        combo.setLayoutData(gridData);

        return combo;
    }

    /**
     * Creates the spinner.
     * 
     * @param parent
     *            the parent to create the spinner
     * @param min
     *            the minimum value of spinner
     * @param max
     *            the maximum value of spinner
     * @return the spinner
     */
    protected Spinner createSpinnerControl(Composite parent, int min, int max) {
        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setMinimum(min);
        spinner.setMaximum(max);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 1;
        spinner.setLayoutData(gridData);

        return spinner;
    }
	


    /**
     * Apply the values specified on controls.
     */
    abstract public void apply();

}

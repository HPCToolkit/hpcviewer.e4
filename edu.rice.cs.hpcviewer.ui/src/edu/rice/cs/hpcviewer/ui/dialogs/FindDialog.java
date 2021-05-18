package edu.rice.cs.hpcviewer.ui.dialogs;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;

public class FindDialog extends Dialog 
{
    private Text txtSearch;
    final private EPartService partService;

    public FindDialog(Shell parentShell, EPartService partService) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
        setBlockOnOpen(false);
        this.partService = partService;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        //
        // 1st row: find label and text
        //
        Label lblSearch = WidgetFactory.label(SWT.NONE)
        							   .layoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false))
        							   .create(container);
        lblSearch.setText("Find:");

        txtSearch = WidgetFactory.text(SWT.BORDER)
        						 .layoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
        						 .create(container);
        txtSearch.setText("");

        //
        // 2nd row: search button and message text
        //
        
        Button btnSearch = WidgetFactory.button(SWT.PUSH)
        								.layoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false))
        								.create(container);
        btnSearch.setText("Search");

        final Label lblMessage = WidgetFactory.label(SWT.WRAP)
        									  .layoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true))
        									  .create(container);
        lblMessage.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

        btnSearch.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
        		if (partService == null)
        			return;
        		
        		MPart part = partService.getActivePart();
        		if (part == null)
        			return;
        		
        		if (!(part.getObject() instanceof ProfilePart))
        			return;
        		
        		ProfilePart profilePart = (ProfilePart) part.getObject();
        		Editor editor = profilePart.getActiveEditor();
        		if (editor == null) 
        			return;
        		
        		if (!editor.search(txtSearch.getText())) {
        			lblMessage.setText("Text not found");
        			lblMessage.setVisible(true);
        		}
        	}
		});
 
        txtSearch.setFocus();
 
        Point size = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        container.setSize(size);
        
        getShell().setDefaultButton(btnSearch);
        getShell().setText("Find ");
        
        return container;
    }


    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
    }

    
    @Override
    protected void buttonPressed(int buttonId) {
    	okPressed();
    }
    
    @Override
    protected Point getInitialSize() {
        return new Point(350, 200);
    }
    
    public static void main(String []args) {
    	Display display = Display.getDefault();
    	FindDialog dialog = new FindDialog(display.getActiveShell(), null);
    	dialog.setBlockOnOpen(true);
    	dialog.open();
    }
}

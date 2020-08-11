package edu.rice.cs.hpcviewer.ui.dialogs;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
        GridLayout layout   = new GridLayout(2, false);
        layout.marginRight  = 5;
        layout.marginLeft   = 10;
        container.setLayout(layout);

        Label lblSearch = new Label(container, SWT.NONE);
        lblSearch.setText("Find:");

        txtSearch = new Text(container, SWT.BORDER);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtSearch.setText("");

		/*
		 * txtSearch.addModifyListener(e -> { Text textWidget = (Text) e.getSource();
		 * String userText = textWidget.getText(); });
		 */
        
        Button btnSearch = new Button(container, SWT.PUSH);
        btnSearch.setText("Search");
        
        final Label lblMessage = new Label(container, SWT.NONE);
        lblMessage.setVisible(false);
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
        return new Point(350, 150);
    }
    
    public static void main(String []args) {
    	Display display = Display.getDefault();
    	FindDialog dialog = new FindDialog(display.getActiveShell(), null);
    	dialog.setBlockOnOpen(true);
    	dialog.open();
    }
}

 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import javax.annotation.PreDestroy;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.layout.GridData;

public class TracePart {
	@Inject
	public TracePart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		SashForm sashForm_1 = new SashForm(parent, SWT.NONE);
		
		SashForm sashForm_2 = new SashForm(sashForm_1, SWT.VERTICAL);
		
		CTabFolder tabFolderTopLeft = new CTabFolder(sashForm_2, SWT.BORDER);
		tabFolderTopLeft.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmTraceView = new CTabItem(tabFolderTopLeft, SWT.NONE);
		tbtmTraceView.setText("Trace view");
		
		Composite composite = new Composite(tabFolderTopLeft, SWT.NONE);
		tbtmTraceView.setControl(composite);
		composite.setLayout(new GridLayout(2, false));
		
		Canvas canvas = new Canvas(composite, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		canvas.setBounds(0, 0, 64, 64);
		canvas.setLayout(new GridLayout(1, false));
		
		Canvas canvas_1 = new Canvas(composite, SWT.NONE);
		canvas_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		canvas_1.setBounds(0, 0, 64, 64);
		
		CTabFolder tabFolderBottomLeft = new CTabFolder(sashForm_2, SWT.BORDER | SWT.CLOSE);
		tabFolderBottomLeft.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmDepthView = new CTabItem(tabFolderBottomLeft, SWT.NONE);
		tbtmDepthView.setText("Depth view");
		sashForm_2.setWeights(new int[] {11});
		
		CTabFolder tabFolderRight = new CTabFolder(sashForm_1, SWT.BORDER);
		tabFolderRight.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmCallStack = new CTabItem(tabFolderRight, SWT.NONE);
		tbtmCallStack.setText("Call stack");
		sashForm_1.setWeights(new int[] {1, 1});		
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {
		
	}
}
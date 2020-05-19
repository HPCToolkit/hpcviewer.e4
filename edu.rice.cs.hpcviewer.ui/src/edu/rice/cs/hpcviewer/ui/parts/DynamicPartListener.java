package edu.rice.cs.hpcviewer.ui.parts;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;

public class DynamicPartListener implements IPartListener 
{
	final private IBaseView view;
	
	public DynamicPartListener(IBaseView view) {
		this.view = view;
	}
	
	@Override
	public void partVisible(MPart part) {
		System.out.println(part.getElementId()+ " visible ");
	}
	
	@Override
	public void partHidden(MPart part) {
		System.out.println(part.getElementId()+ " hidden ");
	}
	
	@Override
	public void partDeactivated(MPart part) {
		System.out.println(part.getElementId()+ " deactivated ");
	}
	
	@Override
	public void partBroughtToTop(MPart part) {
		System.out.println(part.getElementId()+ " top ");
	}
	
	@Override
	public void partActivated(MPart part) {
		System.out.println(part.getElementId()+ " active " );
	}

}

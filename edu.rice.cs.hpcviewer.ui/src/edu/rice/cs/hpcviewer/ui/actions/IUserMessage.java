package edu.rice.cs.hpcviewer.ui.actions;

public interface IUserMessage 
{
	public void showErrorMessage(String str);
	public void showInfo(String message);
	public void showWarning(String message);
}

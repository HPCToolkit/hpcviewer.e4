package edu.rice.cs.hpcbase.ui;

public interface IUserMessage 
{
	public void showErrorMessage(String str);
	public void showInfo(String message);
	public void showWarning(String message);
}

package edu.rice.cs.hpcviewer.ui.parts.editor;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IEditorInput;
import edu.rice.cs.hpcbase.ui.IUpperPart;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class EditorInputScope implements IEditorInput 
{
	private final IDatabase database;
	private final Scope scope;
	private final Shell shell;
	
	public EditorInputScope(Shell shell,IDatabase database, Scope scope) {
		this.shell = shell;
		this.scope = scope;
		this.database = database;
	}
	
	@Override
	public String getShortName() {
		return scope.getSourceFile().getName();
	}
	
	@Override
	public String getLongName() {
		var sourceFile = scope.getSourceFile();
		var path = sourceFile.getFilename().toString();
		return database.getId() + "/" + path;
	}
	
	@Override
	public String getId() {
		return getLongName();
	}
	
	@Override
	public boolean needToTrackActivatedView() {
		return false;
	}
	
	@Override
	public IUpperPart createViewer(Composite parent) {
		return new Editor((CTabFolder) parent, SWT.NONE);
	}

	@Override
	public String getContent() {
		var sourceFile = scope.getSourceFile();
		try {
			return database.getSourceFileContent(sourceFile);
		} catch (IOException e) {
			MessageDialog.openError(shell, "Fail to get the content", getLongName() + ": " + e.getMessage());
		}
		return null;
	}

	@Override
	public int getLine() {
		return scope.getFirstLineNumber();
	}

}

// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.parts.editor;

import java.io.IOException;

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
	
	public EditorInputScope(Shell shell,IDatabase database, Scope scope) {
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
			throw new IllegalAccessError(getLongName() + ": " + e.getMessage());
		}
	}

	@Override
	public int getLine() {
		return scope.getFirstLineNumber();
	}

}

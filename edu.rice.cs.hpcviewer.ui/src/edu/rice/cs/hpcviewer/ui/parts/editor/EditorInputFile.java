// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.parts.editor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IEditorInput;
import edu.rice.cs.hpcbase.ui.IUpperPart;

public class EditorInputFile implements IEditorInput 
{
	private File file;
	private Shell shell;

	public EditorInputFile(Shell shell, File file) {
		this.shell = shell;
		this.file = file;
	}
	
	@Override
	public String getShortName() {
		return file.getName();
	}
	
	@Override
	public String getLongName() {
		return file.getAbsolutePath();
	}
	
	@Override
	public String getId() {
		return file.getPath();
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
	public int getLine() {
		return 1;
	}
	
	@Override
	public String getContent() {
		Path path = Path.of(file.getAbsolutePath());
		try {
			return Files.readString(path, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			MessageDialog.openError(shell, "Error open the file", file.getAbsolutePath() + ": " + e.getMessage());
		}
		return null;
	}

}

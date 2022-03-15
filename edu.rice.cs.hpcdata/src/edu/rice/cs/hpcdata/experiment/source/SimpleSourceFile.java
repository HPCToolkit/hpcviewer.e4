package edu.rice.cs.hpcdata.experiment.source;

import java.io.File;

public class SimpleSourceFile implements SourceFile {

	private final int id;
	private final File filename;
	private final boolean available;
	
	public SimpleSourceFile(int id, File filename, boolean available) {
		this.id = id;
		this.filename = filename;
		this.available = available;
	}
	
	
	@Override
	public int getFileID() {
		return id;
	}

	@Override
	public String getName() {
		return filename.getName();
	}


	@Override
	public boolean isAvailable() {
		return available;
	}


	@Override
	public File getFilename() {
		return filename;
	}
}

package edu.rice.cs.hpcviewer.ui.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.base.IUserMessage;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.util.Utilities;

public class ExportTable 
{
    
    /**
     * Constant comma separator. Should be configurable somewhere instead of a constant
     */
    final private String COMMA_SEPARATOR = ",";
    final private String SPACE_SEPARATOR = "  ";

	final private ScopeTreeViewer treeViewer;
	final private IUserMessage    message;
	
	public ExportTable(ScopeTreeViewer viewer, IUserMessage message) {
		
		this.treeViewer = viewer;
		this.message    = message;
	}
	
	public void export() {
		
		BaseExperiment experiment = treeViewer.getExperiment();
		
		Shell shell = treeViewer.getTree().getShell();
		
		FileDialog fileDlg = new FileDialog(shell, SWT.SAVE);
		
		fileDlg.setFileName(experiment.getName() + ".csv");
		fileDlg.setFilterPath(experiment.getDefaultDirectory().getAbsolutePath());
		fileDlg.setFilterExtensions(new String [] {"*.csv", "*.*"});
		fileDlg.setText("Save the data in the table to a file (CSV format)");

		final String sFilename = fileDlg.open();
		if (sFilename == null)
			return;
		
		// -----------------------------------------------------------------------
		// Check if the status of the file
		// -----------------------------------------------------------------------
		File objFile = new File( sFilename );
		if ( objFile.exists() ) {
			if ( !MessageDialog.openConfirm( shell, "File already exists" , 
				sFilename + ": file already exist. Do you want to replace it ?") )
				return;
		}
		// WARNING: java.io.File seems always fail to verify writable status on Linux !
		/*
		if ( !objFile.canWrite() ) {
			MessageDialog.openError( shell, "Error: Unable to write the file", 
					sFilename + ": File is not writable ! Please check if you have right to write in the directory." );
			return;
		} */

		// -----------------------------------------------------------------------
		// prepare the file
		// -----------------------------------------------------------------------
		if (message != null)
			message.showInfo( "Writing to file: "+sFilename);
		
		try {
			writeContentToFile(objFile);
		} catch (IOException e) {
			MessageDialog.openError(shell, "Error writing to " + sFilename, e.getLocalizedMessage());
		}

	}

	private void writeContentToFile(File objFile) throws IOException {
		
		
		FileWriter objWriter = new FileWriter( objFile );
		BufferedWriter objBuffer = new BufferedWriter (objWriter);
		
		// -----------------------------------------------------------------------
		// writing to the file
		// -----------------------------------------------------------------------
		
		// write the title
		String sTitle = treeViewer.getColumnTitle(0, COMMA_SEPARATOR);
		objBuffer.write(sTitle + Utilities.NEW_LINE);

		// write the top row items
		Object root = treeViewer.getInput();
		if (root instanceof Scope) {
			StringBuffer sb = new StringBuffer();
			saveContentIntoBuffer(sb, (Scope)root, COMMA_SEPARATOR);
			objBuffer.write(sb.toString());
			objBuffer.write(Utilities.NEW_LINE);
			
		} else {
			String sTopRow[] = Utilities.getTopRowItems(treeViewer);
			// tricky: add '"' for uniting the text in the spreadsheet
			sTopRow[0] = "\"" + sTopRow[0] + "\"";	
			sTitle = treeViewer.getTextBasedOnColumnStatus(sTopRow, COMMA_SEPARATOR, 0, 0);
			objBuffer.write(sTitle + Utilities.NEW_LINE);
		}

		// write the content text
		ArrayList<TreeItem> items = new ArrayList<TreeItem>();
		internalCollectExpandedItems(items, treeViewer.getTree().getItems());
		String sText = getContent( items.toArray(new TreeItem[items.size()]), 
				COMMA_SEPARATOR);
		objBuffer.write(sText);
		
		// -----------------------------------------------------------------------
		// End of the process
		// -----------------------------------------------------------------------							
		objBuffer.close();
	}
	
	/**
	 * Retrieve the content of the table into a string
	 * @param items (list of items to be exported)
	 * @param sSeparator (separator)
	 * @return String: content of the table
	 */
	private String getContent(TreeItem []items, String sSeparator) {
    	StringBuffer sbText = new StringBuffer();
    	
    	// get all selected items
    	for (int i=0; i< items.length; i++) {
    		TreeItem objItem = items[i];
    		Object o = objItem.getData();
    		
    		// let get the metrics if the selected item is a scope node
    		if (o instanceof Scope) {
    			Scope objScope = (Scope) o;
    			saveContentIntoBuffer( sbText, objScope, sSeparator);
    			
    		} else {
    			// in case user click the first row, we need a special treatment
    			// first row of the table is supposed to be a sub-header, but at the moment we allow user
    			//		to do anything s/he wants.
    			String sElements[] = (String []) o; 
    			sbText.append( "\"" + sElements[0] + "\"" );
    			sbText.append( sSeparator ); // separate the node title and the metrics
    			sbText.append( this.treeViewer.getTextBasedOnColumnStatus(sElements, sSeparator, 1, 0) );
    		}
    		sbText.append(Utilities.NEW_LINE);
    	}
    	return sbText.toString();
	}
	
	
	/****
	 * Return the selected items in a table.
	 * It supports multi-line selections.
	 * @return StringBuffer of the selected items
	 */
	public StringBuffer getSelectedRows() {
		ITreeSelection selection = treeViewer.getStructuredSelection();
		
		@SuppressWarnings("unchecked")
		Iterator<Object> iterator = selection.iterator();
		
		StringBuffer sb = new StringBuffer();
		
		while(iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof Scope) {
				saveContentIntoBuffer(sb, (Scope) o, SPACE_SEPARATOR);
			} else {
				saveContentIntoBuffer(sb, (Scope) treeViewer.getInput(), SPACE_SEPARATOR);
			}
		}
		return sb;
	}
	
	/**
	 * private function to copy a scope node into a buffer string
	 * @param objScope current scope
	 * @param sSeparator string separator between different metric column
	 * @param sbTargetStringBuffer output saved content
	 */
	private void saveContentIntoBuffer( StringBuffer sbTargetStringBuffer, Scope objScope, String sSeparator ) {

		final TreeColumn []columns = treeViewer.getTree().getColumns();
		sbTargetStringBuffer.append( "\"" + objScope.getName() + "\"" );
		
		for(int j=1; j<columns.length; j++) 
		{
			if (columns[j].getWidth()>0) {
				Object obj = columns[j].getData();
				if (obj != null && obj instanceof BaseMetric) {
					// the column is not hidden
					BaseMetric metric = (BaseMetric) obj;
					MetricValue value = objScope.getMetricValue(metric);
					if (value == MetricValue.NONE) {
						// no value: write empty space
						sbTargetStringBuffer.append(sSeparator + " ");
					} else {
						sbTargetStringBuffer.append(sSeparator + value.getValue());
					}
				}
			}
		}
	}
	
	
	/**
	 * This method is a modified version of AbstractViewer.internalCollectExpandedItems()
	 * @param result
	 * @param items
	 */
	private void internalCollectExpandedItems(List<TreeItem> result, TreeItem []items) {
		if (items != null)
			for (int i = 0; i < items.length; i++) {
				TreeItem itemChild = items[i];
				if (itemChild.getData() instanceof Scope)
					result.add(itemChild);
				if (itemChild.getExpanded())
					internalCollectExpandedItems(result, itemChild.getItems());
			}
	}

}

// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcmerge;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;

public class DatabaseMergeWizard extends Wizard 
{
	private List<IDatabase> listDb;
	private DatabasesToMerge database;
	
	public DatabaseMergeWizard(List<IDatabase> list) {
		setNeedsProgressMonitor(true);
		this.listDb = list;
		this.database = new DatabasesToMerge();
	}

	
	/****
	 * Retrieve the databases to be merged and the metrics to correspond.
	 * 
	 * @return
	 */
	public DatabasesToMerge getDatabaseToMerge() {
		return database;
	}
	
    @Override
    public String getWindowTitle() {
        return "Merging two databases";
    }
    
    
    @Override
    public void addPages() {
     WizardPage []pages;
    	// if we only have 2 database, we directly pick the metric to compare.
    	// otherwise we have to chose the databases, and then select the metric.
    	if (listDb.size() == 2) {
    		// no need to select databases
    		// initialize the two databases to merge here. 
    		database.experiment[0] = (Experiment) listDb.get(0).getExperimentObject();
    		database.experiment[1] = (Experiment) listDb.get(1).getExperimentObject();
    		
    		ChooseMetricPage page = new ChooseMetricPage(database);
    		pages = new WizardPage[] {page};
    	} else {
    		pages = new WizardPage[] {new ChooseDatabasePage(listDb, database), new ChooseMetricPage(database)};
    	}
    	for(WizardPage page : pages) {
        	addPage(page);
    	}
    }
    
    
    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {
    	
    	IWizardPage page = super.getNextPage(currentPage);
    	if (page instanceof ChooseMetricPage cmp) {
    		cmp.setDatabasesToMerge(database);
    	}
    	return page;
    }
    
	@Override
	public boolean performFinish() {
		return true;
	}
}

package edu.rice.cs.hpcmerge;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;

public class DatabaseMergeWizard extends Wizard 
{
	private WizardPage []pages;
	private List<Experiment> listDb;
	private DatabasesToMerge database;
	
	public DatabaseMergeWizard(List<Experiment> list) {
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
    	// if we only have 2 database, we directly pick the metric to compare.
    	// otherwise we have to chose the databases, and then select the metric.
    	if (listDb.size() == 2) {
    		// no need to select databases
    		// initialize the two databases to merge here. 
    		database.experiment[0] = listDb.get(0);
    		database.experiment[1] = listDb.get(1);
    		
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
    	if (page instanceof ChooseMetricPage) {
    		ChooseMetricPage cmp = (ChooseMetricPage) page;
    		cmp.setDatabasesToMerge(database);
    	}
    	return page;
    }
    
	@Override
	public boolean performFinish() {
		return true;
	}
}

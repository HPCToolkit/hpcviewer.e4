package edu.rice.cs.hpc.data.experiment.extdata;

public interface IFilteredData extends IBaseData{

	//filter() shouldn't be public but by specifying it here, we would force it to be.
	//void filter();
	public void setFilter(FilterSet filter) ;
	public FilterSet getFilter() ;
	public boolean isGoodFilter();

	
}

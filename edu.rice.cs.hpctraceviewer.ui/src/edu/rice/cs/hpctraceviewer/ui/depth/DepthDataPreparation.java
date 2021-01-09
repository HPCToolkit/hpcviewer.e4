package edu.rice.cs.hpctraceviewer.ui.depth;

import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpctraceviewer.data.BaseDataVisualization;
import edu.rice.cs.hpctraceviewer.data.DataLinePainting;
import edu.rice.cs.hpctraceviewer.data.DataPreparation;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;

/*********************************************
 * 
 * Class to prepare data for depth view
 *
 *********************************************/
public class DepthDataPreparation extends DataPreparation {

	/*****
	 * list of data to be painted on the depth view
	 */
	final private TimelineDataSet dataset;
	final private int minDepth;
	/****
	 * Constructor to prepare data
	 * 
	 * @param _colorTable
	 * @param _ptl
	 * @param _begTime
	 * @param _depth
	 * @param _height
	 * @param _pixelLength
	 * @param _usingMidpoint
	 */
	public DepthDataPreparation(DataLinePainting data, int minDepth, int visibleDepths) {
		
		super(data);
		dataset = new TimelineDataSet(data.ptl.line(), data.ptl.size(), data.height);
		this.minDepth = minDepth;
	}

	@Override
	public void finishLine(int currSampleMidpoint, int succSampleMidpoint,
			int currDepth, Color color, int sampleCount) {

		BaseDataVisualization data = new BaseDataVisualization(currSampleMidpoint, 
				succSampleMidpoint, currDepth, color);
		
		dataset.add(data);
	}
	
	/***
	 * retrieve the list of data to be painted 
	 * 
	 * @return
	 */
	public TimelineDataSet getList() {
		
		return dataset;
	}

	public int getMinDepth() {
		return minDepth;
	}

}

// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.internal;


import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;




/******************************************************************
 * An abstract class for the two canvases on the viewer to extend.
 *****************************************************************/
public abstract class SpaceTimeCanvas extends BufferedCanvas
{
	/**The SpaceTimeData corresponding to this canvas.*/
	protected SpaceTimeDataController stData;
	
    /**Creates a SpaceTimeCanvas with the data _stData and Composite _composite.*/
    public SpaceTimeCanvas(Composite _composite)
    {
		super(_composite, false);
	}
        
    /**Conversion factor from actual time to pixels on the x axis. To be implemented in subclasses.*/
    public abstract double getScalePixelsPerTime();
    
    /**Conversion factor from actual processes to pixels on the y axis.  To be implemented in subclasses.*/
    public abstract double getScalePixelsPerRank();
    
    public void setSpaceTimeData(SpaceTimeDataController dataTraces) {
    	this.stData = dataTraces;
    }
}
// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.base;


public interface ISpaceTimeCanvas 
{
    /**Conversion factor from actual time to pixels on the x axis. To be implemented in subclasses.*/
    double getScalePixelsPerTime();
    
    /**Conversion factor from actual processes to pixels on the y axis.  To be implemented in subclasses.*/
    double getScalePixelsPerRank();

    /** display a temporary message on the canvas **/
	void setMessage(String message);

}

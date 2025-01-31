// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.map;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/*****
 * 
 * Class to store procedure class and its data
 *
 */
public class ProcedureClassData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1704953739329962260L;
	
	private String procClass;
	private RGB rgb;
	
	/***
	 * Initialize object using color data
	 * 
	 * @param procClass
	 * @param color
	 */
	public ProcedureClassData( String procClass, Color color ) {
		this.procClass = procClass;
		this.rgb = color.getRGB();
	}

	/****
	 * Object initialization with RGB data
	 * @param procClass
	 * @param rgb
	 */
	public ProcedureClassData( String procClass, RGB rgb ) {
		this.procClass = procClass;
		this.rgb = rgb;
	}

	/****
	 * get the procedure class of this object
	 * @return
	 */
	public String getProcedureClass() {
		return procClass;
	}
	
	/****
	 * get the data (rgb) of this object
	 * @return
	 */
	public RGB getRGB() {
		return rgb;
	}
}

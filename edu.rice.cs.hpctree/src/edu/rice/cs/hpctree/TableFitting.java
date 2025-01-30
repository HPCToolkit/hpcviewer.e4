// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree;

import java.io.IOException;

import org.eclipse.jface.preference.PreferenceStore;

import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/*****************************
 * 
 * Resize all metric columns of a table.
 *
 *****************************/
public class TableFitting 
{
	private static final String PREF_FIT_MODE = "hpcviewer.fit.mode";
	/***
	 * 
	 * Mode of table column fitting:
	 * <ul>
	 * 	<li> {@code FIT_BOTH} fit both the header and the data
	 *  <li> {@code FIT_DATA} fit only the data
	 *  <li> {@code FIT_HEADER} fit only the header (not supported yet)
	 * </ul>
	 */
	public enum ColumnFittingMode {
		FIT_BOTH, FIT_DATA;
 	}

 	
 	/**
 	 * No implicit constructor
 	 */
 	private TableFitting() {
 		// hide the implicit constructor
 	}
	
 	/****
 	 * Get the label of the {@code ColumnFittingMode} enumearation.
 	 * 
 	 * @param mode
 	 * @return {@code String}
 	 * 	
 	 */
	public static String toString(ColumnFittingMode mode) {
		if (mode == ColumnFittingMode.FIT_DATA)
			return "The resize mode is based on the data";
		return "The resize mode is based on both the header label and the data";
	}

	
	/****
	 * Get the current fitting mode from the preference file.
	 * 
	 * @return {@code ColumnFittingMode}
	 */
	public static ColumnFittingMode getFittingMode() {
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		int fitMode = pref.getInt(PREF_FIT_MODE);
		return ColumnFittingMode.values()[fitMode];
	}
	
	
	/****
	 * Store the fitting mode to the preference file.
	 * The caller needs to intercept the I/O exception
	 * 
	 * @param mode 
	 * 			The mode the be saved to the file
	 * @throws IOException
	 */
	public static void saveFittingMode(ColumnFittingMode mode) throws IOException {
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		String value = String.valueOf(mode.ordinal());
		pref.putValue(PREF_FIT_MODE, value);
		pref.save();
	}
	
	
	/****
	 * Get the next fitting mode.
	 * 
	 * @param mode
	 * 
	 * @return {@code ColumnFittingMode} the next fitting mode
	 */
	public static ColumnFittingMode getNext(ColumnFittingMode mode) {
		int ord = mode.ordinal();
		var values = ColumnFittingMode.values();
		int next = (ord + 1) % values.length;
		return values[next];
	}
	
	
	/*****
	 * The main method to:
	 * <ul>
	 *  <li>Load the current fitting mode
	 *  <li>Convert to the next fitting mode
	 *  <li>Resize the table based on the "next" fitting mode
	 *  <li>Store the "next" fitting mode.
	 * </ul>
	 * @param table
	 * @throws IOException
	 */
	public static void fitTable(ScopeTreeTable table) throws IOException {
		var mode = getFittingMode();
		var next = getNext(mode);
		saveFittingMode(next);
		table.pack(true);
	}
}

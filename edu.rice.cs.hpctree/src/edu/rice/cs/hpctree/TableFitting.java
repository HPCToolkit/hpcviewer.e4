package edu.rice.cs.hpctree;

import java.io.IOException;

import org.eclipse.jface.preference.PreferenceStore;

import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;

public class TableFitting 
{
	private static final String PREF_FIT_MODE = "viewer.fit.mode";
	/***
	 * 
	 * Mode of table column fitting:
	 * <ul>
	 * 	<li> {@code FIT_BOTH} fit both the header and the data
	 *  <li> {@code FIT_DATA} fit only the data
	 *  <li> {@code FIT_HEADER} fit only the header (not supported yet)
	 * </ul>
	 */
	public static enum ColumnFittingMode {
		FIT_BOTH, FIT_DATA;
 	};

	
	public static String toString(ColumnFittingMode mode) {
		if (mode == ColumnFittingMode.FIT_DATA)
			return "Autofit is based on the data";
		return "Autofit is based on both the header and the data";
	}

	public static ColumnFittingMode getFittingMode() {
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		int fitMode = pref.getInt(PREF_FIT_MODE);
		return ColumnFittingMode.values()[fitMode];
	}
	
	
	public static void saveFittingMode(ColumnFittingMode mode) throws IOException {
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		String value = String.valueOf(mode.ordinal());
		pref.putValue(PREF_FIT_MODE, value);
		pref.save();
	}
	
	public static ColumnFittingMode getNext(ColumnFittingMode mode) {
		int ord = mode.ordinal();
		var values = ColumnFittingMode.values();
		int next = (ord + 1) % values.length;
		return values[next];
	}
	
	public static void fitTable(ScopeTreeTable table) throws IOException {
		var mode = getFittingMode();
		var next = getNext(mode);
		saveFittingMode(next);
		table.pack();
	}
}

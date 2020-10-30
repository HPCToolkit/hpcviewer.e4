package edu.rice.cs.hpctraceviewer.data;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.map.ProcedureClassData;
import edu.rice.cs.hpctraceviewer.data.util.Constants;
import edu.rice.cs.hpctraceviewer.data.util.ProcedureClassMap;

/**************************************************************
 * A data structure designed to hold all the name-color pairs
 * needed for the actual drawing.
 **************************************************************/
public class ColorTable 
{	
	static private final int COLOR_MIN = 16;
	static private final int COLOR_MAX = 200 - COLOR_MIN;
	static private final long RANDOM_SEED = 612543231L;
	
	static final private String SEPARATOR_PROCNAME = "\n";
	static final public String  UNKNOWN_PROCNAME   = "<no activity>";
	
	/**The display this ColorTable uses to generate the random colors.*/
	final private Display display;

	/** user defined color */
	private ProcedureClassMap classMap;
	
	final private Random random_generator;

	// data members
	
	private	ConcurrentMap<String, Color> colorMatcher;
	private	ConcurrentMap<String, Color> predefinedColorMatcher;
	
	private HashMap<Integer, AbstractCollection<String>>  mapRGBtoProcedure;
	private HashMap<Integer, String> mapReservedColor;

	/**
	 * Constructor: Creates a new ColorTable with Display _display.
	 * */
	public ColorTable()
	{
		display = Display.getCurrent();
		
		// rework the color assignment to use a single random number stream
		random_generator = new Random((long)RANDOM_SEED);

		// initialize the procedure-color map (user-defined color)
		classMap = new ProcedureClassMap(display);
		
		colorMatcher 		   = new ConcurrentHashMap<String, Color>();
		
		initializeWhiteColor();
		
		predefinedColorMatcher = new ConcurrentHashMap<String, Color>();
		mapRGBtoProcedure	   = new HashMap<Integer, AbstractCollection<String>>();
	}
	
	/**
	 * Dispose the allocated resources
	 */
	public void dispose() {
		for (Color col: colorMatcher.values()) {
			if (col != null) col.dispose();
		}
		
		colorMatcher.clear();
		
		for (Color col: predefinedColorMatcher.values()) {
			if (col != null) col.dispose();
		}
		predefinedColorMatcher.clear();
		
		classMap.dispose();
		
		mapRGBtoProcedure.clear();
	}
	
	
	/***
	 * Reset user defined color into the default one.
	 */
	public void resetPredefinedColor() 
	{
		for (Color col: predefinedColorMatcher.values()) {
			if (col != null) col.dispose();
		}
		predefinedColorMatcher.clear();
		classMap.clear();
		
		// reset the default
		classMap.refresh();
		
		Object []entries = classMap.getEntrySet();
		
		for (Object obj:entries) {
			@SuppressWarnings("unchecked")
			Entry<String, ProcedureClassData> entry = (Entry<String, ProcedureClassData>) obj;
			
			ProcedureClassData data = entry.getValue();
			final RGB rgb = data.getRGB();
			String proc = entry.getKey();
			Color cip = createColor(proc, rgb);
			
			predefinedColorMatcher.put(proc, cip);
			
			AbstractCollection<String> colProcs = mapRGBtoProcedure.get(rgb.hashCode());
			
			if (colProcs == null) {
				colProcs = new TreeSet<String>();
				colProcs.add(proc);
			} else if (!colProcs.contains(proc)){
				colProcs.add(proc);
				
			}
			mapRGBtoProcedure.put(rgb.hashCode(), colProcs);
		}
	}
	
	/**
	 * Returns the color in the colorMatcher that corresponds to the name's class
	 * @param name
	 * @return
	 */
	public Color getColor(String name)
	{		
		return  createColorIfAbsent(name);
	}

	
	/************************************************************************
	 * Return the name of the procedure for a given RGB or Color hashcode.<br/>
	 * If the hashcode is not recognized, it returns null.<br/>
	 * Notes: on Mac, the hashcode for RGB is the same as the hashcode for Color
	 * 
	 * @param int hashcode
	 * @return String the name of the procedure
	 ************************************************************************/
	public String getProcedureNameByColorHash(int hashcode) 
	{
		// get the reserved color first
		String proc = mapReservedColor.get(Integer.valueOf(hashcode));
		if (proc != null)
			return proc;
		
		// get the normal procedure (if exist)
		AbstractCollection<String> collProc = mapRGBtoProcedure.get(Integer.valueOf(hashcode));
		if (collProc == null)
			return UNKNOWN_PROCNAME;
		
		proc = "";
		Iterator<String> iterator = collProc.iterator();
		while(iterator.hasNext()) {
			proc += iterator.next();
			if (iterator.hasNext()) 
				proc += SEPARATOR_PROCNAME;
		}
		return proc;
	}
	
	/************************************************************************
	 * add list of reserved color-procedure pair
	 * 
	 * @param procName
	 * @param rgb
	 ************************************************************************/
	public void addReservedColor(String procName, RGB rgb)
	{
		if (mapReservedColor == null) {
			mapReservedColor = new HashMap<Integer, String>(1);
		}
		mapReservedColor.put(rgb.hashCode(), procName);
	}
		
	
	
	//private ColorImagePair []listDefinedColorImagePair = null;
	
	/************************************************************************
	 * Main method to generate color if necessary <br/>
	 * This creates a pair of color and image based on the procedure name.
	 * If a procedure is already assigned a color, we do nothing. <br/>
	 * Otherwise, it creates color and image to be assigned to this procedure.
	 * <br> If the list of colors is too big, it will pick randomly from existing
	 * color to avoid too many handles created. Some OS like Windows has limitation
	 * of the number of handles to be generated.
	 * 
	 * @param procName the name of the procedure
	 * 
	 * @return ColorImagePair
	 ************************************************************************/
	private Color createColorIfAbsent(String procName)
	{
		
		// 1. check if it matches predefined colors
		Entry<String, ProcedureClassData> data = classMap.getEntry(procName);
		if (data != null) {
			return predefinedColorMatcher.
					computeIfAbsent(procName, 
									val -> createColor(procName, data.getValue().getRGB()));
		}
		
		// 2. check duplicates

		Color color = colorMatcher.computeIfAbsent(procName, 
									 	   val -> createColor(procName, 
											 	      getProcedureColor(procName, 
											 	    		  			COLOR_MIN, 
											 	    		  			COLOR_MAX, 
											 	    		  			random_generator)));
		  
		// store in a hashmap the pair of RGB hashcode and procedure name
		// if the hash is already stored, we concatenate the procedure name
		storeProcedureName(color.getRGB(), procName);
		
		return color;
	}
	
	
	/************************************************************************
	 * Store a procedure name to the map from rgb to procedure name
	 *   
	 * @param rgb
	 * @param procName
	 ************************************************************************/
	private void storeProcedureName(RGB rgb, String procName)
	{		
		// store in a hashmap the pair of RGB hashcode and procedure name
		// if the hash is already stored, we concatenate the procedure name
		Integer key = Integer.valueOf(rgb.hashCode());
		AbstractCollection<String> setOfProcs = mapRGBtoProcedure.get(key);
		
		if (setOfProcs != null) {
			if (setOfProcs.contains(procName))
				return;
		} else {
			setOfProcs = new TreeSet<String>();
		}
		setOfProcs.add(procName);
		mapRGBtoProcedure.put(key, setOfProcs);
	}
	
	/************************************************************************
	 * Create a pair color with a specified RGB.
	 * If the OS limits the number of colors, and we exceeds the quota,
	 * it will display an error message and throw a runtime exception.
	 * 
	 * @param procName
	 * @param rgb
	 * @return Color
	 ************************************************************************/
	private Color createColor(String procName, RGB rgb) 
	{
		try {
			return new Color(display, rgb);

		} catch (Exception e) {
			// Windows only: in case we don't have enough GDI objects to ve
			// created, we should notify users. They can then set the max
			// GGI object to higher number
			String msg = "The number of colors exceeds the quota from the OS.\n" +
						 e.getLocalizedMessage();
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error resource creation: " + msg, e);
			
			MessageDialog.openError(display.getActiveShell(), 
									"Error " + e.getClass(), 
									msg
									);
			
			throw new RuntimeException(msg);
		}
	}
	
	/***********************************************************************
	 * retrieve color for a procedure. If the procedure has been assigned to
	 * 	a color, we'll return the allocated color, otherwise, create a new one
	 * 	randomly.
	 * 
	 * @param name name of the procedure
	 * @param colorMin minimum integer value
	 * @param colorMax maximum integer value
	 * @param r random integer
	 * 
	 * @return RGB
	 ***********************************************************************/
	private RGB getProcedureColor( String name, int colorMin, int colorMax, Random r ) {
		
		RGB rgb = new RGB(	colorMin + r.nextInt(colorMax), 
				colorMin + r.nextInt(colorMax), 
				colorMin + r.nextInt(colorMax));
		return rgb;
	}

	/************************************************************************
	 * Initialize the predefined-value of white color
	 * 
	 * If the white color value is not initialize, we create a new one
	 * Otherwise, do nothing.
	 ************************************************************************/
	private void initializeWhiteColor() {
		// create our own white color so we can dispose later, instead of disposing
		//	Eclipse's white color
		final RGB rgb_white = display.getSystemColor(SWT.COLOR_WHITE).getRGB();
		final Color col_white = new Color(display, rgb_white);
		
		colorMatcher.put(Constants.NULL_FUNCTION, col_white);
		
		addReservedColor(UNKNOWN_PROCNAME, rgb_white);
	}
	
	
	@Override
	public String toString() {
		return "pc: " + predefinedColorMatcher.size() + ", cm: " + colorMatcher.size();
	}
}
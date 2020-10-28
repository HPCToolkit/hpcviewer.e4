package edu.rice.cs.hpctraceviewer.data;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
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
	static final public int COLOR_ICON_SIZE = 8;
	
//	static private final int MAX_NUM_DIFFERENT_COLORS = 512;
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

	private ColorImagePair IMAGE_WHITE;
	private	HashMap<String, ColorImagePair> colorMatcher;
	private	HashMap<String, ColorImagePair> predefinedColorMatcher;
	private HashMap<Integer, AbstractCollection<String>>        mapRGBtoProcedure;
	private HashMap<Integer, String>		mapReservedColor;

	/**Creates a new ColorTable with Display _display.*/
	public ColorTable()
	{
		display = Display.getCurrent();
		
		// rework the color assignment to use a single random number stream
		random_generator = new Random((long)RANDOM_SEED);

		// initialize the procedure-color map (user-defined color)
		classMap = new ProcedureClassMap(display);
		
		colorMatcher 		   = new HashMap<String, ColorTable.ColorImagePair>();
		
		initializeWhiteColor();
		
		predefinedColorMatcher = new HashMap<String, ColorTable.ColorImagePair>();
		mapRGBtoProcedure	   = new HashMap<Integer, AbstractCollection<String>>();
	}
	
	/**
	 * Dispose the allocated resources
	 */
	public void dispose() {
		for (ColorImagePair pair: colorMatcher.values()) {
			if (pair != null) pair.dispose();
		}
		
		colorMatcher.clear();
		
		for (ColorImagePair pair: predefinedColorMatcher.values()) {
			if (pair != null) pair.dispose();
		}
		predefinedColorMatcher.clear();
		
		classMap.dispose();
		
		mapRGBtoProcedure.clear();
	}
	
	public void resetPredefinedColor() 
	{
		for (ColorImagePair pair: predefinedColorMatcher.values()) {
			if (pair != null) pair.dispose();
		}
		predefinedColorMatcher.clear();
		classMap.clear();
		
		classMap.refresh();
		
		Object []entries = classMap.getEntrySet();
		for (Object obj:entries) {
			@SuppressWarnings("unchecked")
			Entry<String, ProcedureClassData> entry = (Entry<String, ProcedureClassData>) obj;
			
			ProcedureClassData data = entry.getValue();
			final RGB rgb = data.getRGB();
			String proc = entry.getKey();
			ColorImagePair cip = createColorImagePair(proc, rgb);
			
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
		ColorImagePair cip = createColorImagePair(name);
		return cip.getColor();
	}
	
	/**
	 * returns the image that corresponds to the name's class
	 * @param name
	 * @return
	 */
	public Image getImage(String name) 
	{
		ColorImagePair cip = createColorImagePair(name); 
		return cip.getImage();
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
	
	/***********************************************************************
	 * create an image based on the color
	 * the caller is responsible to free the image
	 * 
	 * @param display
	 * @param color
	 * @return an image (to be freed)
	 ***********************************************************************/
	static public Image createImage(Display display, RGB color) {
		PaletteData palette = new PaletteData(new RGB[] {color} );
		ImageData imgData = new ImageData(COLOR_ICON_SIZE, COLOR_ICON_SIZE, 1, palette);
		Image image = new Image(display, imgData);
		return image;
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
	private ColorImagePair createColorImagePair(String procName)
	{
		ColorImagePair cip;
		
		// 1. check if it matches predefined colors
		Entry<String, ProcedureClassData> data = classMap.getEntry(procName);
		if (data != null) {
			
			cip = predefinedColorMatcher.get(procName);
			if (cip != null)
				return cip;
			
			ProcedureClassData value = data.getValue();
			
			final RGB rgb = value.getRGB();
			cip = createColorImagePair(procName, rgb);
			predefinedColorMatcher.put(procName, cip);
			
			// store the key, not the procedure name
			storeProcedureName(rgb, data.getKey());
			
			return cip;
		}
		
		// 2. check duplicates
		cip = colorMatcher.get(procName);
		if (cip != null) {
			return cip;
		}
		
		// 3. generate a new color-image if we have enough handles
		RGB rgb = getProcedureColor( procName, COLOR_MIN, COLOR_MAX, random_generator );
		cip = createColorImagePair(procName, rgb);

		// store in a hashmap the pair procdure and image-color
		colorMatcher.put(procName, cip);
		
		// store in a hashmap the pair of RGB hashcode and procedure name
		// if the hash is already stored, we concatenate the procedure name
		storeProcedureName(cip.color.getRGB(), procName);
		
		return cip;
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
	 * Create a pair color and image with a specified RGB
	 * 
	 * @param procName
	 * @param rgb
	 * @return
	 ************************************************************************/
	private ColorImagePair createColorImagePair(String procName, RGB rgb) 
	{
		try {
			Color c = new Color(display, rgb);
			Image i = createImage(display, rgb);
			ColorImagePair cip = new ColorImagePair(c, i);
			
			return cip;
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error resource creation", e);
			
			MessageDialog.openError(display.getActiveShell(), 
									"Error " + e.getClass(), 
									e.getLocalizedMessage());
		}
		return null;
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
		if (IMAGE_WHITE == null || IMAGE_WHITE.getImage().isDisposed()) {
			// create our own white color so we can dispose later, instead of disposing
			//	Eclipse's white color
			final RGB rgb_white = display.getSystemColor(SWT.COLOR_WHITE).getRGB();
			final Color col_white = new Color(display, rgb_white);
			final Image img_white = createImage(display, rgb_white);
			
			IMAGE_WHITE = new ColorImagePair(col_white, img_white );
			
			colorMatcher.put(Constants.NULL_FUNCTION, IMAGE_WHITE);
			
			addReservedColor(UNKNOWN_PROCNAME, rgb_white);
		}
	}
	
	
	/************************************************************************
	 * class to pair color and image
	 * @author laksonoadhianto
	 *
	 ************************************************************************/
	private class ColorImagePair {
		private Color color;
		private Image image;
		
		/****
		 * create a color-image pair
		 * @param Color color
		 * @param Image image
		 */
		ColorImagePair(Color color, Image image) {
			// create an empty image filled with color c
			this.image = image;
			this.color = color;
		}
		
		/***
		 * get the color 
		 * @return Color
		 */
		public Color getColor() {
			return this.color;
		}
		
		/***
		 * get the image
		 * @return Image
		 */
		public Image getImage() {
			return this.image;
		}
		
		public void dispose() {
			this.color.dispose();
			this.image.dispose();
		}
	}
}
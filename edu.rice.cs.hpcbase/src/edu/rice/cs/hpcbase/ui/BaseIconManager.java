package edu.rice.cs.hpcbase.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;


/********************************************
 * 
 * Icon manager for registering to ImageRegistry
 *
 ********************************************/
public abstract class BaseIconManager 
{
	/********
	 * get image from the registry
	 * 
	 * @param desc
	 * @return Image if the key matches, null otherwise
	 */
	static public Image getImage(final String desc) {
		final ImageRegistry registry = getRegistry();
		
		return registry.get(desc);
	}
	
	/********
	 * get image descriptor from the registry
	 * 
	 * @param desc : the key descriptor
	 * @return ImageRegistry if the key matches, null otherwise
	 */
	static public ImageDescriptor getDescriptor(final String desc) {
		final ImageRegistry registry = getRegistry();
		
		return registry.getDescriptor(desc);
	}

	static protected ImageRegistry getRegistry() {
		
		return JFaceResources.getImageRegistry();
	}
	
	protected void registerImage(ImageRegistry registry, Class<?> location, String key) {
		final ImageDescriptor desc = ImageDescriptor.createFromFile(location, key);
		registry.put(key, desc.createImage());
	}

	protected void registerDescriptor(ImageRegistry registry, Class<?> location, String key) {
		final ImageDescriptor desc = ImageDescriptor.createFromFile(location, key);
		registry.put(key, desc);
	}
}

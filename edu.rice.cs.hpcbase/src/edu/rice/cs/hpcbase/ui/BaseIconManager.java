package edu.rice.cs.hpcbase.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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
	static public Image getImage(AbstractUIPlugin plugin, final String desc) {
		final ImageRegistry registry = getRegistry(plugin);
		
		return registry.get(desc);
	}
	
	/********
	 * get image descriptor from the registry
	 * 
	 * @param desc : the key descriptor
	 * @return ImageRegistry if the key matches, null otherwise
	 */
	static public ImageDescriptor getDescriptor(AbstractUIPlugin plugin, final String desc) {
		final ImageRegistry registry = getRegistry(plugin);
		
		return registry.getDescriptor(desc);
	}

	static protected ImageRegistry getRegistry(AbstractUIPlugin plugin) {
    	// prepare the icon
		ImageRegistry imageRegistry = plugin.getImageRegistry();
		
		return imageRegistry;
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

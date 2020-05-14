/**
 * 
 */
package edu.rice.cs.hpcviewer.ui.util;

import java.util.Stack;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 *
 */
public class TreeItemManager {

	private Stack<ElementDescriptor> stackElement;
	
	/**
	 * Constructor. this class has to be initialized
	 */
	public TreeItemManager() {
		this.stackElement = new Stack<ElementDescriptor>();
	}
	
	/**
	 * save the context of the tree view (such as the top row header)
	 * @param tree
	 */
	public void saveContext(TreeViewer treeViewer) {
		final Tree tree = treeViewer.getTree();
		if (!tree.isDisposed())
		{
			ElementDescriptor objDesc = this.getTopItem(tree);
			if (objDesc != null)
				this.stackElement.push(objDesc);
		}
	}
	
	/**
	 * Restore the saved context into the tree view
	 * @param tree
	 */
	public void restoreContext(TreeViewer treeViewer) {
		if (this.stackElement.size()>0) {
			ElementDescriptor objDesc = this.stackElement.pop();
			Tree tree = treeViewer.getTree();
			if (!tree.isDisposed())
			{
				Utilities.insertTopRow(treeViewer, objDesc.imgElement, objDesc.sTextElement);
			}
		}
	}
	
	/**
	 * Retrieve the descriptor context of the top item in the tree
	 * @param tree
	 * @return
	 */
	private ElementDescriptor getTopItem(Tree tree) {
		if (tree.getItemCount() == 0)
			return null;
		
		TreeItem itemTop = tree.getItem(0);
		Image imgItem = itemTop.getImage(0);
		String []sText= null;
		if(itemTop.getData() instanceof Scope) {
			// the table has been zoomed-out
		} else {
			// the table is in original form or flattened or zoom-in
			Object o = itemTop.getData();
			if(o != null) {
				Object []arrObj = (Object []) o;
				if(arrObj[0] instanceof String) {
					sText = (String[]) itemTop.getData(); 
				}
			}
		}
		return new ElementDescriptor(itemTop, imgItem, sText);
	}

	/**
	 * Class containing elements for the context
	 * @author laksonoadhianto
	 *
	 */
	private class ElementDescriptor {
		Image imgElement;
		String []sTextElement;
		
		
		/**
		 * Constructor
		 * @param item
		 * @param img
		 * @param sTexts
		 */
		ElementDescriptor (TreeItem item, Image img, String []sTexts) {
			this.imgElement = img;
			this.sTextElement = sTexts;
		}
	}

}

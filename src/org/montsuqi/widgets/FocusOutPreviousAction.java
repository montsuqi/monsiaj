/*
 * Created on 2004/09/29
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.montsuqi.widgets;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


class FocusOutPreviousAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
		Component c = (Component)e.getSource();
		c.transferFocusBackward();
	}
}
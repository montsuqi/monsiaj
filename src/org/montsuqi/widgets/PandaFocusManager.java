package org.montsuqi.widgets;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.DefaultFocusManager;

import org.montsuqi.monsia.Interface;

public class PandaFocusManager extends DefaultFocusManager {

	public void processKeyEvent(Component focusedComponent, KeyEvent e) {
		if (handleAccels(e)) {
			e.consume();
		} else {
			super.processKeyEvent(focusedComponent, e);
		}
	}

	private boolean handleAccels(KeyEvent e) {
		return Interface.handleAccels(e);
	}

}

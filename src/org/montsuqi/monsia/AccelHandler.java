package org.montsuqi.monsia;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;

import org.montsuqi.util.Logger;

public class AccelHandler {
	class Pair {
		Component widget;
		AccelInfo accel;
		Pair(Component widget, AccelInfo accel) {
			this.widget = widget;
			this.accel = accel;
		}
	}

	private Logger logger;

	List list;

	AccelHandler() {
		list = new ArrayList();
		logger = Logger.getLogger(AccelHandler.class);
	}

	public void addAccels(Component widget, List accels) {
		Iterator i = accels.iterator();
		while (i.hasNext()) {
			AccelInfo accel = (AccelInfo)i.next();
			list.add(new Pair(widget, accel));
		}
	}

	public boolean handleAccel(KeyEvent e) {
		if (e.getID() != KeyEvent.KEY_PRESSED) {
			return false;
		}
		Iterator i = list.iterator();
		while (i.hasNext()) {
			Pair pair = (Pair)i.next();
			Component widget = pair.widget;
			if ( ! (widget instanceof AbstractButton)) {
				continue;
			}
			AbstractButton button = (AbstractButton)widget;
			AccelInfo accel = pair.accel;
			if (accel.getKey() == e.getKeyCode()&& accel.getModifiers() == e.getModifiers()) {
				button.doClick();
				return true;
			}
		}
		return false;
	}
}

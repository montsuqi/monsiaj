/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

This module is part of PANDA.

		PANDA is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
to anyone for the consequences of using it or for whether it serves
any particular purpose or works at all, unless he says so in writing.
Refer to the GNU General Public License for full details.

		Everyone is granted permission to copy, modify and redistribute
PANDA, but only under the conditions described in the GNU General
Public License.  A copy of this license is supposed to have been given
to you along with PANDA so you can know your rights and
responsibilities.  It should be in a file named COPYING.  Among other
things, the copyright notice and this notice must be preserved on all
copies.
*/

package org.montsuqi.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Timer;
import javax.swing.JComponent;
import org.montsuqi.util.Logger;

public class PandaTimer extends JComponent {

	protected static final Logger logger = Logger.getLogger(PandaTimer.class);

	private Timer timer;

	private static final Map timers;
	static {
		timers = new WeakHashMap();
	}

	public static void suspendAllTimers() {
		Iterator i = timers.keySet().iterator();
		while (i.hasNext()) {
			Timer timer = (Timer)i.next();
			timer.stop();
		}
	}

	public static void resumeAllTimers() {
		Iterator i = timers.keySet().iterator();
		while (i.hasNext()) {
			Timer timer = (Timer)i.next();
			timer.start();
		}
	}

	public PandaTimer() {
		super();
		timer = new Timer(0, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireTimeEvent(new TimerEvent(PandaTimer.this));
			}
		});
		timer.setRepeats(true);
		timer.start();
		timers.put(this, null);
	}

	public void addTimerListener(TimerListener l) {
		listenerList.add(TimerListener.class, l);
	}

	public void removeTimerListener(TimerListener l) {
		listenerList.remove(TimerListener.class, l);
	}

	protected void fireTimeEvent(TimerEvent e) {
		TimerListener[] listeners = (TimerListener[])listenerList.getListeners(TimerListener.class);
		for (int i = 0, n = listeners.length; i < n; i++) {
			TimerListener l = listeners[i];
			l.timerSignaled(e);
		}
		Object[] args = { getName(), new Date(), Thread.currentThread() };
		logger.debug("timer {0} ring at {1,time} in {2}", args); //$NON-NLS-1$
	}

	public void setDuration(int duration) {
		timer.setDelay(duration * 1000);
	}

	public int getDuration() {
		return timer.getDelay() / 1000;
	}

	public void reset() {
		timer.restart();
		Object[] args = { getName(), new Date(), Thread.currentThread() };
		logger.debug("timer {0} reset at {1,time} in {2}", args); //$NON-NLS-1$
	}

	public void finalize() {
		timers.remove(this);
	}
}

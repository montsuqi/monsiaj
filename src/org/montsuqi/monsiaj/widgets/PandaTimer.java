/*      PANDA -- a simple transaction monitor

 Copyright (C) 1998-1999 Ogochan.
 2000-2003 Ogochan & JMA (Japan Medical Association).
 2002-2006 OZAWA Sakuro.

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
package org.montsuqi.monsiaj.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>A component that holds a timer to fire events periodically.</p>
 *
 * <p>This class repeatedly fires a TimerEvent once on every repetition of
 * period to its TimerListeners.</p>
 */
public class PandaTimer extends JComponent {

    protected static final Logger logger = LogManager.getLogger(PandaTimer.class);
    private Timer timer;

    /**
     * <p>Constructs a timer component.</p>
     *
     * <p>Initially this component's timer has a duration of 60 seconds. This
     * helps it wait firing events until the component is on view.</p>
     * <p>Correct duration should be set later.</p>
     */
    public PandaTimer() {
        super();
        // initial delay is 60sec, to wait widget construction,
        // should be set to correct value later
        timer = new Timer(60 * 1000, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fireTimeEvent(new TimerEvent(PandaTimer.this));
            }
        });
        timer.setRepeats(true);
        startTimer();
        this.setFocusable(false);
    }

    public void startTimer() {
        if (!timer.isRunning() && timer.getDelay() != 0) {
            logger.debug("starting timer: {0}", getName()); 
            timer.start();
        }
    }

    public void stopTimer() {
        if (timer.isRunning()) {
            logger.debug("stopping timer: {0}", getName()); 
            timer.stop();
        }
    }

    /**
     * <p>Stops and restarts the timer.</p>
     */
    public void reset() {
        stopTimer();
        startTimer();
    }

    /**
     * <p>Adds a timer listener to this component.</p>
     *
     * @param l a TimerListener instance to add.
     */
    public void addTimerListener(TimerListener l) {
        listenerList.add(TimerListener.class, l);
    }

    /**
     * <p>Removes a timer listener from this component.</p>
     *
     * @param l a TimerListener instance to remove.
     */
    public void removeTimerListener(TimerListener l) {
        listenerList.remove(TimerListener.class, l);
    }

    protected void fireTimeEvent(TimerEvent e) {
        java.awt.Window w = SwingUtilities.windowForComponent(this);
        if (w instanceof Window && !((Window) w).isActive()) {
            return;
        }
        TimerListener[] listeners = (TimerListener[]) listenerList.getListeners(TimerListener.class);
        for (int i = 0, n = listeners.length; i < n; i++) {
            TimerListener l = listeners[i];
            l.timerSignaled(e);
        }
    }

    /**
     * <p>Sets the duration(repetition period) of the timer in seconds.</p>
     *
     * @param duration specifies duration in seconds. If it is zero, the timer
     * is stopped.
     */
    public void setDuration(int duration) {
        Object[] args = {getName(), new Integer(duration)};
        logger.debug("duration of {0}: {1}", args); 
        timer.setInitialDelay(duration * 1000); //throws IllegalArgumentException on negative argument.
        timer.setDelay(duration * 1000);
        assert duration >= 0;
        if (duration == 0) {
            stopTimer();
        } else {
            if (!timer.isRunning()) {
                startTimer();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        stopTimer();
        super.finalize();
    }

    /**
     * <p>Gets the duration(repetition period) of the timer in seconds.</p>
     */
    public int getDuration() {
        return timer.getDelay() / 1000;
    }
}

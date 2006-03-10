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
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public abstract class Preview extends JPanel {

	private final class ResetScaleAction extends AbstractAction {
		ResetScaleAction() {
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/zoom-reset.png"); //$NON-NLS-1$
			if (iconURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
			}
			putValue(Action.NAME, Messages.getString("PandaPreview.reset_scale")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.reset_scale_short_description")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			setScale(1.0);
		}
	}

	private final class ZoomInAction extends AbstractAction {
		ZoomInAction() {
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/zoom-in.png"); //$NON-NLS-1$
			if (iconURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
			}
			putValue(Action.NAME, Messages.getString("PandaPreview.zoom_in")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.zoom_in_short_description")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			setScale(scale * SCALE_FACTOR);
		}
	}

	private final class ZoomOutAction extends AbstractAction {
		ZoomOutAction() {
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/zoom-out.png"); //$NON-NLS-1$
			if (iconURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
			}
			putValue(Action.NAME, Messages.getString("PandaPreview.zoom_out")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.zoom_out_short_description")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			setScale(scale / SCALE_FACTOR);
		}
	}

	private final class RotateClockwiseAction extends AbstractAction {
		RotateClockwiseAction() {
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/redo.png"); //$NON-NLS-1$
			if (iconURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
			}
			putValue(Action.NAME, Messages.getString("PandaPreview.rotate_clockwise")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.rotate_clockwise_short_description")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			setRotationStep(rotationStep + 1);
		}
	}

	private final class RotateCounterClockwiseAction extends AbstractAction {
		RotateCounterClockwiseAction() {
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/undo.png"); //$NON-NLS-1$
			if (iconURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
			}
			putValue(Action.NAME, Messages.getString("PandaPreview.rotate_counter_clockwise")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.rotate_counter_clockwise_short_description")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			setRotationStep(rotationStep - 1);
		}
	}

	private final Action resetScaleAction;
	private final Action zoomOutAction;
	private final Action zoomInAction;
	private final Action rotateClockwiseAction;
	private final Action rotateCounterClockwiseAction;

	public Preview() {
		resetScaleAction = new ResetScaleAction();
		zoomOutAction = new ZoomOutAction();
		zoomInAction = new ZoomInAction();
		rotateClockwiseAction = new RotateClockwiseAction();
		rotateCounterClockwiseAction = new RotateCounterClockwiseAction();
		setScale(1.0);
		setRotationStep(0);
	}

	public Action getResetScaleAction() {
		return resetScaleAction;
	}

	public Action getZoomOutAction() {
		return zoomOutAction;
	}

	public Action getZoomInAction() {
		return zoomInAction;
	}

	public Action getRotateClockwiseAction() {
		return rotateClockwiseAction;
	}

	public Action getRotateCounterClockwiseAction() {
		return rotateCounterClockwiseAction;
	}

	public abstract void load(String fileName) throws IOException;

	public abstract void clear();

	protected double scale;
	protected int rotationStep;
	private static final double SCALE_FACTOR = 1.2;

	protected void setScale(double newScale) {
		if ( ! isValidScale(newScale)) {
			throw new IllegalArgumentException("non-positive scale"); //$NON-NLS-1$
		}
		if (Double.compare(scale, newScale) != 0) {
			double oldScale = scale;
			scale = newScale;
			firePropertyChange("scale", oldScale, newScale); //$NON-NLS-1$
		}
	}

	private boolean isValidScale(double newScale) {
		return ! Double.isNaN(newScale) && 0 < newScale && newScale <= Double.MAX_VALUE;
	}

	protected void setRotationStep(int newRotationStep) {
		// normalize rotationStep to the range of [0,4)
		if (newRotationStep < 0) {
			newRotationStep += 4 * ((-newRotationStep) / 4 + 1);
		}
		newRotationStep %= 4;
		if ( ! isValidRotationStep(newRotationStep)) {
			throw new IllegalArgumentException("invalid rotation"); //$NON-NLS-1$
		}
		rotationStep = newRotationStep;
	}

	private boolean isValidRotationStep(int newRotationStep) {
		return 0 <= newRotationStep && newRotationStep < 4;
	}
}

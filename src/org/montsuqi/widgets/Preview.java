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
			zoomIn();
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
			zoomOut();
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
			rotateClockwise();
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
			rotateCounterClockwise();
		}
	}

	private final Action zoomOutAction;
	private final Action zoomInAction;
	private final Action rotateClockwiseAction;
	private final Action rotateCounterClockwiseAction;

	public Preview() {
		zoomOutAction = new ZoomOutAction();
		zoomInAction = new ZoomInAction();
		rotateClockwiseAction = new RotateClockwiseAction();
		rotateCounterClockwiseAction = new RotateCounterClockwiseAction();
		resetScale();
		resetRotationStep();
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

	protected double scale;
	private static final double SCALE_FACTOR = 1.2;
	protected int rotationStep;

	public void zoomIn() {
		setScale(scale * SCALE_FACTOR);
	}

	public void zoomOut() {
		setScale(scale / SCALE_FACTOR);
	}

	protected final void setScale(double newScale) {
		if ( ! isValidScale(newScale)) {
			throw new IllegalArgumentException("non-positive scale"); //$NON-NLS-1$
		}
		scale = newScale;
		updatePreferredSize(newScale, rotationStep);
		revalidate();
		repaint();
	}

	protected abstract  void updatePreferredSize(double newScale, int newRotationStep);

	private boolean isValidScale(double newScale) {
		return ! Double.isNaN(newScale) && 0 < newScale && newScale <= Double.MAX_VALUE;
	}

	protected void resetScale() {
		setScale(1.0);
	}

	void rotateClockwise() {
		setRotationStep(rotationStep + 1);
	}

	void rotateCounterClockwise() {
		setRotationStep(rotationStep - 1);
	}

	protected void resetRotationStep() {
		setRotationStep(0);
	}

	private void setRotationStep(int newRotationStep) {
		if (newRotationStep < 0) {
			newRotationStep += 4 * ((-newRotationStep) / 4 + 1);
		}
		newRotationStep %= 4;
		if ( ! isValidRotationStep(newRotationStep)) {
			throw new IllegalArgumentException("invalid rotation"); //$NON-NLS-1$
		}
		rotationStep = newRotationStep;
		updatePreferredSize(scale, newRotationStep);
		revalidate();
		repaint();
	}

	private boolean isValidRotationStep(int newRotationStep) {
		return 0 <= newRotationStep && newRotationStep < 4;
	}
}

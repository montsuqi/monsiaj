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
import java.math.BigInteger;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

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

	private final class FitToSizeAction extends AbstractAction {

		FitToSizeAction() {
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/zoom-fitp.png"); //$NON-NLS-1$
			if (iconURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
			}
			putValue(Action.NAME, Messages.getString("PandaPreview.fit_to_size")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.fit_to_size_short_description")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			fitToSize();
		}

	}

	private final class FitToSizeHorizontallyAction extends AbstractAction {

		FitToSizeHorizontallyAction() {
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/zoom-fitw.png"); //$NON-NLS-1$
			if (iconURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
			}
			putValue(Action.NAME, Messages.getString("PandaPreview.fit_to_size_horizontally")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Messages.getString("PandaPreview.fit_to_size_horizontally_short_description")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			fitToSizeHorizontally();
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

	private final Action fitToSizeAction;
	private final Action fitToSizeHorizontallyAction;
	private final Action resetScaleAction;
	private final Action zoomOutAction;
	private final Action zoomInAction;
	private final Action rotateClockwiseAction;
	private final Action rotateCounterClockwiseAction;

	public Preview() {
		fitToSizeAction = new FitToSizeAction();
		fitToSizeHorizontallyAction = new FitToSizeHorizontallyAction();
		resetScaleAction = new ResetScaleAction();
		zoomOutAction = new ZoomOutAction();
		zoomInAction = new ZoomInAction();
		rotateClockwiseAction = new RotateClockwiseAction();
		rotateCounterClockwiseAction = new RotateCounterClockwiseAction();
		ActionMap actionMap = getActionMap();
		actionMap.put("fitToSize", fitToSizeAction);
		actionMap.put("fitToSizeHorizontally", fitToSizeHorizontallyAction);
		actionMap.put("resetScale", resetScaleAction);
		actionMap.put("zoomOut", zoomOutAction);
		actionMap.put("zoomIn", zoomInAction);
		actionMap.put("rotateClockwise", rotateClockwiseAction);
		actionMap.put("rotateCounterClockwise", rotateCounterClockwiseAction);

		InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke("ctrl G"), "fitToSize");
		inputMap.put(KeyStroke.getKeyStroke("shift F6"), "fitToSize");
		inputMap.put(KeyStroke.getKeyStroke("ctrl F"), "fitToSizeHorizontally");
		inputMap.put(KeyStroke.getKeyStroke("shift F5"), "fitToSizeHorizontally");
		inputMap.put(KeyStroke.getKeyStroke("ctrl MINUS"), "zoomOut");
		inputMap.put(KeyStroke.getKeyStroke("shift F8"), "zoomOut");
		inputMap.put(KeyStroke.getKeyStroke("ctrl PLUS"), "zoomIn");
		inputMap.put(KeyStroke.getKeyStroke("shift F7"), "zoomIn");
		setScale(1.0);
		setRotationStep(0);
	}

	public Action getFitToSizeAction() {
		return fitToSizeAction;
	}

	public Action getFitToSizeHorizontallyAction() {
		return fitToSizeHorizontallyAction;
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
	public abstract void fitToSize();
	public abstract void fitToSizeHorizontally();
	
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
		BigInteger r = BigInteger.valueOf(newRotationStep);
		r = r.mod(BigInteger.valueOf(4));
		newRotationStep = r.intValue();
		assert 0 <= newRotationStep && newRotationStep < 4;
		rotationStep = newRotationStep;
	}

}

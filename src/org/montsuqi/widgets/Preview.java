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
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/zoom-in.png"); //$NON-NLS-1$
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
			URL iconURL = getClass().getResource("/org/montsuqi/widgets/zoom-out.png"); //$NON-NLS-1$
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

	private final Action zoomOutAction;
	private final Action zoomInAction;

	public Preview() {
		zoomOutAction = new ZoomOutAction();
		zoomInAction = new ZoomInAction();
		setScale(1.0);
	}

	public Action getZoomOutAction() {
		return zoomOutAction;
	}

	public Action getZoomInAction() {
		return zoomInAction;
	}
	
	public abstract void load(String fileName) throws IOException;

	protected double scale;
	private static final double SCALE_FACTOR = 1.2;

	public void zoomIn() {
		setScale(scale * SCALE_FACTOR);
	}

	public void zoomOut() {
		setScale(scale / SCALE_FACTOR);
	}

	protected void setScale(double scale) {
		if ( ! Double.isNaN(scale) && 0 < scale && scale < Double.MAX_VALUE) {
			this.scale = scale;
			repaint();
		} else {
			throw new IllegalArgumentException(Messages.getString("ImagePreview.non_positive_scale_factor")); //$NON-NLS-1$
		}
	}

	protected void resetScale() {
		setScale(1.0);
	}
}

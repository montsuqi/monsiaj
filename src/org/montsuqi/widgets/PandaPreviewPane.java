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

import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

public class PandaPreviewPane extends JPanel {

	private JToolBar toolbar;
	Preview preview;

	public PandaPreviewPane() {
		super();
		setLayout(new BorderLayout());

		preview = new ImagePreview();

		toolbar = new JToolBar();
		toolbar.add(preview.getZoomInAction());
		toolbar.add(preview.getZoomOutAction());
		add(toolbar, BorderLayout.NORTH);

		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(preview);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scroll, BorderLayout.CENTER);

	}

	public void load(String fileName) throws IOException {
		preview.load(fileName);
	}

	public static void main(String[] args) throws IOException {
		JFrame f = new JFrame();
		PandaPreviewPane prev = new PandaPreviewPane();
		f.add(prev);
		f.setVisible(true);
		prev.load("C:\\Documents and Settings\\crouton\\My Documents\\My Pictures\\fmo-wall-ex1024.jpg"); //$NON-NLS-1$
	}
}

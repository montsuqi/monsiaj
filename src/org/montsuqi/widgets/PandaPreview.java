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
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

public class PandaPreview extends JPanel {

	private JToolBar toolbar;
	PandaPreviewComponent ps;

	public PandaPreview() {
		super();
		setLayout(new BorderLayout());

		toolbar = new JToolBar();
		add(toolbar, BorderLayout.NORTH);

		ps = new PandaPreviewComponent();
		JScrollPane scroll = new JScrollPane(ps);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scroll, BorderLayout.CENTER);

		toolbar.add(new AbstractAction() {
			{
				URL iconURL = getClass().getResource("/org/montsuqi/widgets/zoom-in.png"); //$NON-NLS-1$
				if (iconURL != null) {
					putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
				}
				putValue(Action.NAME, Messages.getString("PandaPSComposite.zoom_in")); //$NON-NLS-1$
			}

			public void actionPerformed(ActionEvent arg0) {
				ps.zoomIn();
			}
			
		});
		toolbar.add(new AbstractAction() {
			{
				URL iconURL = getClass().getResource("/org/montsuqi/widgets/zoom-out.png"); //$NON-NLS-1$
				if (iconURL != null) {
					putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
				}
				putValue(Action.NAME, Messages.getString("PandaPSComposite.zoom_out")); //$NON-NLS-1$
			}

			public void actionPerformed(ActionEvent arg0) {
				ps.zoomOut();
			}
			
		});
	}

	public void zoomIn() {
		ps.zoomIn();
	}

	public void zoomOut() {
		ps.zoomOut();
	}

	public void load(String fileName) {
		ps.load(fileName);
	}
}

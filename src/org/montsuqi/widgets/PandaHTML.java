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

import java.io.IOException;
import java.net.URL;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.montsuqi.util.Logger;

public class PandaHTML extends JScrollPane {

	private Logger logger;
	private JTextPane pane;

	public PandaHTML() {
		super();
		logger = Logger.getLogger(PandaHTML.class);
		pane = new JTextPane();
		setViewportView(pane);
		pane.setEditable(false);
		pane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					setURI(event.getURL());
				}
			 }
		});
	}

	public void setURI(String uri) {
		try {
			pane.setPage(uri);
		} catch (IOException e) {
			logger.warn(e);
		}
	}

	public void setURI(URL uri) {
		try {
			pane.setPage(uri);
		} catch (IOException e) {
			logger.warn(e);
		}
	}

	public URL getURI() {
		return pane.getPage();
	}
}

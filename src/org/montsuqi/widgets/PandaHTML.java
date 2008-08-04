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

package org.montsuqi.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.AttributeSet;

import com.centerkey.utils.BareBonesBrowserLaunch;

import org.montsuqi.util.Logger;

/** <p>A HTML viewer for platforms other than MacOS X.</p>
 * <p>This component uses JEditorPane to render HTML.</p>
 */
public class PandaHTML extends JPanel {

	protected static final Logger logger = Logger.getLogger(PandaHTML.class);
	protected Component html;

	public PandaHTML() {
		setLayout(new BorderLayout());
		initComponents();
	}

	protected void initComponents() {
		JEditorPane editorPane = new JEditorPane();
		AbstractDocument doc = (AbstractDocument)editorPane.getDocument();
		doc.setAsynchronousLoadPriority(1); // load documents asynchronously
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				    AttributeSet attribs = event.getSourceElement().getAttributes();
				    Enumeration attribNames = attribs.getAttributeNames();
				    AttributeSet tagAttribs = (AttributeSet)attribs.getAttribute(HTML.Tag.A);
				    String target = (String)tagAttribs.getAttribute(HTML.Attribute.TARGET);
				    if(target == null) {
					setURI(event.getURL());
				    } else {
					BareBonesBrowserLaunch.openURL(event.getURL().toString());
				    }
				}
			}
		});
		html = editorPane;
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(editorPane);
		add(scroll, BorderLayout.CENTER);
	}

	/** <p>Loads a HTML from the given URL and render it.</p>
	 * 
	 * @param uri source URL of the document.
	 */
	public void setURI(URL uri) {
		Runnable loader = createLoader(uri);
		logger.info("loading: {0}", uri); //$NON-NLS-1$
		SwingUtilities.invokeLater(loader);
	}

	protected Runnable createLoader(final URL uri) {
		return new Runnable() {
			public void run() {
				try {
					assert html instanceof JEditorPane;
					JEditorPane editorPane = (JEditorPane)html;
					editorPane.setPage(uri);
				} catch (IOException e) {
					logger.warn(e);
				}
			}
		};
	}
}

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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;

import org.montsuqi.util.Logger;

public class PandaHTML extends JScrollPane {

	protected static final Logger logger = Logger.getLogger(PandaHTML.class);
	JTextPane pane;

	public PandaHTML() {
		super();
		pane = new JTextPane();
		AbstractDocument doc = (AbstractDocument)pane.getDocument();
		doc.setAsynchronousLoadPriority(1); // load documents asynchronously
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

	public void setURI(final URL uri) {
		Runnable loader = new HTMLLoader(uri);
		SwingUtilities.invokeLater(loader);
	}

	public URL getURI() {
		return pane.getPage();
	}

	public void setText(String text) {
		pane.setText(text);
	}

	public static void main(String[] args) throws MalformedURLException {
		final JFrame frame = new JFrame();
		Container container = frame.getContentPane();
		container.setLayout(new BorderLayout());
		final PandaHTML html = new PandaHTML();
		html.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				Object[] logArgs = new Object[] {
						e.getPropertyName(),
						e.getOldValue(),
						e.getNewValue()
					};
				logger.debug("change: {0}: {1} => {2}", logArgs); //$NON-NLS-1$
			}
		});
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(html);
		container.add(scroll, BorderLayout.CENTER);

		JToolBar toolBar = new JToolBar();
		container.add(toolBar, BorderLayout.PAGE_START);
		JButton quit = new JButton("Quit"); //$NON-NLS-1$
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		toolBar.add(quit);

		final JTextField location = new JTextField();
		location.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					html.setURI(new URL(location.getText()));
				} catch (MalformedURLException ex) {
					logger.warn(ex);				}
			}

		});
		toolBar.add(location);

		frame.setSize(640, 480);
		frame.setVisible(true);
		if (args.length > 0) {
			URL uri = new URL(args[0]);
			html.setURI(uri);
		}
	}

	class HTMLLoader implements Runnable {

		URL uri;

		HTMLLoader(URL uri) {
			this.uri = uri;
		}

		public void run() {
			setText(Messages.getString("PandaHTML.loading_please_wait")); //$NON-NLS-1$
			try {
				logger.debug("loading {0}", uri); //$NON-NLS-1$
				pane.setPage(uri);
			} catch (FileNotFoundException e) {
				setText(e.toString());
			} catch (IOException e) {
				logger.warn(e);
			}
		}
	}
}

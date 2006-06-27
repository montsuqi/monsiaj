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
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class PandaPreviewPane extends JPanel implements PropertyChangeListener {

	private boolean hasLoadedImageOnce;
	private JToolBar toolbar;
	private Preview preview;
	private JFormattedTextField scaleField;

	public PandaPreviewPane() {
		super();
		hasLoadedImageOnce = false;
		setLayout(new BorderLayout());

		preview = new ImagePreview();
		preview.addPropertyChangeListener("scale", this); //$NON-NLS-1$

		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(new JLabel(Messages.getString("PandaPreviewPane.scale"))); //$NON-NLS-1$
		final String format = "###.##%"; //$NON-NLS-1$
		final NumberFormat formatter = new DecimalFormat(format);
		scaleField = new JFormattedTextField(formatter);
		scaleField.setEditable(false);
		scaleField.setValue(new Double(1.0));
		final int length = format.length();
		scaleField.setColumns(length);
		final Dimension preferredSize = scaleField.getPreferredSize();
		scaleField.setMaximumSize(preferredSize);
		scaleField.setHorizontalAlignment(SwingConstants.RIGHT);
		toolbar.add(scaleField);
		toolbar.add(preview.getFitToSizeAction());
		toolbar.add(preview.getFitToSizeHorizontallyAction());
		//toolbar.add(preview.getResetScaleAction());
		toolbar.add(preview.getZoomOutAction());
		toolbar.add(preview.getZoomInAction());
		//toolbar.add(preview.getRotateClockwiseAction());
		//toolbar.add(preview.getRotateCounterClockwiseAction());
		add(toolbar, BorderLayout.NORTH);

		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(preview);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scroll, BorderLayout.CENTER);
	}

	public void load(String fileName) throws IOException {
		preview.load(fileName);
		if ( ! hasLoadedImageOnce) {
			hasLoadedImageOnce = true;
			preview.fitToSizeHorizontally();
		}
	}

	public void clear() {
		preview.clear();
	}

	public static void main(String[] args) throws IOException {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		PandaPreviewPane preview = new PandaPreviewPane();
		f.add(preview);
		f.setVisible(true);
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(preview);
		preview.load(chooser.getSelectedFile().getAbsolutePath());
		f.pack();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		if ("scale".equals(name)) { //$NON-NLS-1$
			double newScale = ((Double)evt.getNewValue()).doubleValue();
			scaleField.setValue(new Double(newScale));
		}
	}
}

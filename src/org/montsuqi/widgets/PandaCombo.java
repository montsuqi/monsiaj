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

import javax.swing.JComboBox;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class PandaCombo extends JComboBox {
	public PandaCombo() {
		super();
		setEditor(new PandaComboBoxEditor());
	}
}

class PandaComboBoxEditor extends BasicComboBoxEditor {
	public PandaComboBoxEditor() {
		editor  = new BorderlessPandaEntry("", 9); //$NON-NLS-1$
		editor.setBorder(null);
	}

	static class BorderlessPandaEntry extends PandaEntry {
		public BorderlessPandaEntry(String value,int n) {
			super(value,n);
		}
		// workaround for 4530952
		public void setText(String s) {
			if (getText().equals(s)) {
				return;
			}
			super.setText(s);
		}
		public void setBorder(Border b) {}
	}
    
}

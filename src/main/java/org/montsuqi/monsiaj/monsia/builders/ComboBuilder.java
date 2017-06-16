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

package org.montsuqi.monsiaj.monsia.builders;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComboBox;

import org.montsuqi.monsiaj.monsia.AccelHandler;
import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;

/** <p>A builder to create combo widgets.</p>
 * <p>JComboBox does not have children while Gtk+ combo's editor is expected
 *  to be child of the combo.  To fill this gap settings for children are delegated
 *  to JComboBox's editor component.</p>
 */
class ComboBuilder extends ContainerBuilder {
        @Override
	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		Component widget = super.buildSelf(xml, parent, info);
		JComboBox combo = (JComboBox)widget;
		combo.setEditable(true);
		combo.addPopupMenuListener(new AccelHandler.Enabler());
		return widget;
	}

        @Override
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		JComboBox combo = (JComboBox)parent;
		if (info.getChildren().size() != 1) {
			throw new WidgetBuildingException("only one child for a Combo"); 
		}
		ChildInfo cInfo = info.getChild(0);
		WidgetInfo wInfo = cInfo.getWidgetInfo();
		ensureValidEntryType(wInfo.getClassName());
		Component editor = combo.getEditor().getEditorComponent();
		setCommonParameters(xml, editor, wInfo);
		setSignals(xml, editor, wInfo);
		setProperties(xml, parent, editor, wInfo.getProperties());
	}

	/** <p>Tests if the type of editor component is valid.</p>
	 * @param actualType the type given in screen definition.
	 */
	protected void ensureValidEntryType(String actualType) {
		if ( ! "Entry".equals(actualType)) { 
			throw new WidgetBuildingException("not a Entry widget"); 
		}
	}
}

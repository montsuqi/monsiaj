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

package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;

import org.montsuqi.monsia.ChildInfo;
import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.widgets.FileEntry;

public class FileEntryBuilder extends WidgetBuilder {
	
	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		FileEntry fileEntry = (FileEntry)parent;
		if (info.getChildren().size() != 1) {
			throw new WidgetBuildingException("only one child for a FileEntry"); 
		}
		ChildInfo cInfo = info.getChild(0);
		WidgetInfo wInfo = cInfo.getWidgetInfo();
		Component entry = fileEntry.getEntry();
		setCommonParameters(xml, entry, wInfo);
		setSignals(xml, entry, wInfo);
		setProperties(xml, parent, entry, wInfo.getProperties());
	}

}

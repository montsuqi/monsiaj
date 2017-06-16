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
import java.util.Map;

import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.util.ParameterConverter;
import org.montsuqi.monsiaj.widgets.TableConstraints;
import org.montsuqi.monsiaj.widgets.TableLayout;

/** <p>A builder to create Gtk+'s Table container widget.</p>
 * <p>This class uses TableLayout as its layout manager.</p>
 */
class TableBuilder extends ContainerBuilder {

	private static final String LEFT_ATTACH_KEY = "left_attach"; 
	private static final String RIGHT_ATTACH_KEY = "right_attach"; 
	private static final String TOP_ATTACH_KEY = "top_attach"; 
	private static final String BOTTOM_ATTACH_KEY = "bottom_attach"; 
	private static final String XEXPAND_KEY = "xexpand"; 
	private static final String YEXPAND_KEY = "yexpand"; 
	private static final String XSHRINK_KEY = "xshrink"; 
	private static final String YSHRINK_KEY = "yshrink"; 
	private static final String XPAD_KEY = "xpad"; 
	private static final String YPAD_KEY = "ypad"; 
	private static final String X_KEY = "x"; 
	private static final String Y_KEY = "y"; 

	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		TableLayout layout = (TableLayout)parent.getLayout();
		for (int i = 0, n = info.getChildren().size(); i < n; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Component child = null;
			TableConstraints tc = new TableConstraints();
			child = buildWidget(xml, wInfo, parent);
			Map properties = cInfo.getProperties();
			if (properties.containsKey(LEFT_ATTACH_KEY)) {
				tc.leftAttach = ParameterConverter.toInteger((String)properties.get(LEFT_ATTACH_KEY));
			}
			if (properties.containsKey(RIGHT_ATTACH_KEY)) {
				tc.rightAttach = ParameterConverter.toInteger((String)properties.get(RIGHT_ATTACH_KEY));
			}
			if (properties.containsKey(TOP_ATTACH_KEY)) {
				tc.topAttach = ParameterConverter.toInteger((String)properties.get(TOP_ATTACH_KEY));
			}
			if (properties.containsKey(BOTTOM_ATTACH_KEY)) {
				tc.bottomAttach = ParameterConverter.toInteger((String)properties.get(BOTTOM_ATTACH_KEY));
			}
			if (properties.containsKey(X_KEY)) {
				// x_options = ParameterConverter.toInteger(value);
			}
			if (properties.containsKey(Y_KEY)) {
				// y_options = ParameterConverter.toInteger(value);
			}
			if (properties.containsKey(XEXPAND_KEY)) {
				tc.xExpand = ParameterConverter.toBoolean((String)properties.get(XEXPAND_KEY));
			}
			if (properties.containsKey(YEXPAND_KEY)) {
				tc.yExpand = ParameterConverter.toBoolean((String)properties.get(YEXPAND_KEY));
			}
			if (properties.containsKey(XSHRINK_KEY)) {
				tc.xShrink = ParameterConverter.toBoolean((String)properties.get(XSHRINK_KEY));
			}
			if (properties.containsKey(YSHRINK_KEY)) {
				tc.yShrink = ParameterConverter.toBoolean((String)properties.get(YSHRINK_KEY));
			}
			if (properties.containsKey(XPAD_KEY)) {
				tc.xPadding = ParameterConverter.toInteger((String)properties.get(XPAD_KEY));
			}
			if (properties.containsKey(YPAD_KEY)) {
				tc.yPadding = ParameterConverter.toInteger((String)properties.get(YPAD_KEY));
			}
			parent.add(child);
			layout.setConstraints(child, tc);
		}
	}
}

package org.montsuqi.client;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import org.montsuqi.util.Logger;
import org.montsuqi.monsia.Style;

final class WidgetValueManager {
	private Logger logger;
	private Protocol con;
	private Map styles;
	private Map valueTable;

	WidgetValueManager(Protocol con, Map styles) {
		this.con = con;
		this.styles = styles;
		logger = Logger.getLogger(WidgetValueManager.class);
		valueTable = new HashMap();
	}

	Protocol getProtocol() {
		return con;
	}

	void registerValue(Component widget, String valueName, Object opt) {
		String longName = con.getInterface().getLongName(widget);
		ValueAttribute va = (ValueAttribute)(valueTable.get(longName));

		if (va == null) {
			va = new ValueAttribute(longName, valueName, con.getWidgetNameBuffer().toString(), con.getLastDataType(), opt);
			valueTable.put(va.getKey(), va);
		} else {
			va.setNameSuffix(valueName);
			va.setOpt(con.getLastDataType(), opt);
		}
	}

	ValueAttribute getValue(String name) {
		return (ValueAttribute)(valueTable.get(name));
	}
	void setStyle(Component widget, String styleName) {
		Style style = (Style)styles.get(styleName);
		if (style != null) {
			style.apply(widget);
		} else {
			String widgetName = widget == null ? "null" : widget.getName(); //$NON-NLS-1$
			logger.debug(Messages.getString("WidgetMarshaller.ignoring_style"), new Object[] { styleName, widgetName }); //$NON-NLS-1$
		}
	}

}

package org.montsuqi.client.marshallers;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import org.montsuqi.util.Logger;
import org.montsuqi.client.Messages;
import org.montsuqi.client.Protocol;
import org.montsuqi.monsia.Style;

public final class WidgetValueManager {
	private Logger logger;
	private Protocol con;
	private Map styles;
	private Map valueTable;

	public WidgetValueManager(Protocol con, Map styles) {
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

		ValueAttribute va;
		if ( ! valueTable.containsKey(longName)) {
			String name = con.getWidgetNameBuffer().toString();
			va = new ValueAttribute(name, con.getLastDataType(), valueName, opt);
			valueTable.put(va.getName(), va);
		} else {
			va = (ValueAttribute)valueTable.get(longName);
			va.setVName(valueName);
			int lastType = con.getLastDataType();
			synchronized (va) {
				va.setType(lastType);
				va.setOpt(opt);
			}
		}
	}

	ValueAttribute getValue(String name) {
		if (valueTable.containsKey(name)) {
			ValueAttribute va = (ValueAttribute)valueTable.get(name);
			return va;
		} else {
			throw new IllegalArgumentException("no such value name:" + name);
		}
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

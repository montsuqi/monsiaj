package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.util.Map;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.ParameterConverter;

final class CheckButtonBuilder extends WidgetBuilder {

	Component buildSelf(final Interface xml, final Container parent, final WidgetInfo info) {
		if (looksLikeToggleButton(info)) {
			info.setClassName("ToggleButton");
		}
		return super.buildSelf(xml, parent, info);
	}

	private boolean looksLikeToggleButton(final WidgetInfo info) {
		final Map props = info.getProperties();
		return props.containsKey("draw_indicator") && ! ParameterConverter.toBoolean(info.getProperty("draw_indicator"));
	}
}

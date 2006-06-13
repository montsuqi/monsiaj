package org.montsuqi.client.marshallers;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.widgets.PandaHTML;

public class PandaHTMLMarshaller extends WidgetMarshaller {

	public void receive(WidgetValueManager manager, Component widget) throws IOException {
		final Protocol con = manager.getProtocol();
		final PandaHTML html = (PandaHTML)widget;

		con.receiveDataTypeWithCheck(Type.RECORD);
		for (int i = 0, n = con.receiveInt(); i < n; i++) {
			/* String dummy = */ con.receiveName();
			final String text = con.receiveStringData();
			try {
				final URL url = new URL(text);
				html.setURI(url);
			} catch (MalformedURLException e) {
				logger.info(e);
			}
		}
}

	public void send(WidgetValueManager manager, String name, Component widget) throws IOException {
		// do nothing
	}

}

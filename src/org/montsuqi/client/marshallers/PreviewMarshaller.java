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

package org.montsuqi.client.marshallers;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;
import org.montsuqi.widgets.PandaPreviewPane;

/** <p>A class to send/receive Preview data.</p>
 */
class PreviewMarshaller extends WidgetMarshaller {

	private static final String TEMP_PREFIX = "pandapreview"; //$NON-NLS-1$
	private static final String TEMP_SUFFIX = ".png"; //$NON-NLS-1$
	
	public synchronized void receive(WidgetValueManager manager, Component widget) throws IOException {
		Protocol con = manager.getProtocol();
		PandaPreviewPane preview = (PandaPreviewPane)widget;

		con.receiveDataTypeWithCheck(Type.RECORD);
		for (int i = 0, n = con.receiveInt(); i < n; i++) {
			/* String dummy = */ con.receiveName();
			byte[] bin = con.receiveBinaryData();
			File temp = File.createTempFile(TEMP_PREFIX, TEMP_SUFFIX);
			temp.deleteOnExit();
			OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
			out.write(bin);
			out.flush();
			out.close();
			preview.load(temp.getAbsolutePath());
		}
	}

	public void send(WidgetValueManager manager, String name, Component widget) throws IOException {
		// do nothing
	}
}

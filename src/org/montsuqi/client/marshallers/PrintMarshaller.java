/*      PANDA -- a simple transaction monitor

Copyright (C) 2010 JMA (Japan Medical Association).

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
import java.io.IOException;

import org.montsuqi.client.Protocol;
import org.montsuqi.client.Type;

/** <p>A class to send/receive Timer data.</p>
 */
public class PrintMarshaller extends WidgetMarshaller {

    public void receive(WidgetValueManager manager, Component widget) throws IOException {
        Protocol con = manager.getProtocol();

        con.receiveDataTypeWithCheck(Type.RECORD);
        for (int i = 0, n = con.receiveInt(); i < n; i++) {
            String name = con.receiveName();
            if ("item".equals(name)) {
                con.receiveDataTypeWithCheck(Type.ARRAY);
                for (int j = 0, n2 = con.receiveInt(); j < n2; j++) {
                    String path = "";
                    String title = "";
                    con.receiveDataTypeWithCheck(Type.RECORD);
                    for (int k = 0, n3 = con.receiveInt(); k < n3; k++) {
                        String name2 = con.receiveName();
                        if ("path".equals(name2)) { //$NON-NLS-1$
                            path = con.receiveStringData();
                        } else if ("title".equals(name2)) {
                            title = con.receiveStringData();
                        }
                    }
                    if (!path.isEmpty() && !title.isEmpty()) {
                        con.addPrintRequest(path, title);
                    }
                }
            }
        }
    }

    public void send(WidgetValueManager manager, String name, Component widget) throws IOException {
        // do nothing
    }
}

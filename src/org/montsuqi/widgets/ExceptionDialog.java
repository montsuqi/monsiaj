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

import java.io.IOException;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;

import javax.net.ssl.SSLException;
import javax.swing.JOptionPane;

import org.montsuqi.client.Messages;
import org.montsuqi.util.StringUtils;

public class ExceptionDialog extends JOptionPane {

	// inhibit instantiation
	private ExceptionDialog() {
		// do nothing
	}

	public static void showExceptionDialog(Throwable e) {
		Throwable t = e;
		Throwable cause;
		while ((cause = t.getCause()) != null) {
			t = cause;
		}
		final String exceptionDialogTitle = Messages.getString("Launcher.exception_dialog_title"); //$NON-NLS-1$
		final String[] messageArgs = new String[3];

		if (t instanceof SSLException) {
			messageArgs[0] = Messages.getString("Launcher.ssl_exception_message"); //$NON-NLS-1$
		} else if (t instanceof SocketException) {
			messageArgs[0] = Messages.getString("Launcher.socket_exception_message"); //$NON-NLS-1$
		} else if (t instanceof IOException) {
			messageArgs[0] = Messages.getString("Launcher.io_exception_message"); //$NON-NLS-1$
		} else if (t instanceof GeneralSecurityException) {
			messageArgs[0] = Messages.getString("Launcher.security_exception_message"); //$NON-NLS-1$
		} else {
			messageArgs[0] = Messages.getString("Launcher.generic_exception_message"); //$NON-NLS-1$
		}
		messageArgs[1] = t.getClass().getName().replaceAll(".*\\.", ""); //$NON-NLS-1$ //$NON-NLS-2$
		messageArgs[2] = StringUtils.escapeHTML(t.getMessage()).replaceAll("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
		final String format = Messages.getString("Launcher.exception_message_format"); //$NON-NLS-1$
		final String exceptionMessage = MessageFormat.format(format, messageArgs);
		JOptionPane.showMessageDialog(null, exceptionMessage, exceptionDialogTitle, JOptionPane.ERROR_MESSAGE);
	}
}

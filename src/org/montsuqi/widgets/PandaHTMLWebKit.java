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

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public class PandaHTMLWebKit extends PandaHTML {

	private static Class WebKitJava;
	static Method sendMessage;
	static Integer loadURL;

	static {
		try {
			WebKitJava = Class.forName("org.dm.webkit.WebKitJava"); //$NON-NLS-1$
			Class[] types = { Integer.TYPE, Object.class };
			sendMessage = WebKitJava.getMethod("sendMessage", types); //$NON-NLS-1$
			Field loadURLField = WebKitJava.getField("loadURL"); //$NON-NLS-1$
			loadURL = new Integer(loadURLField.getInt(null));
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		} catch (NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		} catch (NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		} catch (IllegalArgumentException e) {
			throw new ExceptionInInitializerError(e);
		} catch (IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	protected void initComponents() {
		try {
			html = (Component)WebKitJava.newInstance();
			add(html, BorderLayout.CENTER);
		} catch (InstantiationException e) {
			logger.fatal(e);
			html = null;
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			html = null;
		}
	}

	protected Runnable createLoader(final URL uri) {
		return new Runnable() {
			public void run() {
				while ( ! html.isDisplayable()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// ignore
					}
				}
				try {
					Object[] params = { loadURL, uri.toExternalForm() };
					sendMessage.invoke(html, params);
				} catch (InvocationTargetException e) {
					logger.fatal(e);
				} catch (IllegalArgumentException e) {
					logger.fatal(e);
				} catch (IllegalAccessException e) {
					logger.fatal(e);
				}
			}
		};
	}
}


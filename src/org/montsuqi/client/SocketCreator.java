/*
 * monsiaj
 * org.montsuqi.client.SSLSocketFactory
 * Copyright (C) 2003 crouton
 *
 * $Id: SocketCreator.java,v 1.1 2003-11-13 06:17:38 ozawa Exp $
 */
package org.montsuqi.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

class SocketCreator {
	static Socket create(String host, int port, Object[] options) throws IOException, UnknownHostException {
		return new Socket(host, port);
	}
}

/*
 * monsiaj
 * org.montsuqi.client.SSLSocketCreator
 * Copyright (C) 2003 crouton
 *
 * $Id: SSLSocketCreator.java,v 1.1 2003-11-13 06:17:38 ozawa Exp $
 */
package org.montsuqi.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class SSLSocketCreator extends SocketCreator {
	static Socket create(String host, int port, Object[] options) throws IOException, UnknownHostException {
		Socket s = new Socket(host, port);
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket ssl = (SSLSocket)factory.createSocket(s, host, port, true);
		/* key, cert, capath, cafile */
		boolean verify = ((Boolean)options[0]).booleanValue();
		ssl.setNeedClientAuth(verify);
		return ssl;
	}
}

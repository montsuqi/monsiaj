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

package org.montsuqi.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.montsuqi.util.Logger;
import org.montsuqi.util.OptionParser;

public class Client implements Runnable {

	private boolean connected;
	public static final int PORT_GLTERM = 8000;
	private static final String CLIENT_VERSION = "0.0"; //$NON-NLS-1$
	private static final String PANDA_SCHEME = "panda:"; //$NON-NLS-1$

	private int portNumber;
	private String host;
	private String cache;
	private String user;
	private String pass;
	private String encoding;
	private String styles;
	private String currentApplication;

	// if USE_SSL
	//private String key;
	//private String cert;
	private boolean useSSL;
	private boolean verify;
	//private String CApath;
	//private String CAfile;

	private Protocol protocol;
	private Logger logger;

	public Client() {
		logger = Logger.getLogger(Client.class);
	}

	private static Client parseCommandLine(String[] args) {
		Client client = new Client();

		String[] files = client.parseOptions(args);

		if (files.length > 0) {
			client.setCurrentApplication(files[0]);
		} else {
			client.setCurrentApplication(null);
		}
		return client;
	}

	public void connect() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnknownHostException, IOException {
		Socket s = null;
		connected = false;
		String factoryName;
		Object[] options;
		if (useSSL) {
			factoryName = "org.montsuqi.client.SSLSocketCreator"; //$NON-NLS-1$
			options = new Object[] { new Boolean(verify) };
		} else {
			factoryName = "org.montsuqi.client.SocketCreator"; //$NON-NLS-1$
			options = null;
		}
		Class clazz = Class.forName(factoryName);
		SocketCreator creator = (SocketCreator)clazz.newInstance();
		s = creator.create(host, portNumber, options);
		protocol = new Protocol(this, s);
		protocol.sendConnect(user, pass, currentApplication);
		connected = true;
	}

	public void run() {
		try {
			protocol.checkScreens(true);
			protocol.getScreenData();
			while (connected) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// ignore
				}
			}
			protocol.close();
		} catch (IOException e) {
			logger.fatal(e);
		}
	}
		
	void exitSystem() {
		try {
			synchronized (this) {
				protocol.sendPacketClass(PacketClass.END);
				connected = false;
			}
		} catch (Exception e) {
			logger.warn(e);
		} finally {
			System.exit(0);
		}
	}

	private String[] parseOptions(String[] args) {
		OptionParser options = new OptionParser();

		options.add("port", Messages.getString("Client.port_number"), PORT_GLTERM); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("host", Messages.getString("Client.host_name"), "localhost"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		options.add("cache", Messages.getString("Client.cache_directory"), System.getProperty("user.home") + System.getProperty("file.separator") + "cache"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		options.add("user", Messages.getString("Client.user_name"), System.getProperty("user.name")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		options.add("pass", Messages.getString("Client.password"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		options.add("encoding", Messages.getString("Client.server_character_encoding"), "EUC-JP"); //$NON-LNS-1$ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		options.add("style", Messages.getString("Client.styles"), ""); //$NON-LNS-1$ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		options.add("v1", Messages.getString("Client.use_protocol_version_1"), true); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("v2", Messages.getString("Client.use_protocol_version_2"), true); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("useSSL", "SSL", false); //$NON-NLS-1$ //$NON-NLS-2$
		//options.add("key", "key file name(pem)", null);
		//options.add("cert", "certification file name(pem)", null);
		//options.add("ssl", "use SSL", false);
		options.add("verifypeer", Messages.getString("Client.verify_peer"), false); //$NON-NLS-1$ //$NON-NLS-2$
		//options.add("CApath", "path to CA", null);
		//options.add("CAfile", "CA file", null);

		String[] files = options.parse(Client.class.getName(), args);

		setPortNumber(((Integer)options.getValue("port")).intValue()); //$NON-NLS-1$
		setHost((String)options.getValue("host")); //$NON-NLS-1$
		setCache((String)options.getValue("cache")); //$NON-NLS-1$
		setUser((String)options.getValue("user")); //$NON-NLS-1$
		setPass((String)options.getValue("pass")); //$NON-NLS-1$
		setEncoding((String)options.getValue("encoding")); //$NON-NLS-1$
		setStyles((String)options.getValue("style")); //$NON-NLS-1$
		setUseSSL(((Boolean)options.getValue("useSSL")).booleanValue()); //$NON-NLS-1$

		if (useSSL) {
			//key = (String)options.getValue("key");
			//cert = (String)options.getValue("cert");
			//useSSL = ((Boolean)options.getValue("ssl")).booleanValue();
			setVerify(((Boolean)options.getValue("verifypeer")).booleanValue()); //$NON-NLS-1$
			//CApath = options.getValue("CApath");
			//CAfile = options.getValue("CAfile");
		}
		return files;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setCache(String cache) {
		this.cache = cache;
	}

	String getCacheFileName(String name) {
		String sep = System.getProperty("file.separator"); //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		buf.append(cache);
		buf.append(sep);
		buf.append(host);
		buf.append(sep);
		buf.append(portNumber);
		buf.append(sep);
		buf.append(name);
		return buf.toString();
	}
	
	public void setUser(String user) {
		this.user = user;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setStyles(String styles) {
		this.styles = styles;
	}

	public String getStyles() {
		return styles;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public void setVerify(boolean verify) {
		this.verify = verify;
	}

	public void setCurrentApplication(String application) {
		if (application == null) {
			currentApplication = "demo"; //$NON-NLS-1$
		} else if ( ! application.startsWith(PANDA_SCHEME)) {
			currentApplication = PANDA_SCHEME + application;
		} else {
			currentApplication = application;
		}
	}

	public void finalize() {
		if (protocol != null) {
			exitSystem();
		}
	}
	
	public static void main(String[] args) {
		try {
			String format = Messages.getString("Client.banner_format"); //$NON-NLS-1$
			Object[] bannerArgs = new Object[] { CLIENT_VERSION };
			String banner = MessageFormat.format(format, bannerArgs);
			System.out.println(banner);

			Client client = Client.parseCommandLine(args);
			client.connect();
			Thread t = new Thread(client);
			t.start();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}

package org.montsuqi.client;

import java.io.IOException;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.montsuqi.util.Logger;
import org.montsuqi.util.OptionParser;

public class Client implements Runnable {

	static final int PORT_GLTERM = 8000;
	private static final String CLIENT_VERSION = "0.0";

	private int portNumber;
	private String host;
	private String cache;
	private String user;
	private String pass;

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

	public String getCacheFileName(String name) {
		String sep = System.getProperty("path.separator");
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
	
	private String[] parseOptions(String[] args) {
		OptionParser options = new OptionParser();

		options.add("port", "ポート番号", PORT_GLTERM);
		options.add("host", "ホスト名", "localhost");
		options.add("cache", "キャッシュディレクトリ名", "cache");
		options.add("user", "ユーザ名", System.getProperty("user.name"));
		options.add("pass", "パスワード", "");
		options.add("v1", "データ処理プロトコルバージョン 1 を使う", true);
		options.add("v2", "データ処理プロトコルバージョン 2 を使う", true);

		options.add("useSSL", "SSL", false);
		//options.add("key", "鍵ファイル名(pem)", null);
		//options.add("cert", "証明書ファイル名(pem)", null);
		//options.add("ssl", "SSLを使う", false);
		options.add("verifypeer", "クライアント証明書の検証を行う", false);
		//options.add("CApath", "CA証明書へのパス", null);
		//options.add("CAfile", "CA証明書ファイル", null);

		String[] files = options.parse(Client.class.getName(), args);

		portNumber = ((Integer)options.getValue("port")).intValue();
		host = (String)options.getValue("host");
		cache = (String)options.getValue("cache");
		user = (String)options.getValue("user");
		pass = (String)options.getValue("pass");

		useSSL = ((Boolean)options.getValue("useSSL")).booleanValue();

		if (useSSL) {
			//key = (String)options.getValue("key");
			//cert = (String)options.getValue("cert");
			//useSSL = ((Boolean)options.getValue("ssl")).booleanValue();
			verify = ((Boolean)options.getValue("verifypeer")).booleanValue();
			//CApath = options.getValue("CApath");
			//CAfile = options.getValue("CAfile");
		}

		return files;
	}

	public Client(String[] args) {
		logger = Logger.getLogger(Client.class);

		String[] files = parseOptions(args);

		if (files.length > 0) {
			currentApplication = files[0];
		} else {
			currentApplication = "demo";
		}
		
		Socket s = null;
		try {
			s = new Socket(host, portNumber);
			// if USE_SSL
			if (useSSL) {
				SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
				SSLSocket ssl = (SSLSocket)factory.createSocket(s, host, portNumber, true);
				/* key, cert, capath, cafile */
				ssl.setNeedClientAuth(verify);
				s = ssl;
			}
			logger.info("socket: {0}", s);
			
			protocol = new Protocol(this, s);
			logger.info(protocol.toString());
		} catch (IOException e) {
			logger.fatal(e);
			System.exit(0);
		}
		if (protocol == null) {
			logger.fatal("cannot connect");
		}
	}
		

	public void run() {
		try {
			logger.info("sendConnect({0}, {1}, {2})...", new Object[] { user, pass, currentApplication });
			protocol.sendConnect(user, pass, currentApplication);
			logger.info("done.");
			while (true) {
				logger.info("checkScreens(true)");
				protocol.checkScreens(true);
				logger.info("getScreenData()");
				protocol.getScreenData();
			}
		} catch (IOException e) {
			logger.fatal(e);
		}
	}
		
	public static void main(String[] args) {
		showBannar();
		Client client = new Client(args);
		client.run();
		client.exitSystem();
	}

	public void finalize() {
		if (protocol != null) {
			exitSystem();
		}
	}
	
	public void exitSystem() {
		try {
			protocol.sendPacketClass(PacketClass.END);
			protocol.close();
			System.exit(0);
		} catch (IOException e) {
			logger.fatal(e);
		}
	}

	protected static void showBannar() {
		System.out.println("glclient java ver " + CLIENT_VERSION);
		System.out.println("Copyright (c) 1998-1999 Masami Ogoshi <ogochan@nurs.or.jp>");
		System.out.println("              2000-2002 Masami Ogoshi & JMA.");
	}
}

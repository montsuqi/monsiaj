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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;

public class BrowserSSLSocketFactory extends SSLSocketFactory {

	private SSLSocketFactory factory;
	
	private static final ClassLoader classLoader;
	private static final Logger logger = Logger.getLogger(BrowserSSLSocketFactory.class);
	static {
		ClassLoader cl = null;
		String javaHomePath = System.getProperty("java.home");
		File deployJarFile = SystemEnvironment.createFilePath(new String[] {
				javaHomePath, "lib", "deploy.jar"
		});
		File rtJarFile = SystemEnvironment.createFilePath(new String[] {
				javaHomePath, "lib", "rt.jar"
		});
		if (deployJarFile.exists()) {
			try {
				URL[] urls = new URL[] {
					deployJarFile.toURL(),
					rtJarFile.toURL()
				};
				cl = new URLClassLoader(urls);
			} catch (MalformedURLException e) {
				logger.warn("{0} could not be transformed to URL.", deployJarFile);
			}
		} else {
			logger.warn("{0} not found.", deployJarFile);
		}
		classLoader = cl;
	}

	public BrowserSSLSocketFactory() throws GeneralSecurityException, ClassNotFoundException {
		try {
//			Class.forName("com.sun.deploy.security.WIExplorerMyKeyStore", true, classLoader);
//			Class.forName("sun.security.jca.GetInstance", true, classLoader);
//			Class.forName("java.security.KeyStore", true, classLoader);
//			Class.forName("java.security.KeyStoreSpi", true, classLoader);
//			Class.forName("java.security.Provider", true, classLoader);
//			Class.forName("java.security.Provider$Service", true, classLoader);
//			Class.forName("javax.net.ssl.TrustManagerFactory", true, classLoader);

			//ServiceManager.setService(33024);
			Class serviceManager;
			serviceManager = Class.forName("com.sun.deploy.services.ServiceManager", true, classLoader);
			Method setService = serviceManager.getMethod("setService", new Class[] { Integer.TYPE });
			setService.invoke(null, new Object[] { new Integer(33024) });

			//BrowserKeystore.registerSecurityProviders();
			Class browserKeyStore = Class.forName("com.sun.deploy.security.BrowserKeystore", true, classLoader);
			Method registerSecurityProviders = browserKeyStore.getMethod("registerSecurityProviders", null);
			registerSecurityProviders.invoke(null, null);

			synchronized (this) {
				Thread.currentThread().setContextClassLoader(classLoader);
				SSLContext ctx;
				ctx = SSLContext.getInstance("SSL");
				Class x509DeployTrustManager = Class.forName("com.sun.deploy.security.X509DeployTrustManager", true, classLoader);
				TrustManager tm = (TrustManager)x509DeployTrustManager.newInstance();
				TrustManager[] tms = new TrustManager[] { tm };
				Class x509DeployKeyManager = Class.forName("com.sun.deploy.security.X509DeployKeyManager", true, classLoader);
				KeyManager km = (KeyManager)x509DeployKeyManager.newInstance();
				KeyManager[] kms = new KeyManager[] { km };
	
				//			KeyStore ks = KeyStore.getInstance("PKCS12");
				//			String pass = conf.getClientCertificatePass();
				//			FileInputStream fis = new FileInputStream(getTrustStorePath());
				//			ks.load(fis, pass.toCharArray());
				//			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				//			kmf.init(ks, pass.toCharArray());
				//			KeyManager[] kms = kmf.getKeyManagers();
				ctx.init(kms, tms, null);
				factory = ctx.getSocketFactory();
			}
		} catch (Exception e) {
			if (e instanceof ClassNotFoundException) {
				throw (ClassNotFoundException)e;
			}
			if (e instanceof GeneralSecurityException) {
				throw (GeneralSecurityException)e;
			} else {
				GeneralSecurityException gse = new GeneralSecurityException();
				gse.initCause(e);
				throw gse;
			}
		}
	}

	public String[] getDefaultCipherSuites() {
		return factory.getDefaultCipherSuites();
	}

	public String[] getSupportedCipherSuites() {
		return factory.getSupportedCipherSuites();
	}

	public Socket createSocket(Socket socket, String host, int port, boolean flag) throws IOException {
		return factory.createSocket(socket, host, port, flag);
	}

	public Socket createSocket(String arg0, int arg1) throws IOException,
			UnknownHostException {
		return factory.createSocket(arg0, arg1);
	}

	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		return factory.createSocket(arg0, arg1, arg2, arg3);
	}

	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return factory.createSocket(arg0, arg1);
	}

	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
			int arg3) throws IOException {
		return factory.createSocket(arg0, arg1, arg2, arg3);
	}
}

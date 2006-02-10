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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.sun.deploy.security.BrowserKeystore;
import com.sun.deploy.security.X509DeployKeyManager;
import com.sun.deploy.security.X509DeployTrustManager;
import com.sun.deploy.services.ServiceManager;

public class BrowserSSLSocketFactory extends SSLSocketFactory {

	private SSLSocketFactory factory;
	
	public BrowserSSLSocketFactory() {
		ServiceManager.setService(33024);
		BrowserKeystore.registerSecurityProviders();

		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("SSL");
			X509DeployTrustManager tm = new X509DeployTrustManager();
			TrustManager[] tms = new TrustManager[] { tm };
			
			X509DeployKeyManager km = new X509DeployKeyManager();
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
		} catch (NoSuchAlgorithmException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
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

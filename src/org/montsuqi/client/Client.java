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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.x500.X500Principal;

import org.montsuqi.monsia.Style;
import org.montsuqi.util.Logger;
import org.montsuqi.util.OptionParser;
import org.montsuqi.util.SystemEnvironment;

public class Client implements Runnable {

	private Configuration conf;
	Logger logger;
	private Protocol protocol;

	private static final String CLIENT_VERSION = "0.0"; //$NON-NLS-1$

	public Client(Configuration conf) {
		this.conf = conf;
		logger = Logger.getLogger(Client.class);
	}

	private static Client parseCommandLine(String[] args) {
		OptionParser options = new OptionParser();
		options.add("port", Messages.getString("Client.port_number"), Configuration.DEFAULT_PORT); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("host", Messages.getString("Client.host_name"), Configuration.DEFAULT_HOST); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("cache", Messages.getString("Client.cache_directory"), Configuration.DEFAULT_CACHE_PATH); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("user", Messages.getString("Client.user_name"), Configuration.DEFAULT_USER); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("pass", Messages.getString("Client.password"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		options.add("encoding", Messages.getString("Client.server_character_encoding"), Configuration.DEFAULT_ENCODING); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("style", Messages.getString("Client.styles"), Configuration.DEFAULT_STYLES); //$NON-NLS-1$ //$NON-NLS-2$
		int protocolVersion = Configuration.DEFAULT_PROTOCOL_VERSION;
		options.add("v1", Messages.getString("Client.use_protocol_version_1"), protocolVersion == 1); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("v2", Messages.getString("Client.use_protocol_version_2"), protocolVersion == 2); //$NON-NLS-1$ //$NON-NLS-2$
		options.add("useSSL", "SSL", Configuration.DEFAULT_USE_SSL); //$NON-NLS-1$ //$NON-NLS-2$

		String[] files = options.parse(Client.class.getName(), args);
		
		Configuration conf = new Configuration(Client.class);
		conf.setPort(options.getInt("port")); //$NON-NLS-1$
		conf.setHost(options.getString("host")); //$NON-NLS-1$
		conf.setCache(options.getString("cache")); //$NON-NLS-1$
		conf.setUser(options.getString("user")); //$NON-NLS-1$
		conf.setPass(options.getString("pass")); //$NON-NLS-1$
		conf.setEncoding(options.getString("encoding")); //$NON-NLS-1$
		conf.setStyleFileName(options.getString("style")); //$NON-NLS-1$

		boolean v1 = options.getBoolean("v1"); //$NON-NLS-1$
		boolean v2 = options.getBoolean("v2"); //$NON-NLS-1$
		if ( ! (v1 ^ v2)) {
			throw new IllegalArgumentException("specify -v1 or -v2, not both."); //$NON-NLS-1$
		}
		if (v1) {
			conf.setProtocolVersion(1);
		} else if (v2) {
			conf.setProtocolVersion(2);
		} else {
			assert false : "-v1 or -v2 should have been given."; //$NON-NLS-1$
		}

		conf.setUseSSL(options.getBoolean("useSSL")); //$NON-NLS-1$

		conf.setApplication(files.length > 0 ? files[0] : null);

		return new Client(conf);
	}

	void connect() throws IOException {
		String encoding = conf.getEncoding();
		Map styles = loadStyles();
		String[] pathElements = {
			conf.getCache(),
			conf.getHost(),
			String.valueOf(conf.getPort())
		};
		File cacheRoot = SystemEnvironment.createFilePath(pathElements);
		int protocolVersion = conf.getProtocolVersion();
		protocol = new Protocol(this, encoding, styles, cacheRoot, protocolVersion);

		String user = conf.getUser();
		String password = conf.getPass();
		String application = conf.getApplication();
		protocol.sendConnect(user, password, application);
	}

	private Map loadStyles() {
		URL url = conf.getStyleURL();
		try {
			logger.info("loading styles from URL: {0}", url); //$NON-NLS-1$
			InputStream in = url.openStream();
			return Style.load(in);
		} catch (IOException e) {
			logger.debug(e);
			logger.info("using empty style set"); //$NON-NLS-1$
			return Collections.EMPTY_MAP;
		}
	}

	Socket createSocket() throws IOException {
		String host = conf.getHost();
		int port = conf.getPort();
		SocketAddress address = new InetSocketAddress(host, port);
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(address);
		Socket socket = socketChannel.socket();
		if ( ! conf.getUseSSL()) {
			return socket;
		}
		try {
			final SSLSocket sslSocket = createSSLSocket(host, port, socket);
			final SSLSession session = sslSocket.getSession();
			logger.debug("Checking local certificate chain"); //$NON-NLS-1$
			validateLocalCertificates(session.getLocalCertificates());
			logger.debug("Checking peer certificate chain"); //$NON-NLS-1$
			validatePeerCertificates(session.getPeerCertificates());
			return sslSocket;
		} catch (SocketException e) {
			if (isMissingPassphraseMessage(e.getMessage())) {
				final String message = Messages.getString("Client.client_certificate_password_maybe_invalid"); //$NON-NLS-1$
				throw new SSLException(message);
			} else {
				throw e;
			}
		} catch (IOException e) {
			if (isBrokenPipeMessage(e.getMessage())) {
				throw new IOException(Messages.getString("Client.broken_pipe")); //$NON-NLS-1$
			}
			throw e;
		}
	}

	private boolean isBrokenPipeMessage(String message) {
		return message.toLowerCase().startsWith("broken pipe"); //$NON-NLS-1$
	}

	private boolean isMissingPassphraseMessage(String message) {
		return message.indexOf("Default SSL context init failed") >= 0 && //$NON-NLS-1$
			message.indexOf("/ by zero") >= 0; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void validateLocalCertificates(final Certificate[] certificates) throws SSLException {
		if (certificates == null) {
			return;
		}
		for (int i = 0; i < certificates.length; i++) {
			if (certificates[i] instanceof X509Certificate) {
				checkCertificateExpiration(((X509Certificate)certificates[i]),
					Messages.getString("Client.client_certificate_expired_format"),
					Messages.getString("Client.client_certificate_not_yet_valid_format"));
			}
		}
	}

	private void validatePeerCertificates(final Certificate[] certificates) throws SSLException {
		if (certificates == null) {
			return;
		}
		for (int i = 0; i < certificates.length; i++) {
			if (certificates[i] instanceof X509Certificate) {
				checkCertificateExpiration((X509Certificate)certificates[i],
					Messages.getString("Client.server_certificate_expired_format"),
					Messages.getString("Client.server_certificate_not_yet_valid_format"));
			}
		}
		final Certificate serverCertificate = certificates[0];
		if (serverCertificate instanceof X509Certificate) {
			checkHostNameInCertificate((X509Certificate)serverCertificate);
		} else {
			logger.warn("Server Certificate is not a X.509 Certificate");
		}
	}

	private void checkCertificateExpiration(final X509Certificate certificate,
			final String expiredMessageFormat,
			final String notYetValidMessageFormat) throws SSLException {
		try {
			certificate.checkValidity();
		} catch (CertificateExpiredException e) {
			final Object[] args = { certificate.getSubjectDN(), certificate.getNotAfter() };
			final String message = MessageFormat.format(expiredMessageFormat, args);
			throw new SSLException(message);
		} catch (CertificateNotYetValidException e) {
			final Object[] args = { certificate.getSubjectDN(), certificate.getNotBefore() };
			final String message = MessageFormat.format(notYetValidMessageFormat, args);
			throw new SSLException(message);
		}
	}

	private void checkHostNameInCertificate(final X509Certificate certificate) throws SSLPeerUnverifiedException {
		final String host = conf.getHost();
		// no check against these hostnames.
		if ("localhost".equalsIgnoreCase(host) // $NON-NLS-1$
			|| "127.0.0.1".equals(host) // $NON-NLS-1$
			|| "::1".equals(host)) { // $NON-NLS-1$
			return;
		}
		// check subjectAlternativeNames first.
		try {
			final Collection subjectAlternativeNames = certificate.getSubjectAlternativeNames();
			if (subjectAlternativeNames == null) {
				logger.info("Server certificate does not have subjectAlternativeNames.");
			} else {
				final Iterator i = subjectAlternativeNames.iterator();
				while (i.hasNext()) {
					final List alternativeName = (List)i.next();
					final Integer type = (Integer)alternativeName.get(0);
					if (type.intValue() == 2) { // dNSName. No symbolic names!
						final String value = (String)alternativeName.get(1);
						if (value.equalsIgnoreCase(host)) {
							logger.info("One of subjectAlternativeNames matches.");
							return;
						}
					}
				}
			}
		} catch (CertificateParsingException e) {
			SSLPeerUnverifiedException sslpue = new SSLPeerUnverifiedException(e.getMessage());
			sslpue.initCause(e);
			throw sslpue;
		}
		// If the flow comes here, check commonName then.
		final X500Principal principal = certificate.getSubjectX500Principal();
		final String name = principal.getName();
		final Pattern pattern = Pattern.compile("CN\\s*=\\s*([^;,\\s]+)", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(name);
		if ( ! matcher.find() || ! matcher.group(1).equalsIgnoreCase(host)) {
			final String format = Messages.getString("Client.hostname_mismatch_format");
			final String message = MessageFormat.format(format, new Object[] { matcher.group(1), host });
			throw new SSLPeerUnverifiedException(message);
		}
		logger.info("CN matches.");
	}

	private SSLSocket createSSLSocket(String host, int port, Socket socket) throws IOException {
		boolean useBrowserSetting = getUseBrowserSetting();
		logger.debug("use browser setting = {0}", new Boolean(useBrowserSetting));
		logger.debug("ignored...");
		useBrowserSetting = false;
		if ( ! useBrowserSetting) {
			File trustStorePath = getTrustStorePath();
			System.setProperty("javax.net.ssl.trustStore", trustStorePath.getAbsolutePath());
		}
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		String fileName = conf.getClientCertificateFileName();
		System.setProperty("javax.net.ssl.keyStore", fileName);
		String pass = conf.getClientCertificatePassword();
		if (pass != null && pass.length() > 0) {
			System.setProperty("javax.net.ssl.keyStorePassword", pass);
		}
		checkPkcs12FileFormat();
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		return (SSLSocket)factory.createSocket(socket, host, port, true);
	}

	private void checkPkcs12FileFormat() throws IOException {
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
			File file = new File(conf.getClientCertificateFileName());
			String password = conf.getClientCertificatePassword();
			FileInputStream fis = new FileInputStream(file);
			ks.load(fis, password.toCharArray());
		} catch (FileNotFoundException e) {
			SSLException ssle = new SSLException(e.getMessage());
			ssle.initCause(e);
			throw ssle;
		} catch (IOException e) {
			Throwable t = e.getCause();
			if (t instanceof BadPaddingException) {
				final String message = Messages.getString("Client.client_certificate_password_maybe_invalid"); //$NON-NLS-1$
				final SSLException ssle = new SSLException(message);
				throw ssle;
			} else {
				final File file = new File(conf.getClientCertificateFileName());
				final Object[] args = { file.getName() };
				final String format = Messages.getString("Client.not_pkcs12_certificate_format"); //$NON-NLS-1$
				final String message = MessageFormat.format(format, args);
				final SSLException ssle = new SSLException(message);
				throw ssle;
			}
		} catch (Exception e) {
			IOException ioe = new IOException(e.getMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}
	private boolean getUseBrowserSetting() {
		if ( ! SystemEnvironment.isWindows()) {
			return false;
		}
		Properties deploymentProperties = new Properties();
		String home = System.getProperty("user.home");
		File deploymentDirectory = SystemEnvironment.createFilePath(new String[] {
				home, "Application Data", "Sun", "Java", "Deployment"
			});
		File deploymentPropertiesFile = new File(deploymentDirectory, "deployment.properties");
		try {
			FileInputStream fis = new FileInputStream(deploymentPropertiesFile);
			deploymentProperties.load(fis);
			return ! "false".equals(deploymentProperties.getProperty("deployment.security.browser.keystore.use"));
		} catch (FileNotFoundException e) {
			logger.info("{0} not fould", deploymentPropertiesFile);
			return false;
		} catch (IOException e) {
			logger.info("{0} could not be read", deploymentPropertiesFile);
			return false;
		}
	}

	private File getTrustStorePath() {
		String home = System.getProperty("user.home");
		if (SystemEnvironment.isWindows()) {
			File deploymentDirectory = SystemEnvironment.createFilePath(new String[] {
				home, "Application Data", "Sun", "Java", "Deployment"
			});


			File securityDirectory = new File(deploymentDirectory, "security");
			return new File(securityDirectory, "trusted.jssecacerts");
			
		} else if (SystemEnvironment.isMacOSX()){
			final String javaHome = System.getProperty("java.home"); //$NON-NLS-1$
			final File path = SystemEnvironment.createFilePath(new String[] {
				javaHome, "lib", "security" //$NON-NLS-1$ //$NON-NLS-2$
			});
			return new File(path, "cacerts");
		} else {
			File path = SystemEnvironment.createFilePath(new String[] {
				home, ".java", "deployment", "security"
			});
			return new File(path, "trusted.jssecacerts");
		}
	}

	public void run() {
		try {
			protocol.checkScreens(true);
			protocol.getScreenData();
		} catch (IOException e) {
			logger.fatal(e);
		}
	}

	void exitSystem() {
		try {
			synchronized (this) {
				if (protocol != null) {
					protocol.sendPacketClass(PacketClass.END);
				}
			}
		} catch (Exception e) {
			logger.warn(e);
		} finally {
			System.exit(0);
		}
	}

	public void finalize() {
		if (protocol != null) {
			exitSystem();
		}
	}

	public static void main(String[] args) {
		Object[] params = { CLIENT_VERSION };
		System.out.println(MessageFormat.format(Messages.getString("Client.banner_format"), params)); //$NON-NLS-1$

		Client client = Client.parseCommandLine(args);
		try {
			client.connect();
			Thread t = new Thread(client);
			t.start();
		} catch (Exception e) {
			throw new Error(e);
		}
	}
}

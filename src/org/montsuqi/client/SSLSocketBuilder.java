package org.montsuqi.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;
import javax.swing.JOptionPane;

import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;

public class SSLSocketBuilder {

	private static final int DEFAULT_WARN_CERTIFICATE_EXPIRATION_THRESHOLD = 30;

	private Configuration conf;
	private Logger logger;

	public SSLSocketBuilder(Configuration conf) {
		this.conf = conf;
		logger = Logger.getLogger(this.getClass());
	}

	public SSLSocket createSSLSocket(Socket socket) throws IOException {
		final String host = conf.getHost();
		final int port = conf.getPort();
		try {
			final KeyManager[] kms = getKeyManagers();
			final TrustManager[] tms = getTrustManagers();
			final SSLContext ctx = SSLContext.getInstance("TLS"); //$NON-NLS-1$
			ctx.init(kms, tms, null);
			final SSLSocketFactory factory = ctx.getSocketFactory();
			SSLSocket sslSocket = (SSLSocket)factory.createSocket(socket, host, port, true);
			sslSocket.startHandshake();
			final SSLSession session = sslSocket.getSession();
			validatePeerCertificates(session.getPeerCertificates());
			return sslSocket;
		} catch (GeneralSecurityException e) {
			final String message = e.getMessage();
			final SSLException ssle = new SSLException(message);
			throw ssle;
		} catch (FileNotFoundException e) {
			final String message = e.getMessage();
			final SSLException ssle = new SSLException(message);
			throw ssle;
		} catch (SocketException e) {
			if (isMissingPassphraseMessage(e.getMessage())) {
				final String message = Messages.getString("Client.client_certificate_password_maybe_invalid"); //$NON-NLS-1$
				throw new SSLException(message);
			} else {
				throw e;
			}
		} catch (SSLHandshakeException e) {
			final Throwable t = e.getCause();
			if (t != null && (t instanceof SSLHandshakeException)) {
				throw new IOException(Messages.getString("Client.broken_pipe")); //$NON-NLS-1$
			}
			throw e;
		} catch (IOException e) {
			final Throwable t = e.getCause();
			if (t != null && isBrokenPipeMessage(t.getMessage())) {
				throw new IOException(Messages.getString("Client.broken_pipe")); //$NON-NLS-1$
			}
			if (t != null && (t instanceof BadPaddingException || t.getMessage().equals("Could not perform unpadding: invalid pad byte."))) { //$NON-NLS-1$
				final String message = Messages.getString("Client.client_certificate_password_maybe_invalid"); //$NON-NLS-1$
				final SSLException ssle = new SSLException(message);
				throw ssle;
			} else {
				System.out.println(e);
				final File file = new File(conf.getClientCertificateFileName());
				final Object[] args = { file.getName() };
				final String format = Messages.getString("Client.not_pkcs12_certificate_format"); //$NON-NLS-1$
				final String message = MessageFormat.format(format, args);
				final SSLException ssle = new SSLException(message);
				throw ssle;
			}
		}
	}

	private void validatePeerCertificates(final Certificate[] certificates) throws SSLException {
		final Certificate serverCertificate = certificates[0];
		if (serverCertificate instanceof X509Certificate) {
			checkHostNameInCertificate((X509Certificate)serverCertificate);
		} else {
			logger.warn("Server Certificate is not a X.509 Certificate");
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
			final SSLPeerUnverifiedException sslpue = new SSLPeerUnverifiedException(e.getMessage());
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

	private boolean isMissingPassphraseMessage(String message) {
		return message != null && message.indexOf("Default SSL context init failed") >= 0 && //$NON-NLS-1$
			message.indexOf("/ by zero") >= 0; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean isBrokenPipeMessage(String message) {
		return message != null && message.toLowerCase().startsWith("broken pipe"); //$NON-NLS-1$
	}

	private TrustManager[] getTrustManagers() throws GeneralSecurityException, FileNotFoundException, IOException {
		boolean useBrowserSetting = getUseBrowserSetting();
		logger.debug("use browser setting = {0}", new Boolean(useBrowserSetting));
		logger.debug("ignored...");
		useBrowserSetting = false;
		if (useBrowserSetting) {
			return null;
		}
		final KeyStore ks = KeyStore.getInstance("JKS"); //$NON-NLS-1$
		final File trustStorePath = getTrustStorePath();
		final InputStream is = new FileInputStream(trustStorePath);
		ks.load(is, null);
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); //$NON-NLS-1$
		tmf.init(ks);
		return tmf.getTrustManagers();
	}

	private KeyManager[] getKeyManagers() throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
		String fileName = conf.getClientCertificateFileName();
		if (fileName == null || fileName.length() <= 0) {
			return null;
		}
		String pass = conf.getClientCertificatePassword();
		if (pass != null && pass.length() > 0) {
			KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
			InputStream is = new FileInputStream(fileName);
			ks.load(is, pass.toCharArray());
			checkPkcs12FileFormat(ks);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); //$NON-NLS-1$
			kmf.init(ks, pass.toCharArray());
			return kmf.getKeyManagers();
		} else {
			final String message = Messages.getString("Client.empty_pass"); //$NON-NLS-1$
			throw new SSLException(message);
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

	private void checkPkcs12FileFormat(KeyStore ks) throws IOException {
		try {
			final Enumeration e = ks.aliases();
			final String alias = (String)e.nextElement();
			if (e.hasMoreElements()) {
				final String message = Messages.getString("Client.more_than_one_client_certificate_in_pkcs12"); //$NON-NLS-1$
				throw new SSLException(message);
			}
			final Certificate certificate = ks.getCertificate(alias);
			if (certificate instanceof X509Certificate) {
				final X509Certificate x509certificate = (X509Certificate)certificate;
				checkCertificateExpiration(x509certificate);
				final int before = getWarnCertificateExpirationThreashold();
				warnCertificateExpirationWithin(x509certificate, before);
			}
		} catch (SSLException e) {
			throw e;
		} catch (Exception e) {
			IOException ioe = new IOException(e.getMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}

	private void checkCertificateExpiration(final X509Certificate x509certificate) throws SSLException {
		try {
			x509certificate.checkValidity();
		} catch (CertificateExpiredException ex) {
			final Object[] args = { x509certificate.getSubjectDN(), x509certificate.getNotAfter() };
			final String format = Messages.getString("Client.client_certificate_expired_format"); //$NON-NLS-1$
			final String message = MessageFormat.format(format, args);
			throw new SSLException(message);
		} catch (CertificateNotYetValidException ex) {
			final Object[] args = { x509certificate.getSubjectDN(), x509certificate.getNotBefore() };
			final String format = Messages.getString("Client.client_certificate_not_yet_valid_format"); //$NON-NLS-1$
			final String message = MessageFormat.format(format, args);
			throw new SSLException(message);
		}
	}

	private void warnCertificateExpirationWithin(final X509Certificate x509certificate, int before) {
		final Date end = x509certificate.getNotAfter();
		final Calendar cal = Calendar.getInstance();
		cal.setTime(end);
		cal.add(Calendar.DATE, -before);
		final Date oneMonthBeforeEnd = cal.getTime();
		final Date today = new Date();
		if (today.after(oneMonthBeforeEnd) && today.before(end)) {
			final Object[] args = { x509certificate.getSubjectDN(), end };
			final String format = Messages.getString("Client.warn_certificate_expiration_format");
			final String message = MessageFormat.format(format, args);
			final String title = Messages.getString("Client.warning_title");
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
		}
	}

	private int getWarnCertificateExpirationThreashold() {
		int before = DEFAULT_WARN_CERTIFICATE_EXPIRATION_THRESHOLD;
		final String warnExpirationThreshold = System.getProperty("monsia.warn.certificate.expiration.before"); //$NON-NLS-1$
		if (warnExpirationThreshold != null) {
			try {
				final int newBefore = Integer.parseInt(warnExpirationThreshold);
				if (newBefore > 0) {
					before = newBefore;
				} else {
					logger.warn("monsia.warn.certificate.expiration.before must be a positive value, ignored."); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				logger.warn("monsia.warn.certificate.expiration.before is not a number, falling back to default."); //$NON-NLS-1$
			}
		}
		return before;
	}

}

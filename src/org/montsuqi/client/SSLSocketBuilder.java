/*      PANDA -- a simple transaction monitor

 Copyright (C) 1998-1999 Ogochan.
 2000-2003 Ogochan & JMA (Japan Medical Association).
 2002-2006 OZAWA Sakuro.

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

import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.net.ssl.*;
import javax.security.auth.x500.X500Principal;
import javax.swing.JOptionPane;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SSLSocketBuilder {

    private static final int DEFAULT_WARN_CERTIFICATE_EXPIRATION_THRESHOLD = 30;
    private static final Logger logger = LogManager.getLogger(SSLSocketBuilder.class);
    private final SSLSocketFactory factory;
    private final KeyManager[] keyManagers;
    final TrustManager[] trustManagers;
    public static final HostnameVerifier CommonNameVerifier = new CommonNameVerifier();

    public static class CommonNameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            try {
                SSLSocketBuilder.validatePeerCertificates(session.getPeerCertificates(), hostname);
                return true;
            } catch (SSLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public SSLSocketFactory getFactory() {
        return factory;
    }

    public SSLSocketBuilder(String caCert, String p12, String p12Password) throws IOException {
        boolean keyManagerIsReady = false;
        try {
            keyManagers = createKeyManagers(p12, p12Password);
            keyManagerIsReady = true;
            if (caCert == null || caCert.isEmpty()) {
                trustManagers = createSystemDefaultTrustManagers();
            } else {
                trustManagers = createCAFileTrustManagers(caCert);
            }
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(keyManagers, trustManagers, null);
            factory = ctx.getSocketFactory();
        } catch (SSLException e) {
            throw e;
        } catch (FileNotFoundException e) {
            String missingFileName;
            // we cannot check keyManagers != null here, since it may not be initialized.
            if (keyManagerIsReady) {
                missingFileName = null;
            } else {
                missingFileName = new File(p12).getAbsolutePath();
            }
            final String format = Messages.getString("Client.file_not_found_format");
            Object[] args = {missingFileName};
            final String message = MessageFormat.format(format, args);
            throw new IOException(message);
        } catch (IOException e) {
            System.out.println(e);
            final Throwable t = e.getCause();
            if (isMissingPassphraseMessage(e.getMessage())) {
                final String message = Messages.getString("Client.client_certificate_password_maybe_invalid");
                final SSLException ssle = new SSLException(message);
                throw ssle;
            }
            if (t != null && (t instanceof BadPaddingException || t.getMessage().equals("Could not perform unpadding: invalid pad byte."))) {
                final String message = Messages.getString("Client.client_certificate_password_maybe_invalid");
                final SSLException ssle = new SSLException(message);
                throw ssle;
            }
            throw e;
        } catch (GeneralSecurityException e) {
            final String message = e.getMessage();
            final SSLException ssle = new SSLException(message);
            throw ssle;
        }
    }

    private static void validatePeerCertificates(final Certificate[] certificates, final String host) throws SSLException {
        final Certificate serverCertificate = certificates[0];
        if (serverCertificate instanceof X509Certificate) {
            checkHostNameInCertificate((X509Certificate) serverCertificate, host);
        } else {
            System.out.println("Server Certificate is not a X.509 Certificate");
        }
    }

    private static void checkHostNameInCertificate(final X509Certificate certificate, final String host) throws SSLPeerUnverifiedException {
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
                System.out.println("Server certificate does not have subjectAlternativeNames.");
            } else {
                final Iterator i = subjectAlternativeNames.iterator();
                while (i.hasNext()) {
                    final List alternativeName = (List) i.next();
                    final Integer type = (Integer) alternativeName.get(0);
                    if (type.intValue() == 2) { // dNSName == 2. Symbolic names not defined :/
                        final String value = (String) alternativeName.get(1);
                        if (value.equalsIgnoreCase(host)) {
                            System.out.println("One of subjectAlternativeNames matches.");
                            return;
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
            final String message = e.getMessage();
            final SSLPeerUnverifiedException sslpue = new SSLPeerUnverifiedException(message);
            sslpue.initCause(e);
            throw sslpue;
        }
        // If the flow comes here, check commonName then.
        final X500Principal principal = certificate.getSubjectX500Principal();
        final String name = principal.getName();
        final Pattern pattern = Pattern.compile("CN\\s*=\\s*([^;,\\s]+)", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(name);
        if (!matcher.find() || !matcher.group(1).equalsIgnoreCase(host)) {
            final String format = Messages.getString("Client.hostname_mismatch_format");
            final Object[] args = new Object[]{matcher.group(1), host};
            final String message = MessageFormat.format(format, args);
            throw new SSLPeerUnverifiedException(message);
        }
        System.out.println("CN matches.");
    }

    /**
     * <p>
     * Test if the given message means that passphrase is missing.</p>
     */
    private boolean isMissingPassphraseMessage(String message) {
        return message != null && message.indexOf("Default SSL context init failed") >= 0
                && message.indexOf("/ by zero") >= 0;
    }

    protected static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
        String data = new String(pem);
        if (data.contains(beginDelimiter) && data.contains(endDelimiter)) {
            String[] tokens = data.split(beginDelimiter);
            tokens = tokens[1].split(endDelimiter);
            return DatatypeConverter.parseBase64Binary(tokens[0]);
        } else {
            return pem;
        }
    }

    protected static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private byte[] fileToBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream inputStream = new FileInputStream(file);
        inputStream.read(bytes);
        inputStream.close();
        return bytes;
    }

    private TrustManager[] createCAFileTrustManagers(String caCertPath) throws GeneralSecurityException, FileNotFoundException, IOException {
        byte[] certPem = fileToBytes(new File(caCertPath));
        byte[] certBytes = parseDERFromPEM(certPem, "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
        X509Certificate cert = generateCertificateFromDER(certBytes);

        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null);
        keystore.setCertificateEntry("cert-alias", cert);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keystore);
        return trustManagerFactory.getTrustManagers();
    }

    private TrustManager[] createSystemDefaultTrustManagers() throws GeneralSecurityException, FileNotFoundException, IOException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        return trustManagerFactory.getTrustManagers();
    }

    private KeyManager[] createKeyManagers(final String fileName, final String pass) throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
        if (fileName == null || fileName.length() <= 0) {
            return null;
        }
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        final InputStream is = new FileInputStream(fileName);
        ks.load(is, pass.toCharArray());
        checkPkcs12FileFormat(ks);
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pass.toCharArray());
        return kmf.getKeyManagers();
    }

    private void checkPkcs12FileFormat(final KeyStore ks) throws IOException {
        try {
            final Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                final String alias = (String) e.nextElement();
                final Certificate certificate = ks.getCertificate(alias);
                if (certificate instanceof X509Certificate) {
                    final X509Certificate x509certificate = (X509Certificate) certificate;
                    checkCertificateExpiration(x509certificate);
                    final int before = getWarnCertificateExpirationThreashold();
                    warnCertificateExpirationWithin(x509certificate, before);
                }
            }
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            final String message = e.getMessage();
            throw new IOException(message, e);
        }
    }

    private void checkCertificateExpiration(final X509Certificate certificate) throws SSLException {
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException e) {
            final Object[] args = {certificate.getSubjectDN(), certificate.getNotAfter()};
            final String format = Messages.getString("Client.client_certificate_expired_format");
            final String message = MessageFormat.format(format, args);
            throw new SSLException(message);
        } catch (CertificateNotYetValidException e) {
            final Object[] args = {certificate.getSubjectDN(), certificate.getNotBefore()};
            final String format = Messages.getString("Client.client_certificate_not_yet_valid_format");
            final String message = MessageFormat.format(format, args);
            throw new SSLException(message);
        }
    }

    private void warnCertificateExpirationWithin(final X509Certificate certificate, final int before) {
        final Date end = certificate.getNotAfter();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DATE, -before);
        final Date oneMonthBeforeEnd = cal.getTime();
        final Date today = new Date();
        if (today.after(oneMonthBeforeEnd) && today.before(end)) {
            final Object[] args = {certificate.getSubjectDN(), end};
            final String format = Messages.getString("Client.warn_certificate_expiration_format");
            final String message = MessageFormat.format(format, args);
            final String title = Messages.getString("Client.warning_title");
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }

    private int getWarnCertificateExpirationThreashold() {
        int before = DEFAULT_WARN_CERTIFICATE_EXPIRATION_THRESHOLD;
        final String warnExpirationThreshold = System.getProperty("monsia.warn.certificate.expiration.before");
        if (warnExpirationThreshold != null) {
            try {
                final int newBefore = Integer.parseInt(warnExpirationThreshold);
                if (newBefore > 0) {
                    before = newBefore;
                } else {
                    logger.warn("monsia.warn.certificate.expiration.before must be a positive value, ignored.");
                }
            } catch (NumberFormatException e) {
                logger.warn("monsia.warn.certificate.expiration.before is not a number, falling back to default.");
            }
        }
        return before;
    }
}

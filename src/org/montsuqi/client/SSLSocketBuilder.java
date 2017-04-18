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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.CertificateDetailPanel;

public class SSLSocketBuilder {

    private static final int CERT_EXPIRATION_THRESHOLD = 30;
    private static final Logger logger = LogManager.getLogger(SSLSocketBuilder.class);
    private final SSLSocketFactory factory;
    private final KeyManager[] keyManagers;
    final TrustManager[] trustManagers;
    public static final HostnameVerifier CommonNameVerifier = new CommonNameVerifier();

    public static class CommonNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            try {
                SSLSocketBuilder.validatePeerCertificates(session.getPeerCertificates(), hostname);
                return true;
            } catch (SSLException e) {
                logger.info(e, e);
                return false;
            }
        }
    }

    public SSLSocketFactory getFactory() {
        return factory;
    }

    public SSLSocketBuilder(String caFileName, String fileName, String password) throws IOException {
        boolean keyManagerIsReady = false;
        try {
            keyManagers = createKeyManagers(fileName, password);
            keyManagerIsReady = true;
            if (caFileName.isEmpty() || caFileName.matches("^\\s*$")) {
                trustManagers = createTrustManagers();
            } else {
                trustManagers = createCAFileTrustManagers(caFileName);               
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
                missingFileName = new File(fileName).getAbsolutePath();
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

    public SSLSocket createSSLSocket(final Socket socket, final String host, final int port) throws IOException, GeneralSecurityException {
        try {
            final SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, host, port, true);
            sslSocket.startHandshake();
            final SSLSession session = sslSocket.getSession();
            validatePeerCertificates(session.getPeerCertificates(), host);
            return sslSocket;
        } catch (SSLException e) {
            if (e.getCause() instanceof RuntimeException) {
                Throwable t = e.getCause();
                t = t.getCause();
                logger.fatal(t);
                if (t instanceof GeneralSecurityException) {
                    throw (GeneralSecurityException) t;
                }
            }
            throw e;
        } catch (IOException e) {
            if (isBrokenPipeMessage(e.getMessage())) {
                final String message = Messages.getString("Client.broken_pipe");
                throw new IOException(message);
            }
            throw e;
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
                    if (type == 2) { // dNSName == 2. Symbolic names not defined :/
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
        return message != null && message.contains("Default SSL context init failed")
                && message.contains("/ by zero");
    }

    /**
     * <p>
     * Test if the given message means broken pipe.</p>
     */
    private boolean isBrokenPipeMessage(String message) {
        return message != null && message.toLowerCase().startsWith("broken pipe");
    }

    private static X509Certificate parseCertPem(String pem) throws CertificateException {
        byte[] der = DatatypeConverter.parseBase64Binary(pem);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(der));
    }

    private static String[] splitCertFile(String path) throws FileNotFoundException, IOException {

        byte[] fileContentBytes = Files.readAllBytes(Paths.get(path));
        String str = new String(fileContentBytes, StandardCharsets.UTF_8);

        Pattern pattern = Pattern.compile("-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        String[] strs = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            s = s.replace("-----BEGIN CERTIFICATE-----", "");
            strs[i] = s.replace("-----END CERTIFICATE-----", "");
        }
        return strs;
    }

    private TrustManager[] createCAFileTrustManagers(String caCertPath) throws GeneralSecurityException, FileNotFoundException, IOException {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null);

        String pemStrs[] = splitCertFile(caCertPath);
        for (String pem : pemStrs) {
            X509Certificate cert = parseCertPem(pem);
            keystore.setCertificateEntry(cert.getSubjectDN().toString(), cert);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keystore);
        final TrustManager[] tms = tmf.getTrustManagers();
        for (TrustManager tm1 : tms) {
            if (tm1 instanceof X509TrustManager) {
                final X509TrustManager delegatee = (X509TrustManager) tm1;
                final TrustManager tm = new MyTrustManager(delegatee);
                return new TrustManager[]{tm};
            }
        }
        return tms;
    }

    private TrustManager[] createTrustManagers() throws GeneralSecurityException, FileNotFoundException, IOException {

        if (SystemEnvironment.isMacOSX()) {
            System.setProperty("javax.net.ssl.trustStoreType", "KeychainStore");
            System.setProperty("javax.net.ssl.trustStoreProvider", "Apple");
            final File trustStorePath = getTrustStorePath();
            final KeyStore ks;
            ks = KeyStore.getInstance("KeychainStore", "Apple");
            try (InputStream is = new FileInputStream(trustStorePath)) {
                ks.load(is, null);
            } catch (IOException ex) { // for 10.12 Sierra
                InputStream is = new FileInputStream(trustStorePath + "-db");
                ks.load(is, null);
            }
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); //$NON-NLS-1$
            tmf.init(ks);
            final TrustManager[] tms = tmf.getTrustManagers();
            for (TrustManager tm1 : tms) {
                if (tm1 instanceof X509TrustManager) {
                    final X509TrustManager delegatee = (X509TrustManager) tm1;
                    final TrustManager tm = new MyTrustManager(delegatee);
                    return new TrustManager[]{tm};
                }
            }
            return tms;

        } else {
            final KeyStore ks = KeyStore.getInstance("JKS"); //$NON-NLS-1$
            final File trustStorePath = getTrustStorePath();
            try (InputStream is = new FileInputStream(trustStorePath)) {
                ks.load(is, null);
            }
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); //$NON-NLS-1$
            tmf.init(ks);
            final TrustManager[] tms = tmf.getTrustManagers();
            for (TrustManager tm1 : tms) {
                if (tm1 instanceof X509TrustManager) {
                    final X509TrustManager delegatee = (X509TrustManager) tm1;
                    final TrustManager tm = new MyTrustManager(delegatee);
                    return new TrustManager[]{tm};
                }
            }
            return tms;

        }
    }

    private File getTrustStorePath() {
        String home = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("windows vista")
                || osName.startsWith("windows 7")
                || osName.startsWith("windows 8")
                || osName.startsWith("windows 9")
                || osName.startsWith("windows 10")) {
            return new File(
                    SystemEnvironment.createFilePath(new String[]{home, "AppData", "LocalLow", "Sun", "Java", "Deployment", "security"}), "trusted.jssecacerts");
        } else if (SystemEnvironment.isWindows()) {
            return new File(
                    SystemEnvironment.createFilePath(new String[]{home, "Application Data", "Sun", "Java", "Deployment", "security"}), "trusted.jssecacerts");

        } else if (SystemEnvironment.isMacOSX()) {
            return new File(
                    SystemEnvironment.createFilePath(new String[]{home, "Library", "Keychains"}),
                    "login.keychain");
        } else {
            return new File(
                    SystemEnvironment.createFilePath(new String[]{home, ".java", "deployment", "security"}),
                    "trusted.jssecacerts");
        }
    }

    private KeyManager[] createKeyManagers(final String fileName, final String pass) throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
        if (fileName == null || fileName.length() <= 0) {
            return null;
        }
        if (pass != null && pass.length() > 0) {
            final KeyStore ks = KeyStore.getInstance("PKCS12");
            final InputStream is = new FileInputStream(fileName);
            ks.load(is, pass.toCharArray());
            checkPkcs12FileFormat(ks);
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, pass.toCharArray());
            final KeyManager[] kms = kmf.getKeyManagers();
            for (KeyManager km1 : kms) {
                if (km1 instanceof X509KeyManager) {
                    final X509KeyManager delegatee = (X509KeyManager) km1;
                    final KeyManager km = new MyKeyManager(delegatee);
                    return new KeyManager[]{km};
                }
            }
            return kms;
        } else {
            final String message = Messages.getString("Client.empty_pass");
            throw new SSLException(message);
        }
    }

    private void checkPkcs12FileFormat(final KeyStore ks) throws IOException {
        try {
            final Enumeration e = ks.aliases();
            final String alias = (String) e.nextElement();
            if (e.hasMoreElements()) {
                final String message = Messages.getString("Client.more_than_one_client_certificate_in_pkcs12");
                throw new SSLException(message);
            }
            final Certificate certificate = ks.getCertificate(alias);
            if (certificate instanceof X509Certificate) {
                final X509Certificate x509certificate = (X509Certificate) certificate;
                checkCertificateExpiration(x509certificate);
                final int before = getWarnCertificateExpirationThreashold();
                warnCertificateExpirationWithin(x509certificate, before);
            }
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            final String message = e.getMessage();
            final IOException ioe = new IOException(message);
            ioe.initCause(e);
            throw ioe;
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
        int before = CERT_EXPIRATION_THRESHOLD;
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

    final class MyKeyManager implements X509KeyManager {

        private final X509KeyManager delegatee;

        MyKeyManager(X509KeyManager delegatee) {
            this.delegatee = delegatee;
        }

        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return delegatee.getClientAliases(keyType, issuers);
        }

        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            return delegatee.chooseClientAlias(keyType, issuers, socket);
        }

        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return delegatee.getServerAliases(keyType, issuers);
        }

        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return delegatee.chooseServerAlias(keyType, issuers, socket);
        }

        public X509Certificate[] getCertificateChain(String alias) {
            final X509Certificate[] chain = delegatee.getCertificateChain(alias);
            X509TrustManager tm;
            try {
                tm = (X509TrustManager) trustManagers[0];
                tm.checkClientTrusted(chain, "RSA");
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
            return chain;
        }

        public PrivateKey getPrivateKey(String alias) {
            return delegatee.getPrivateKey(alias);
        }
    }

    final class MyTrustManager implements X509TrustManager {

        private final X509TrustManager delegatee;

        MyTrustManager(X509TrustManager delegatee) {
            this.delegatee = delegatee;
        }

        private boolean isSelfCertificate(X509Certificate[] chain) {
            final Principal subjectDN = chain[0].getSubjectDN();
            final Principal issuerDN = chain[0].getIssuerDN();
            return chain.length == 1 && subjectDN.equals(issuerDN);
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (isSelfCertificate(chain)) {
                return;
            }
            try {
                delegatee.checkClientTrusted(chain, authType);
            } catch (CertificateException e) {
                final String messageForDialog = Messages.getString("Client.client_certificate_verify_failed_proceed_connection_p");
                final String messageForException = Messages.getString("Client.untrusted_client_certificate");
                showAuthenticationFailure(chain, messageForDialog, messageForException);
            }
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                delegatee.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                final String messageForDialog = Messages.getString("Client.server_certificate_verify_failed_proceed_connection_p");
                final String messageForException = Messages.getString("Client.server_certificate_could_not_be_trusted");
                showAuthenticationFailure(chain, messageForDialog, messageForException);
            }
        }
        private static final int PROCEED_OPTION = 0;
        private static final int CHECK_OPTION = 1;

        private void showAuthenticationFailure(X509Certificate[] chain, String messageForDialog, String messageForException) throws CertificateException {
            Object[] options = {
                Messages.getString("Client.proceed"),
                Messages.getString("Client.check_certificates"),
                Messages.getString("Client.cancel")
            };
            CONFIRMATION_LOOP:
            while (true) {
                int n = JOptionPane.showOptionDialog(null,
                        messageForDialog,
                        Messages.getString("Client.warning_title"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]);
                switch (n) {
                    case PROCEED_OPTION:
                        break CONFIRMATION_LOOP;
                    case CHECK_OPTION:
                        final CertificateDetailPanel certificatePanel = new CertificateDetailPanel();
                        certificatePanel.setCertificateChain(chain);
                        final String title = Messages.getString("Client.checking_certificate_chain");
                        JOptionPane.showMessageDialog(null, certificatePanel, title, JOptionPane.PLAIN_MESSAGE);
                        continue CONFIRMATION_LOOP;
                    default:
                        throw new CertificateException(messageForException);
                }
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return delegatee.getAcceptedIssuers();
        }
    }
}

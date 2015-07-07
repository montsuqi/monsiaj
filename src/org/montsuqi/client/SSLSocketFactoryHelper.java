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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.*;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.x500.X500Principal;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.util.TempFile;

public class SSLSocketFactoryHelper {
    
    static {
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {

                    @Override
                    public boolean verify(String hostname,javax.net.ssl.SSLSession sslSession) {
                        try {
                            SSLSocketFactoryHelper.validatePeerCertificates(sslSession.getPeerCertificates(), hostname);
                        } catch (SSLException ex) {
                            java.util.logging.Logger.getLogger(SSLSocketFactoryHelper.class.getName()).log(Level.SEVERE, null, ex);
                            return false;
                        }
                        return true;
                    }
                }
        );
    }

    private static final Logger logger = LogManager.getLogger(SSLSocketFactoryHelper.class
    );

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
    
    public static SSLSocketFactory getFactory(String caCert, String p12File, String p12Pass) throws IOException, GeneralSecurityException {
        SSLSocketFactory factory;
        final KeyManager[] keyManagers;
        final TrustManager[] trustManagers;

        if (!p12File.isEmpty()) {
            keyManagers = createKeyManagers(p12File, p12Pass);
        } else {
            keyManagers = new KeyManager[]{};
        }

        if (caCert == null || caCert.isEmpty()) {
            trustManagers = createSystemDefaultTrustManagers();
        } else {
            trustManagers = createCAFileTrustManagers(caCert);
        }

        final SSLContext ctx;
        ctx = SSLContext.getInstance("TLS");
        ctx.init(keyManagers, trustManagers, null);
        factory = ctx.getSocketFactory();
        SSLContext.setDefault(ctx);
        return factory;
    }

    public static SSLSocketFactory getFactoryPKCS11(String caCert, String p11Lib, String p11Slot) throws IOException, GeneralSecurityException {
        SSLSocketFactory factory;
        final KeyManager[] keyManagers;
        final TrustManager[] trustManagers;

        final KeyStore.Builder builder = createPKCS11KeyStoreBuilder(p11Lib, p11Slot);
        final SSLContext ctx;
        ctx = SSLContext.getInstance("TLS");
        keyManagers = createPKCS11KeyManagers(builder);
        if (caCert == null || caCert.isEmpty()) {
            trustManagers = createPKCS11TrustManagers(builder);
        } else {
            trustManagers = createCAFileTrustManagers(caCert);
        }
        ctx.init(keyManagers, trustManagers, null);
        factory = ctx.getSocketFactory();
        SSLContext.setDefault(ctx);
        return factory;
    }

    private static KeyStore.Builder createPKCS11KeyStoreBuilder(String lib, String slot) throws IOException, GeneralSecurityException {
        if (slot.isEmpty()) {
            slot = "1";
        }
        String configStr = "name=monsiaj\nlibrary=" + lib + "\nslot=" + slot;
        File temp = TempFile.createTempFile("pkcs11", "cfg");
        temp.deleteOnExit();
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(temp))) {
            out.write(configStr.getBytes());
            out.close();
        }
        Provider p = new sun.security.pkcs11.SunPKCS11(temp.getAbsolutePath());
        Security.removeProvider("IAIK");
        Security.addProvider(p);
        KeyStore.Builder builder = KeyStore.Builder.newInstance("PKCS11", p, new KeyStore.CallbackHandlerProtection(new MyCallbackHandler()));
        return builder;
    }

    private static KeyManager[] createPKCS11KeyManagers(KeyStore.Builder builder) throws IOException, GeneralSecurityException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
        kmf.init(new KeyStoreBuilderParameters(builder));
        return kmf.getKeyManagers();
    }

    private static TrustManager[] createPKCS11TrustManagers(KeyStore.Builder builder) throws GeneralSecurityException, FileNotFoundException, IOException {
        KeyStore keystore = builder.getKeyStore();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keystore);
        return trustManagerFactory.getTrustManagers();
    }

    private static boolean getSavePIN() {
        Config conf = new Config();
        return conf.getSavePIN(conf.getCurrent());
    }

    private static String getPIN() {
        Config conf = new Config();
        return conf.getPIN(conf.getCurrent());
    }

    public static void setPIN(String pin, boolean savePin) {
        Config conf = new Config();
        int n = conf.getCurrent();
        conf.setPIN(n, pin);
        conf.setSavePIN(n, savePin);
        conf.save();
    }

    private static class MyCallbackHandler implements CallbackHandler {

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback cb : callbacks) {
                if (cb instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) cb;
                    if (getSavePIN()) {
                        pcb.setPassword(getPIN().toCharArray());
                    } else {
                        JPasswordField pf = new JPasswordField();
                        JCheckBox check = new JCheckBox(Messages.getString("SSLSocketFactoryHelper.save_pin"), false);
                        Object[] message = {"PIN", pf, check};
                        int resp = JOptionPane.showConfirmDialog(null, message, "pin:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (resp == JOptionPane.OK_OPTION) {
                            pcb.setPassword(pf.getPassword());
                            if (check.isSelected()) {
                                setPIN(new String(pf.getPassword()), true);
                            } else {
                                setPIN("", false);
                            }
                        }
                    }
                } else {
                    throw new UnsupportedCallbackException(callbacks[0]);
                }
            }
        }
    }

    private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
        String data = new String(pem);
        if (data.contains(beginDelimiter) && data.contains(endDelimiter)) {
            String[] tokens = data.split(beginDelimiter);
            tokens = tokens[1].split(endDelimiter);
            return DatatypeConverter.parseBase64Binary(tokens[0]);
        } else {
            return pem;
        }
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private static byte[] fileToBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            inputStream.read(bytes);
        }
        return bytes;
    }

    private static TrustManager[] createCAFileTrustManagers(String caCertPath) throws GeneralSecurityException, FileNotFoundException, IOException {
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

    private static TrustManager[] createSystemDefaultTrustManagers() throws GeneralSecurityException, FileNotFoundException, IOException {
        if (SystemEnvironment.isMacOSX()) {
            System.setProperty("javax.net.ssl.trustStoreType", "KeychainStore");
            System.setProperty("javax.net.ssl.trustStoreProvider", "Apple");
            final File trustStorePath = getTrustStorePath();
            final KeyStore ks;
            try (InputStream is = new FileInputStream(trustStorePath)) {
                ks = KeyStore.getInstance("KeychainStore", "Apple");
                ks.load(is, null);
            }
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); //$NON-NLS-1$
            tmf.init(ks);
            return tmf.getTrustManagers();
        } else {
            final KeyStore ks = KeyStore.getInstance("JKS"); //$NON-NLS-1$
            final File trustStorePath = getTrustStorePath();
            try (InputStream is = new FileInputStream(trustStorePath)) {
                ks.load(is, null);
            }
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"); //$NON-NLS-1$
            tmf.init(ks);
            return tmf.getTrustManagers();
        }
    }

    private static File getTrustStorePath() {
        String home = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("windows vista")
                || osName.startsWith("windows 7")
                || osName.startsWith("windows 8")
                || osName.startsWith("windows 9")
                || osName.startsWith("windows 10")) {
            return new File(SystemEnvironment.createFilePath(new String[]{home, "AppData", "LocalLow", "Sun", "Java", "Deployment", "security"}), "trusted.jssecacerts");
        } else if (SystemEnvironment.isWindows()) {
            return new File(SystemEnvironment.createFilePath(new String[]{home, "Application Data", "Sun", "Java", "Deployment", "security"}), "trusted.jssecacerts");
        } else if (SystemEnvironment.isMacOSX()) {
            return new File(SystemEnvironment.createFilePath(new String[]{home, "Library", "Keychains"}), "login.keychain");
        } else {
            return new File(SystemEnvironment.createFilePath(new String[]{home, ".java", "deployment", "security"}), "trusted.jssecacerts");
        }
    }

    private static KeyManager[] createKeyManagers(final String fileName, final String pass) throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
        if (fileName == null || fileName.length() <= 0) {
            return null;
        }
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        final InputStream is = new FileInputStream(fileName);
        ks.load(is, pass.toCharArray());
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pass.toCharArray());
        return kmf.getKeyManagers();
    }
}

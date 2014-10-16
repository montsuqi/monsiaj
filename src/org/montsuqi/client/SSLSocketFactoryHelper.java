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
import javax.net.ssl.*;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.util.TempFile;

public class SSLSocketFactoryHelper {

    private static final Logger logger = LogManager.getLogger(SSLSocketFactoryHelper.class);

    public static SSLSocketFactory getFactory(Config conf) throws IOException, GeneralSecurityException {
        int num = conf.getCurrent();
        if (!conf.getUseSSL(num)) {
            return null;
        }

        if (conf.getUsePKCS11(num)) {
            return createPKCS11Factory(conf);
        } else {
            return createFactory(conf);
        }
    }

    private static KeyStore.Builder createPKCS11KeyStoreBuilder(String lib,String slot) throws IOException, GeneralSecurityException {
        /*
         *   ---- pkcs11.cfg
         *   name=test
         *   library=C:\FULLPATH\yourpkcs11.dll
         *   slot=1
         *   ----
         *
         *   see docs.oracle.com/javase/7/docs/technotes/guides/security/p11guide.html
         */
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

    private static class MyCallbackHandler implements CallbackHandler {

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback cb : callbacks) {
                if (cb instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) cb;
                    JPasswordField pf = new JPasswordField();
                    Object[] message = {"pin", pf};
                    int resp = JOptionPane.showConfirmDialog(null, message, "pin:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resp == JOptionPane.OK_OPTION) {
                        pcb.setPassword(pf.getPassword());
                    }
                } else {
                    throw new UnsupportedCallbackException(callbacks[0]);
                }
            }
        }
    }

    private static SSLSocketFactory createPKCS11Factory(Config conf) throws IOException, GeneralSecurityException {
        SSLSocketFactory factory;
        final KeyManager[] keyManagers;
        final TrustManager[] trustManagers;
        int num = conf.getCurrent();
        String caCert = conf.getCACertificateFile(num);
        String pkcs11Lib = conf.getPKCS11Lib(num);
        String pkcs11Slot = conf.getPKCS11Slot(num);

        final KeyStore.Builder builder = createPKCS11KeyStoreBuilder(pkcs11Lib,pkcs11Slot);
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

    private static SSLSocketFactory createFactory(Config conf) throws IOException, GeneralSecurityException {
        SSLSocketFactory factory;
        final KeyManager[] keyManagers;
        final TrustManager[] trustManagers;
        int num = conf.getCurrent();

        String caCert = conf.getCACertificateFile(num);
        String p12 = conf.getClientCertificateFile(num);
        String p12Password = conf.getClientCertificatePassword(num);

        if (!p12.isEmpty()) {
            keyManagers = createKeyManagers(p12, p12Password);
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

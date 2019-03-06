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
package org.montsuqi.monsiaj.client;

import org.montsuqi.monsiaj.util.Messages;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.Provider;
import java.security.Security;
import java.security.cert.*;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import org.montsuqi.monsiaj.util.TempFile;

public class SSLSocketFactoryHelper {

    static final Logger logger = LogManager.getLogger(SSLSocketFactoryHelper.class);

    static {
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((String hostname, javax.net.ssl.SSLSession sslSession) -> {
            try {
                SSLSocketFactoryHelper.validatePeerCertificates(sslSession.getPeerCertificates(), hostname);
            } catch (SSLException ex) {
                java.util.logging.Logger.getLogger(SSLSocketFactoryHelper.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        });
    }

    private static final Logger LOGGER = LogManager.getLogger(SSLSocketFactoryHelper.class);

    private static void validatePeerCertificates(final Certificate[] certificates, final String host) throws SSLException {
        final Certificate serverCertificate = certificates[0];
        if (serverCertificate instanceof X509Certificate) {
            checkHostNameInCertificate((X509Certificate) serverCertificate, host);
        } else {
            LOGGER.info("Server Certificate is not a X.509 Certificate");
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
                LOGGER.debug("Server certificate does not have subjectAlternativeNames.");
            } else {
                final Iterator i = subjectAlternativeNames.iterator();
                while (i.hasNext()) {
                    final List alternativeName = (List) i.next();
                    final Integer type = (Integer) alternativeName.get(0);
                    if (type == 2) { // dNSName == 2. Symbolic names not defined :/
                        final String value = (String) alternativeName.get(1);
                        if (value.equalsIgnoreCase(host)) {
                            LOGGER.debug("One of subjectAlternativeNames matches.");
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
        LOGGER.debug("CN matches.");
    }

    public SSLSocketFactory getFactory(String caCert, String p12File, String p12Pass) throws IOException, GeneralSecurityException {
        SSLSocketFactory factory;
        final KeyManager[] keyManagers;
        final TrustManager[] trustManagers;

        if (!p12File.isEmpty()) {
            keyManagers = createKeyManagers(p12File, p12Pass);
        } else {
            keyManagers = new KeyManager[]{};
        }
        trustManagers = createCAFileTrustManagers(caCert);
        final SSLContext ctx;
        ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(keyManagers, trustManagers, null);
        factory = ctx.getSocketFactory();
        SSLContext.setDefault(ctx);
        return factory;
    }

    public SSLSocketFactory getFactoryPKCS11(String caCert, String p11Lib, String p11Slot) throws IOException, GeneralSecurityException {
        SSLSocketFactory factory;
        final KeyManager[] keyManagers;
        final TrustManager[] trustManagers;

        final KeyStore.Builder builder = createPKCS11KeyStoreBuilder(p11Lib, p11Slot);
        final SSLContext ctx;
        ctx = SSLContext.getInstance("TLSv1.2");
        keyManagers = createPKCS11KeyManagers(builder);
        trustManagers = createCAFileTrustManagers(caCert);
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
        Provider proto = Security.getProvider("SunPKCS11");
        Provider p = proto.configure(temp.getAbsolutePath());
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

    final class MyTrustManager implements X509TrustManager {

        private final X509TrustManager delegatee;

        MyTrustManager(X509TrustManager _delegatee) {
            delegatee = _delegatee;
        }

        private boolean isSelfCertificate(X509Certificate[] chain) {
            final Principal subjectDN = chain[0].getSubjectDN();
            final Principal issuerDN = chain[0].getIssuerDN();
            return chain.length == 1 && subjectDN.equals(issuerDN);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (isSelfCertificate(chain)) {
                return;
            }
            try {
                delegatee.checkClientTrusted(chain, authType);
            } catch (CertificateException ex) {
                logger.info(ex, ex);
                JOptionPane.showMessageDialog(null, Messages.getString("Client.client_certificate_verify_failed_proceed_connection_p"), Messages.getString("Client.certificate_error"), JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                delegatee.checkServerTrusted(chain, authType);
            } catch (CertificateException ex) {
                logger.info(ex, ex);
                JOptionPane.showMessageDialog(null, Messages.getString("Client.server_certificate_verify_failed_proceed_connection_p"), Messages.getString("Client.certificate_error"), JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegatee.getAcceptedIssuers();
        }
    }

}

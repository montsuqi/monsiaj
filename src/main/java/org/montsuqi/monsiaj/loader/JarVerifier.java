package org.montsuqi.monsiaj.loader;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JarVerifier {

    private static Logger log = LogManager.getLogger(JarVerifier.class);
    private static final String[] VALID_CERT_DN_ELEM = {"CN=ORCA Management Organization Co.\\, Ltd."};

    public static void main(String[] args) throws Exception {
        System.out.println(verify(new JarFile(args[0])));
    }

    private static boolean verifyCert(Certificate[] chain, X509Certificate[] trustCerts) {
        if (chain == null || chain.length == 0) {
            return false;
        }
        if (trustCerts == null || trustCerts.length == 0) {
            return false;
        }
        boolean invalidCN = true;
        X509Certificate leaf = (X509Certificate) chain[0];
        String leafName = leaf.getSubjectX500Principal().getName();
        for (String elem : VALID_CERT_DN_ELEM) {
            if (leafName.contains(elem)) {
                invalidCN = false;
            }
        }
        if (invalidCN) {
            log.error("invalid sign certificate. Subject: " + leafName);
            return false;
        }

        for (X509Certificate tc : trustCerts) {
            try {
                X509Certificate p = tc;
                for (int i = chain.length - 1; i >= 0; i--) {
                    X509Certificate xc = (X509Certificate) chain[i];
                    xc.verify(p.getPublicKey());
                    p = xc;
                }
                for (Certificate c : chain) {
                    X509Certificate xc = (X509Certificate) c;
                    xc.checkValidity();
                }
                return true;
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException ex) {
                log.debug(ex, ex);
            }
        }
        return false;
    }

    public static boolean verify(JarFile jar) throws Exception {
        X509Certificate[] certs = null;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
                certs = x509TrustManager.getAcceptedIssuers();
            }
        }

        boolean result = false;
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            try {
                InputStream iis = jar.getInputStream(entry);
            } catch (SecurityException se) {
                log.debug(se, se);
                return false;
            }
            if (verifyCert(entry.getCertificates(), certs)) {
                result = true;
            }
        }
        return result;
    }
}

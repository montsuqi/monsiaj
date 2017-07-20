/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.or.med.orca.monsiaj;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author mihara
 */
public class JarVerifier {

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

        for (X509Certificate tc : trustCerts) {
            X509Certificate root = (X509Certificate) chain[chain.length - 1];
            X509Certificate leaf = (X509Certificate) chain[0];

            try {
                root.verify(tc.getPublicKey());
                for (Certificate c : chain) {
                    X509Certificate xc = (X509Certificate) c;
                    xc.checkValidity();
                }
                if (!leaf.getSubjectDN().getName().contains("CN=Japan Medical Association")) {
                    return false;
                }                
                return true;
            } catch (Exception ex) {
                // do nothing
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
                return false;
            }
            if (verifyCert(entry.getCertificates(), certs)) {
                result = true;
            }
        }
        return result;
    }
}

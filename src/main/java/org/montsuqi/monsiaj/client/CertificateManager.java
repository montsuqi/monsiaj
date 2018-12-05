package org.montsuqi.monsiaj.client;

import org.montsuqi.monsiaj.util.Messages;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import javax.net.ssl.*;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * A class that manages client certificate</p>
 */
public class CertificateManager {

    static final Logger logger = LogManager.getLogger(Protocol.class);
    private final String authURI = null;
    private SSLSocketFactory sslSocketFactory;

    private String fileName;
    private String password;
    private Calendar notAfter;
    public static final int CERT_EXPIRE_CHECK_MONTHES = 2;

    public CertificateManager(String fileName, String pass) throws IOException, GeneralSecurityException {
      this.fileName = fileName;
      this.password = pass;
    }

    public boolean isExpireApproaching() throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
        if (fileName == null || fileName.length() <= 0) {
            return false;
        }
        Calendar notAfter = getNotAfter();
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTimeZone(TimeZone.getDefault());
        checkDate.add(Calendar.MONTH, CERT_EXPIRE_CHECK_MONTHES);
        return(checkDate.compareTo(notAfter) > 0);
    }

    public void setSSLSocketFactory(SSLSocketFactory f) {
      sslSocketFactory = f;
    }

    public void updateCertificate() throws IOException {
      URL url = new URL(authURI);
      URL post_url = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/api/cert", null);
      logger.info(post_url.toString());
      request(post_url.toString(), "POST");
    }

    // TODO
    private JSONObject request(String uri, String method) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        if (url.getProtocol().equals("https")) {
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
            }
        }
        return new JSONObject();
    }

    public Calendar getNotAfter() throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
        if (this.notAfter != null) {
          return this.notAfter;
        }
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        final InputStream is = new FileInputStream(fileName);
        ks.load(is, this.password.toCharArray());
        Enumeration<String> en = ks.aliases();
        String alias = en.nextElement();
        X509Certificate cert = (X509Certificate)ks.getCertificate(alias);
        Date d = cert.getNotAfter();
        Calendar result = Calendar.getInstance();
        result.setTime(d);
        result.setTimeZone(TimeZone.getDefault());
        this.notAfter = result;
        return result;
    }
}

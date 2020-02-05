package org.montsuqi.monsiaj.client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.net.ssl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * <p>
 * A class that manages client certificate</p>
 */
public class CertificateManager {

    static final Logger logger = LogManager.getLogger(Protocol.class);
    private String authURI = null;
    private SSLSocketFactory sslSocketFactory;

    private String fileName;
    private String password;
    private Calendar notAfter;
    public static final int CERT_EXPIRE_CHECK_MONTHES = 2;

    private static final Map<String, String> AUTH_HOST_MAP = new HashMap<String, String>() {
        {
            // 運用環境
            put("sms.orca.orcamo.jp", "auth.cmo.orcamo.jp");
            put("sms.glorca.orcamo.jp", "auth.glcmo.orcamo.jp");
            // ステージング環境
            put("sms-stg.orca.orcamo.jp", "auth-stg.cmo.orcamo.jp");
            put("sms-stg.glorca.orcamo.jp", "auth-stg.glcmo.orcamo.jp");
            // テスト環境
            put("sms-test.orca.orcamo.jp", "auth-test.cmo.orcamo.jp");
            // デモ環境
            put("sms.orca-ng.org", "auth.orca-ng.org");
        }
    };

    public CertificateManager(String fileName, String pass) throws IOException, GeneralSecurityException {
        this.fileName = fileName;
        this.password = pass;
    }

    public boolean isExpire() throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
        if (fileName == null || fileName.length() <= 0) {
            return false;
        }
        Calendar notAfter = getNotAfter();
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTimeZone(TimeZone.getDefault());
        return (checkDate.compareTo(notAfter) > 0);
    }

    public boolean isExpireApproaching() throws SSLException, FileNotFoundException, IOException, GeneralSecurityException {
        if (fileName == null || fileName.length() <= 0) {
            return false;
        }
        Calendar notAfter = getNotAfter();
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTimeZone(TimeZone.getDefault());
        checkDate.add(Calendar.MONTH, CERT_EXPIRE_CHECK_MONTHES);
        return (checkDate.compareTo(notAfter) > 0);
    }

    public void setSSLSocketFactory(SSLSocketFactory f) {
        sslSocketFactory = f;
    }

    public void setAuthURI(String v) {
        authURI = v;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPassword() {
        return password;
    }

    public void updateCertificate() throws IOException {
        URL url = new URL(authURI);
        String host = AUTH_HOST_MAP.get(url.getHost());
        if (host == null) {
            logger.info("does not support certificate update: " + authURI);
            return;
        }
        URL post_url = new URL(url.getProtocol(), host, url.getPort(), "/api/cert", null);
        logger.info(post_url.toString());
        ByteArrayOutputStream body = request(post_url.toString(), "POST");
        JSONObject result = new JSONObject(body.toString("UTF-8"));
        String get_url = result.getString("uri");
        String new_password = result.getString("pass");
        ByteArrayOutputStream cert = request(get_url, "GET");
        File certDir = new File(new File(new File(System.getProperty("user.home")), ".monsiaj"), "certificates");
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        File new_cert = new File(certDir, format.format(now) + ".p12");
        certDir.mkdirs();
        FileOutputStream fos = new FileOutputStream(new_cert);
        cert.writeTo(fos);
        fos.close();
        fileName = new_cert.getPath();
        password = new_password;
    }

    private ByteArrayOutputStream request(String uri, String method) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        if (url.getProtocol().equals("https")) {
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
            }
        }
        con.setInstanceFollowRedirects(false);
        con.setRequestProperty("Accept", "application/json");
        if (method.equals("GET")) {
            con.setDoOutput(false);
            con.setRequestMethod("GET");
        } else {
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
        }

        con.connect();
        int resCode = con.getResponseCode();
        switch (resCode) {
            case 200:
                break;
            case 302:
                break;
            default:
                String message = con.getResponseMessage();
                logger.info("http error: " + resCode + " " + message);
                throw new HttpResponseException(resCode);
        }

        ByteArrayOutputStream body = getHTTPBody(con);
        con.disconnect();
        return body;
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
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        Date d = cert.getNotAfter();
        Calendar result = Calendar.getInstance();
        result.setTime(d);
        result.setTimeZone(TimeZone.getDefault());
        this.notAfter = result;
        return result;
    }

    private ByteArrayOutputStream getHTTPBody(HttpURLConnection con) {
        try ( ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try ( BufferedOutputStream bos = new BufferedOutputStream(bytes)) {
                BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
                int length;
                while ((length = bis.read()) != -1) {
                    bos.write(length);
                }
            }
            return bytes;
        } catch (IOException ex) {
            return new ByteArrayOutputStream();
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.montsuqi.client.SSLSocketBuilder;

/**
 *
 * @author mihara
 */
public class HttpClient {

    public static InputStream get(String strURL,SSLSocketFactory socketFactory) throws IOException {
        URL url = new URL(strURL);
        String protocol = url.getProtocol();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(false);
        if (protocol.equals("https")) {
            if (socketFactory != null) {
            ((HttpsURLConnection) con).setSSLSocketFactory(socketFactory);
            ((HttpsURLConnection) con).setHostnameVerifier(SSLSocketBuilder.CommonNameVerifier);
            }
        } else if (protocol.equals("http")) {
            // do nothing
        } else {
            throw new IOException("bad protocol");
        }
        con.setRequestMethod("GET");
        con.connect();
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("" + con.getResponseCode());
        }
        return con.getInputStream();
    }
}

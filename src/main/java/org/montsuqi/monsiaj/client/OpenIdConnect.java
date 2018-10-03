package org.montsuqi.monsiaj.client;

import org.montsuqi.monsiaj.util.Messages;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * A class that manages OpenID Connect authentication.</p>
 */
public class OpenIdConnect {

    static final Logger logger = LogManager.getLogger(OpenIdConnect.class);
    private String sso_sp_uri;
    private String sso_user;
    private String sso_password;

    public OpenIdConnect(String sso_sp_uri, String sso_user, String sso_password) throws IOException {
        this.sso_sp_uri = sso_sp_uri;
        this.sso_user = sso_user;
        this.sso_password = sso_password;
    }

    public void connect() {
      logger.info("try OpenId connect...");
      // バックエンドサーバへのログイン要求
      doAuthenticationRequestToRP();
      // 認証サーバへのログイン要求
      doAuthenticationRequestToIP();
      // 認証サーバへのログイン
      doLoginToIP();
      // バックエンドサーバへのsession id発行要求
      doLoginToRP();
    }

    private void doAuthenticationRequestToRP() {
    }

    private void doAuthenticationRequestToIP() {
    }

    private void doLoginToIP() {
    }

    private void doLoginToRP() {
    }
}

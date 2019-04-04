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
import java.util.Map;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class LoginFailureException extends RuntimeException {
}

class HttpResponseException extends IOException {

    private int statusCode;

    public HttpResponseException(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}

/**
 * <p>
 * A class that manages OpenID Connect authentication.</p>
 */
public class OpenIdConnect {

    static final Logger logger = LogManager.getLogger(OpenIdConnect.class);
    private String sso_sp_uri;
    private JSONObject sso_sp_params;
    private String sso_user;
    private String sso_password;

    private String client_id;
    private String state;
    private String redirect_uri;
    private String nonce;
    private String authentication_request_uri;
    private String request_url;
    private String get_session_uri;
    private String session_id;
    private String ip_cookie = "";
    private String ip_domain = "";

    public OpenIdConnect(String sso_user, String sso_password, String sso_sp_uri, JSONObject sso_sp_params) throws IOException {
        this.sso_sp_uri = sso_sp_uri;
        this.sso_user = sso_user;
        this.sso_password = sso_password;
        this.sso_sp_params = sso_sp_params;
    }

    public String connect() throws IOException, LoginFailureException {
        logger.info("try OpenId connect...");
        // バックエンドサーバへのログイン要求
        doAuthenticationRequestToRP();
        // 認証サーバへのログイン要求
        doAuthenticationRequestToIP();
        // 認証サーバへのログイン
        doLoginToIP();
        return this.get_session_uri;
    }

    private void doAuthenticationRequestToRP() throws IOException {
        RequestOption option = new RequestOption();
        option.method = "POST";
        option.params = sso_sp_params;
        JSONObject res = request(sso_sp_uri, option);
        this.client_id = res.getString("client_id");
        this.state = res.getString("state");
        this.redirect_uri = res.getString("redirect_uri");
        this.nonce = res.getString("nonce");
        this.authentication_request_uri = res.getJSONObject("header").getString("Location");
    }

    private void doAuthenticationRequestToIP() throws IOException {
        JSONObject params = new JSONObject();
        params.put("response_type", "code");
        params.put("scope", "openid");
        params.put("client_id", client_id);
        params.put("state", state);
        params.put("redirect_uri", redirect_uri);
        params.put("nonce", nonce);
        RequestOption option = new RequestOption();
        option.method = "POST";
        option.params = params;
        JSONObject res = request(authentication_request_uri, option);
        String redirect_uri = res.getJSONObject("header").getString("Location");
        option = new RequestOption();
        res = request(redirect_uri, option);
        this.request_url = res.getString("request_url");
    }

    private void doLoginToIP() throws IOException {
        JSONObject params = new JSONObject();
        params.put("response_type", "code");
        params.put("scope", "openid");
        params.put("client_id", client_id);
        params.put("state", state);
        params.put("redirect_uri", redirect_uri);
        params.put("nonce", nonce);
        params.put("login_id", sso_user);
        params.put("password", sso_password);
        try {
            RequestOption option = new RequestOption();
            option.method = "POST";
            option.params = params;
            JSONObject res = request(request_url, option);
            redirect_uri = res.getJSONObject("header").getString("Location");
            this.ip_cookie = res.getJSONObject("header").getString("Set-Cookie");
            this.ip_domain = (new URL(redirect_uri)).getHost();

            option = new RequestOption();
            option.cookie = this.ip_cookie;
            res = request(redirect_uri, option);
            this.get_session_uri = res.getJSONObject("header").getString("Location");
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 403) {
                throw new LoginFailureException();
            } else {
                throw e;
            }
        }
    }

    private JSONObject request(String uri, RequestOption option) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        con.setInstanceFollowRedirects(false);
        con.setRequestProperty("Accept", "application/json");

        if (option.cookie != null) {
            con.setRequestProperty("Cookie", option.cookie);
        }

        if (option.method == "GET") {
            con.setDoOutput(false);
            con.setRequestMethod("GET");
        } else {
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            if (option.params != null) {
                osw.write(option.params.toString());
            }
            osw.close();
        }

        con.connect();
        int resCode = con.getResponseCode();
        switch (resCode) {
            case 200:
                break;
            case 302:
                break;
            default:
                String message = Protocol.getHTTPBody(con).toString("UTF-8");
                logger.info("http error: " + resCode + " " + message);
                throw new HttpResponseException(resCode);
        }

        JSONObject headerObj = new JSONObject();

        JSONObject result;
        if (con.getHeaderField("Content-Type").contains("application/json")) {
            ByteArrayOutputStream body = Protocol.getHTTPBody(con);
            result = new JSONObject(body.toString("UTF-8"));
        } else {
            result = new JSONObject();
        }
        headerObj.put("Set-Cookie", con.getHeaderField("Set-Cookie"));
        headerObj.put("Location", con.getHeaderField("Location"));
        result.put("header", headerObj);

        con.disconnect();

        return result;
    }

    private class RequestOption {

        public String method = "GET";
        public String cookie = null;
        public JSONObject params = null;
    }
}

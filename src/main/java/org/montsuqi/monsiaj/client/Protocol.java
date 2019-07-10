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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.Base64;
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
 * A class that implements high level operations over client/server
 * connection.</p>
 */
public class Protocol {

    /**
     * @return the serverType
     */
    public String getServerType() {
        return serverType;
    }

    static final Logger logger = LogManager.getLogger(Protocol.class);
    // jsonrpc
    private int rpcId;
    private String sessionId;
    private String tenantId;
    private String groupId;
    private String rpcURI;
    private String restURIRoot;
    private String pusherURI;
    private String authURI;
    private String startupMessage;
    private final String user;
    private final String password;
    private boolean usePushClient;
    private final boolean useSSO;
    private String protocolVersion;
    private String applicationVersion;
    private String serverType;

    private int totalExecTime;
    private int appExecTime;

    private SSLSocketFactory sslSocketFactory;
    static final String PANDA_CLIENT_VERSION = "2.0.1";

    private int sslType;

    public static final int TYPE_NO_SSL = 0;
    public static final int TYPE_SSL_NO_CERT = 1;
    public static final int TYPE_SSL_PKCS12 = 2;
    public static final int TYPE_SSL_PKCS11 = 3;

    private static final String OS_VERSION = System.getProperty("os.name") + "-" + System.getProperty("os.version");
    private static final String JAVA_VERSION = "Java_" + System.getProperty("java.version");
    private static final String MONSIAJ_VERSION = "monsiaj/" + Protocol.class.getPackage().getImplementationVersion();
    private static final String USER_AGENT = MONSIAJ_VERSION + " (" + OS_VERSION + "; " + JAVA_VERSION + ")";

    private String caCert;
    private String certFile;
    private String certFilePassphrase;

    private String openid_connect_rp_cookie = "";

    public Protocol(String authURI, final String user, final String pass, boolean useSSO) throws IOException, GeneralSecurityException {
        this.rpcId = 1;
        this.authURI = authURI;
        this.user = user;
        this.password = pass;
        this.usePushClient = false;
        this.sslType = TYPE_NO_SSL;
        this.totalExecTime = 0;
        this.appExecTime = 0;
        this.tenantId = null;
        this.groupId = null;
        this.startupMessage = null;
        this.useSSO = useSSO;
        this.serverType = "";
    }

    public boolean enablePushClient() {
        return usePushClient;
    }

    public String getStartupMessage() {
        return startupMessage;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getPusherURI() {
        return pusherURI;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getSslType() {
        return sslType;
    }

    public String getCaCert() {
        return caCert;
    }

    public String getCertFile() {
        return certFile;
    }

    public String getCertFilePassphrase() {
        return certFilePassphrase;
    }

    public int getTotalExecTime() {
        return totalExecTime;
    }

    public int getAppExecTime() {
        return appExecTime;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    public void makeSSLSocketFactory(final String caCert) throws IOException, GeneralSecurityException {
        if (caCert == null || caCert.isEmpty()) {
            sslSocketFactory = null;
        } else {
            SSLSocketFactoryHelper helper = new SSLSocketFactoryHelper();
            sslSocketFactory = helper.getFactory(caCert, "", "");
            sslType = TYPE_SSL_NO_CERT;
            this.caCert = caCert;
        }
    }

    public void makeSSLSocketFactoryPKCS12(final String caCert, final String certFile, final String certFilePass) throws IOException, GeneralSecurityException {
        SSLSocketFactoryHelper helper = new SSLSocketFactoryHelper();
        sslSocketFactory = helper.getFactory(caCert, certFile, certFilePass);
        this.sslType = TYPE_SSL_PKCS12;
        this.caCert = caCert;
        this.certFile = certFile;
        this.certFilePassphrase = certFilePass;
    }

    public void makeSSLSocketFactoryPKCS11(final String caCert, final String p11Lib, final String p11Slot) throws IOException, GeneralSecurityException {
        SSLSocketFactoryHelper helper = new SSLSocketFactoryHelper();
        sslSocketFactory = helper.getFactoryPKCS11(caCert, p11Lib, p11Slot);
        this.sslType = TYPE_SSL_PKCS11;
    }

    private HttpURLConnection getHttpURLConnection(String strURL) throws IOException {
        URL url = new URL(strURL);
        return getHttpURLConnection(url);
    }

    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);

        if (url.getProtocol().equals("https")) {
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
            }
        }
        if (!serverType.equals("ginbee")) {
            setAuthHeader(con);
        }
        return con;
    }

    private String makeJSONRPCRequest(String method, JSONObject params) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("jsonrpc", "2.0");
        obj.put("id", rpcId);
        obj.put("method", method);
        obj.put("params", params);
        rpcId += 1;
        return obj.toString();
    }

    private Object checkJSONRPCResponse(String jsonStr) throws JSONException {
        totalExecTime = 0;
        appExecTime = 0;

        JSONObject obj = new JSONObject(jsonStr);
        if (!obj.getString("jsonrpc").matches("2.0")) {
            throw new JSONException("invalid jsonrpc version");
        }
        int id = obj.getInt("id");
        if (id != (this.rpcId - 1)) {
            throw new JSONException("invalid jsonrpc id:" + id + " expected:" + (this.rpcId - 1));
        }
        if (obj.has("error")) {
            JSONObject objError = obj.getJSONObject("error");
            int code = objError.getInt("code");
            String message = objError.getString("message");
            throw new JSONException("jsonrpc error code:" + code + " message:" + message);
        }
        if (!obj.has("result")) {
            throw new JSONException("no result object");
        }
        Object result = obj.get("result");
        if (result instanceof JSONObject) {
            JSONObject res = (JSONObject) result;
            if (res.has("meta")) {
                JSONObject meta = (JSONObject) ((JSONObject) result).getJSONObject("meta");
                if (meta.has("total_exec_time")) {
                    totalExecTime = meta.getInt("total_exec_time");
                }
                if (meta.has("app_exec_time")) {
                    appExecTime = meta.getInt("app_exec_time");
                }
            }
        }
        return result;
    }

    public static ByteArrayOutputStream getHTTPBody(HttpURLConnection con) {
        try ( ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try ( BufferedOutputStream bos = new BufferedOutputStream(bytes)) {
                InputStream stream;
                try {
                    stream = con.getInputStream();
                } catch (IOException e) {
                    stream = con.getErrorStream();
                }
                BufferedInputStream bis = new BufferedInputStream(stream);
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

    private void showHTTPErrorMessage(int code, String message) {
        logger.info("http error: " + code + " " + message);
        JOptionPane.showMessageDialog(null, "http status code: " + code + "\n\n" + message, "http error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    private void setAuthHeader(HttpURLConnection con) {
        String userPass = user + ":" + password;
        String base64UserPass = Base64.getEncoder().encodeToString(userPass.getBytes());
        /* 401 WWW-Authenticate: なしでもAuthヘッダを設定するため(Authenticatorでは初回はAuthヘッダをつけてくれない) */
        con.setRequestProperty("Authorization", "Basic " + base64UserPass);
        logger.debug("set Auth header");
    }

    private synchronized Object jsonRPC(String url, String method, JSONObject params) throws JSONException, IOException {
        long st = System.currentTimeMillis();
        String reqStr = makeJSONRPCRequest(method, params);
        if (System.getProperty("monsia.debug.jsonrpc") != null) {
            logger.info("---- JSONRPC request");
            logger.info(reqStr);
            logger.info("----");
        }
        HttpURLConnection con = getHttpURLConnection(url);
        if (!useSSO) {
            switch (getServerType()) {
                case "ginbee":
                    if (method.equals("start_session")) {
                        setAuthHeader(con);
                    }
                    break;
                default:
                    setAuthHeader(con);
                    break;
            }
        }
        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        //          ((HttpsURLConnection) con).setFixedLengthStreamingMode(reqStr.length());
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (useSSO) {
            con.setRequestProperty("X-Support-SSO", "1");
        }

        if (!this.openid_connect_rp_cookie.isEmpty()) {
            con.setRequestProperty("Cookie", this.openid_connect_rp_cookie);
            this.openid_connect_rp_cookie = "";
        }

        try ( OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8")) {
            osw.write(reqStr);
            osw.flush();
        }

        int resCode = con.getResponseCode();
        String resMessage = con.getResponseMessage();
        String body;

        switch (resCode) {
            case 200:
                // do nothing
                break;
            case 401:
            case 403:
                body = getHTTPBody(con).toString("UTF-8");
                if (body.equalsIgnoreCase("NOT PERMITTED CERTIFICATE")) {
                    logger.info("403 not permitted certificate");
                    JOptionPane.showMessageDialog(null, Messages.getString("Protocol.certificate_error_message"), Messages.getString("Protocol.certificate_error"), JOptionPane.ERROR_MESSAGE);
                } else if (body.equalsIgnoreCase("USER NOT IN TENANTDB")) {
                    logger.info("401 user not in tenantdb");
                    JOptionPane.showMessageDialog(null, Messages.getString("Protocol.user_not_in_tenantdb_message"), Messages.getString("Protocol.auth_error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    logger.info("" + resCode + " auth error ... " + body);
                    JOptionPane.showMessageDialog(null, Messages.getString("Protocol.auth_error_message"), Messages.getString("Protocol.auth_error"), JOptionPane.ERROR_MESSAGE);
                }
                System.exit(0);
                break;
            case 503:
                body = getHTTPBody(con).toString("UTF-8");
                if (body.equalsIgnoreCase("GINBEE_MAINTENANCE")) {
                    logger.info("503 server maintenance ... exit");
                    JOptionPane.showMessageDialog(null, Messages.getString("Protocol.maintenance_error_message"), Messages.getString("Protocol.maintenance_error"), JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    showHTTPErrorMessage(resCode, resMessage);
                }
                break;
            default:
                showHTTPErrorMessage(resCode, resMessage);
                break;
        }

        Object result;
        try ( ByteArrayOutputStream bytes = getHTTPBody(con)) {
            long et = System.currentTimeMillis();
            if (System.getProperty("monsia.do_profile") != null) {
                logger.info(method + ":" + (et - st) + "ms request_bytes:" + reqStr.length() + " response_bytes:" + bytes.size());
            }
            String resStr = bytes.toString("UTF-8");
            if (System.getProperty("monsia.debug.jsonrpc") != null) {
                logger.info("---- JSONRPC response");
                logger.info(resStr);
                logger.info("----");
            }
            result = checkJSONRPCResponse(resStr);
        }
        con.disconnect();
        return result;
    }

    private String startOpenIDConnect(String sso_user, String sso_password, String sso_sp_uri, JSONObject params) throws IOException, JSONException {
        OpenIdConnect sso = new OpenIdConnect(sso_user, sso_password, authURI, params);
        return sso.connect();
    }

    public void startSession() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        params.put("meta", meta);

        if (useSSO) {
            authURI = startOpenIDConnect(user, password, authURI, params);
        } else {
            getServerInfo();
        }

        JSONObject result = (JSONObject) jsonRPC(authURI, "start_session", params);
        meta = result.getJSONObject("meta");

        this.sessionId = meta.getString("session_id");
        if (meta.has("tenant_id")) {
            this.tenantId = meta.getString("tenant_id");
        }
        if (meta.has("group_id")) {
            this.groupId = meta.getString("group_id");
        }

        this.rpcURI = result.getString("app_rpc_endpoint_uri");
        this.restURIRoot = result.getString("app_rest_api_uri_root");
        this.pusherURI = System.getProperty("monsia.pusher_uri");
        if (this.pusherURI == null) {
            if (result.has("pusher_uri")) {
                this.pusherURI = result.getString("pusher_uri");
            }
        }
        if (this.pusherURI != null && !this.pusherURI.isEmpty()) {
            this.usePushClient = true;
        }
        if (System.getProperty("monsia.disable_push_client") != null) {
            this.usePushClient = false;
        }
        if (result.has("startup_message")) {
            this.startupMessage = result.getString("startup_message");
        }

        logger.info("tenant_id:" + this.tenantId);
        logger.info("group_id:" + this.groupId);
        logger.info("session_id:" + this.sessionId);
        logger.info("rpcURI:" + this.rpcURI);
        logger.info("restURIRoot:" + this.restURIRoot);
        logger.info("usePushClient:" + this.usePushClient);
        logger.info("pusherURI:" + this.pusherURI);
    }

    public synchronized void endSession() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        JSONObject result = (JSONObject) jsonRPC(this.rpcURI, "end_session", params);
    }

    public synchronized JSONObject getWindow() throws IOException, JSONException {

        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        return (JSONObject) jsonRPC(this.rpcURI, "get_window", params);

    }

    public synchronized String getScreenDefine(String wname) throws IOException, JSONException {

        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);
        params.put("window", wname);

        JSONObject result = (JSONObject) jsonRPC(this.rpcURI, "get_screen_define", params);
        return result.getString("screen_define");

    }

    public synchronized JSONObject sendEvent(JSONObject params) throws IOException, JSONException {
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);
        return (JSONObject) jsonRPC(this.rpcURI, "send_event", params);

    }

    public synchronized JSONObject getMessage() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        return (JSONObject) jsonRPC(this.rpcURI, "get_message", params);
    }

    public void getServerInfo() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject result = (JSONObject) jsonRPC(authURI, "get_server_info", params);
        this.protocolVersion = result.getString("protocol_version");
        this.applicationVersion = result.getString("application_version");
        this.serverType = result.getString("server_type");

        logger.info("protocol_version:" + this.protocolVersion);
        logger.info("application_version:" + this.applicationVersion);
        logger.info("server_type:" + this.getServerType());
    }

    public synchronized JSONArray listDownloads() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        return (JSONArray) jsonRPC(this.rpcURI, "list_downloads", params);
    }

    public synchronized int getBLOB(String oid, OutputStream out) throws IOException {
        if (oid.equals("0")) {
            // empty object id
            out.close();
            return 404;
        }

        URL url = new URL(this.restURIRoot + "sessions/" + this.sessionId + "/blob/" + oid);
        HttpURLConnection con = getHttpURLConnection(url);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        try ( BufferedInputStream bis = new BufferedInputStream(con.getInputStream())) {
            int length;
            while ((length = bis.read()) != -1) {
                out.write(length);
            }
            out.close();
        }
        con.disconnect();

        return con.getResponseCode();
    }

    public synchronized String postBLOB(byte[] in) throws IOException {
        URL url = new URL(this.restURIRoot + "sessions/" + this.sessionId + "/blob/");
        HttpURLConnection con = getHttpURLConnection(url);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        //((HttpsURLConnection) con.setFixedLengthStreamingMode(in.length);
        con.setRequestProperty("Content-Type", "application/octet-stream");
        con.setRequestProperty("User-Agent", USER_AGENT);
        try ( OutputStream os = con.getOutputStream()) {
            os.write(in);
            os.flush();
        }
        con.disconnect();
        return con.getHeaderField("x-blob-id");
    }

    public String getSessionId() {
        return sessionId;
    }
}

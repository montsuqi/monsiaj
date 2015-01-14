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
import java.security.GeneralSecurityException;
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

    static final Logger logger = LogManager.getLogger(Protocol.class);
    // jsonrpc
    private String protocolVersion;
    private String applicationVersion;
    private String serverType;
    private int rpcId;
    private String sessionId;
    private String rpcURI;
    private String restURIRoot;
    private String authURI;

    private final SSLSocketFactory sslSocketFactory;
    static final String PANDA_CLIENT_VERSION = "2.0.0";

    public Protocol(String authURI, final String user, final String pass) throws IOException, GeneralSecurityException {
        rpcId = 1;
        sslSocketFactory = null;
        this.authURI = authURI;
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });
    }

    public Protocol(String authURI, String caCert, String p12File, String p12Pass) throws IOException, GeneralSecurityException {
        rpcId = 1;
        this.authURI = authURI;
        sslSocketFactory = SSLSocketFactoryHelper.getFactory(caCert, p12File, p12Pass);
    }

    public Protocol(String authURI, String caCert, String p11Lib, String p11Slot, String dummy) throws IOException, GeneralSecurityException {
        rpcId = 1;
        this.authURI = authURI;
        sslSocketFactory = SSLSocketFactoryHelper.getFactoryPKCS11(caCert, p11Lib, p11Slot);
    }

    private HttpURLConnection getHttpURLConnection(String strURL) throws IOException {
        URL url = new URL(strURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String protocol = url.getProtocol();
        switch (protocol) {
            case "https":
                if (strURL.equals(this.authURI)) {
                    if (sslSocketFactory != null) {
                        ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                    }
                }
                break;
            case "http":
                break;
            default:
                throw new IOException("bad protocol");
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
        return obj.get("result");
    }

    private Object jsonRPC(String url, String method, JSONObject params) throws JSONException, IOException {
        long st = System.currentTimeMillis();
        String reqStr = makeJSONRPCRequest(method, params);
        logger.debug("---- JSONRPC request");
        logger.debug(reqStr);
        logger.debug("----");
        HttpURLConnection con = getHttpURLConnection(url);

        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        //          ((HttpsURLConnection) con).setFixedLengthStreamingMode(reqStr.length());
        con.setRequestProperty("Content-Type", "application/json");
        try (OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8")) {
            osw.write(reqStr);
            osw.flush();
        }

        int responseCode = con.getResponseCode();
        if (responseCode == 401 || responseCode == 403) {
            JOptionPane.showMessageDialog(null, Messages.getString("Protocol.auth_error_message"), Messages.getString("Protocol.auth_error"), JOptionPane.ERROR_MESSAGE);
            logger.info("auth error:" + responseCode);
            SSLSocketFactoryHelper.setPIN("", false);
            System.exit(1);
        }
        Object result;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try (BufferedOutputStream bos = new BufferedOutputStream(bytes)) {
                BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
                int length;
                while ((length = bis.read()) != -1) {
                    bos.write(length);
                }
            }
            con.disconnect();

            long et = System.currentTimeMillis();
            if (System.getProperty("monsia.do_profile") != null) {
                logger.info(method + ":" + (et - st) + "ms request_bytes:" + reqStr.length() + " response_bytes:" + bytes.size());
            }

            String resStr = bytes.toString("UTF-8");
            logger.debug("---- JSONRPC response");
            logger.debug(resStr);
            logger.debug("----");      
            result = checkJSONRPCResponse(resStr);
        }
        return result;
    }

    public void getServerInfo() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject result = (JSONObject) jsonRPC(authURI, "get_server_info", params);
        this.protocolVersion = result.getString("protocol_version");
        this.applicationVersion = result.getString("application_version");
        this.serverType = result.getString("server_type");

        logger.debug("protocol_version:" + this.protocolVersion);
        logger.debug("application_version:" + this.applicationVersion);
        logger.debug("server_type:" + this.serverType);
    }

    public void startSession() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        params.put("meta", meta);

        JSONObject result = (JSONObject) jsonRPC(authURI, "start_session", params);
        meta = result.getJSONObject("meta");

        this.sessionId = meta.getString("session_id");

        if (this.serverType.startsWith("glserver")) {
            this.rpcURI = authURI;
            this.restURIRoot = authURI.replaceFirst("/rpc/", "/rest/");
        } else {
            this.rpcURI = result.getString("app_rpc_endpoint_uri");
            this.restURIRoot = result.getString("app_rest_api_uri_root");
        }

        logger.info("session_id:" + this.sessionId);
        logger.info("rpcURI:" + this.rpcURI);
        logger.info("restURIRoot:" + this.restURIRoot);
    }

    public String getServerType() {
        return serverType;
    }

    public void endSession() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        JSONObject result = (JSONObject) jsonRPC(this.rpcURI, "end_session", params);
    }

    public JSONObject getWindow() throws IOException, JSONException {

        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        return (JSONObject) jsonRPC(this.rpcURI, "get_window", params);

    }

    public String getScreenDefine(String wname) throws IOException, JSONException {

        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);
        params.put("window", wname);

        JSONObject result = (JSONObject) jsonRPC(this.rpcURI, "get_screen_define", params);
        return result.getString("screen_define");

    }

    public JSONObject sendEvent(JSONObject params) throws IOException, JSONException {
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        return (JSONObject) jsonRPC(this.rpcURI, "send_event", params);

    }

    public JSONObject getMessage() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        return (JSONObject) jsonRPC(this.rpcURI, "get_message", params);
    }

    public JSONArray listDownloads() throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("client_version", PANDA_CLIENT_VERSION);
        meta.put("session_id", this.sessionId);
        params.put("meta", meta);

        return (JSONArray) jsonRPC(this.rpcURI, "list_downloads", params);
    }

    public int getBLOB(String oid, OutputStream out) throws IOException {
        if (oid.equals("0")) {
            // empty object id
            return 404;
        }

        URL url = new URL(this.restURIRoot + "sessions/" + this.sessionId + "/blob/" + oid);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        String protocol = url.getProtocol();
        switch (protocol) {
            case "https":
                if (sslSocketFactory != null) {
                    ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                }
                break;
            case "http":
                break;
            default:
                throw new IOException("bad protocol");
        }

        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("GET");

        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        int length;
        while ((length = bis.read()) != -1) {
            out.write(length);
        }
        out.close();
        con.disconnect();

        return con.getResponseCode();
    }

    public String postBLOB(byte[] in) throws IOException {
        URL url = new URL(this.restURIRoot + "sessions/" + this.sessionId + "/blob/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        String protocol = url.getProtocol();
        switch (protocol) {
            case "https":
                if (sslSocketFactory != null) {
                    ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
                }
                break;
            case "http":
                break;
            default:
                throw new IOException("bad protocol");
        }

        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        //((HttpsURLConnection) con.setFixedLengthStreamingMode(in.length);
        con.setRequestProperty("Content-Type", "application/octet-stream");
        try (OutputStream os = con.getOutputStream()) {
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

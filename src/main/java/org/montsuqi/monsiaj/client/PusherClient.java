/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.client;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

/**
 *
 * @author mihara
 */
public class PusherClient extends Thread {

    static final Logger logger = LogManager.getLogger(PusherClient.class);
    private final URI uri;
    private final String auth;
    private final SslContextFactory sslContextFactory;
    private final Protocol protocol;
    private final Config conf;
    private String subID;
    static final long WAIT_INIT = 2 * 1000;
    static final long WAIT_MAX = 600 * 1000;

    public PusherClient(Config conf, Protocol protocol) throws URISyntaxException, KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
        this.conf = conf;
        this.protocol = protocol;
        uri = new URI(protocol.getPusherURI());
        String auth_in = protocol.getUser() + ":" + protocol.getPassword();
        this.auth = Base64.getEncoder().encodeToString(auth_in.getBytes());
        switch (protocol.getSslType()) {
            case Protocol.TYPE_SSL_NO_CERT:
                sslContextFactory = new SslContextFactory();
                sslContextFactory.setTrustStore(createCAFileTrustKeyStore(protocol.getCaCert()));
                break;
            case Protocol.TYPE_SSL_PKCS11:
                throw new java.lang.UnsupportedOperationException("PKCS11 Unsupported");
            case Protocol.TYPE_SSL_PKCS12:
                sslContextFactory = new SslContextFactory();
                KeyStore ks = KeyStore.getInstance("PKCS12");
                InputStream is = new FileInputStream(protocol.getCertFile());
                String passphrase = protocol.getCertFilePassphrase();
                ks.load(is, passphrase.toCharArray());
                sslContextFactory.setKeyStore(ks);
                sslContextFactory.setKeyStorePassword(passphrase);
                sslContextFactory.setTrustStore(createCAFileTrustKeyStore(protocol.getCaCert()));
                break;
            default:
                this.sslContextFactory = null;
                break;
        }
    }

    @Override
    public void run() {
        long waitMs = WAIT_INIT;
        while (true) {
            synchronized (this) {
                WebSocketClient client;
                if (this.sslContextFactory != null) {
                    client = new WebSocketClient(sslContextFactory);
                } else {
                    client = new WebSocketClient();
                }
                PusherWebSocket socket = new PusherWebSocket();
                try {
                    wait(waitMs);                    
                    client.setMaxIdleTimeout(Long.MAX_VALUE);
                    client.start();
                    ClientUpgradeRequest request = new ClientUpgradeRequest();
                    request.setHeader("Authorization", "Basic " + this.auth);
                    request.setHeader("X-GINBEE-TENANT-ID", "1");
                    logger.info("Connecting to : " + this.uri);
                    client.connect(socket, this.uri, request);
                    wait(5 * 1000);
                    if (socket.getConnected()) {
                        waitMs = WAIT_INIT;
                        while (!socket.getClosed()) {
                            wait(10 * 1000);
                        }
                    } else {
                        waitMs *= 2;
                        if (waitMs > WAIT_MAX) {
                            waitMs = WAIT_MAX;
                        }
                        logger.info("wait for reconnect: " + waitMs);
                    }
                } catch (Exception ex) {
                    logger.info(ex, ex);
                } finally {
                    try {
                        client.stop();
                    } catch (Exception e) {
                        logger.info(e, e);
                    }
                }
            }
        }
    }

    @WebSocket
    public class PusherWebSocket {

        private final String reqID = UUID.randomUUID().toString();
        private final CountDownLatch closeLatch = new CountDownLatch(1);
        private boolean connected = false;
        private boolean closed = false;

        @OnWebSocketConnect
        public void onConnect(Session session) {
            logger.info("---- onConnect");
            try {
                String subStr = "{"
                        + " \"command\"    : \"subscribe\","
                        + " \"req.id\"     : \"" + reqID + "\","
                        + " \"event\"      : \"*\","
                        + " \"session_id\" : \"" + protocol.getSessionId() + "\""
                        + "}";
                session.getRemote().sendString(subStr);
                connected = true;
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(PusherClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.info("---- onMessage");
            logger.info(message);
            messageHandler(message);
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.info("---- onClose");
            logger.info(statusCode);
            closed = true;
        }

        public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
            return this.closeLatch.await(duration, unit);
        }

        public boolean getConnected() {
            return this.connected;
        }

        public boolean getClosed() {
            return this.closed;
        }
    }

    private void messageHandler(String message) {
        JSONObject obj = new JSONObject(message);
        switch (obj.getString("command")) {
            case "subscribed":
                this.subID = obj.getString("sub.id");
                break;
            case "event":
                JSONObject data = obj.getJSONObject("data");
                switch (data.getString("event")) {
                    case "client_data_ready":
                        clientDataReadyHandler(data.getJSONObject("body"));
                        break;
                }
                break;
        }
    }

    private void clientDataReadyHandler(JSONObject obj) {
        DownloadThread thread = new DownloadThread(conf, protocol, obj);
        thread.start();
    }

    private class DownloadThread extends Thread {

        private final Config conf;
        private final Protocol protocol;
        private final JSONObject obj;

        public DownloadThread(Config conf, Protocol protocol, JSONObject obj) {
            this.conf = conf;
            this.protocol = protocol;
            this.obj = obj;
        }

        @Override
        public void run() {
            switch (obj.getString("type")) {
                case "report":
                    Download.printReport(conf, protocol, obj);
                    break;
                case "misc":
                    Download.downloadFile(conf, protocol, obj);
                    break;
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

    public static KeyStore createCAFileTrustKeyStore(String caCertPath) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null);

        String pemStrs[] = splitCertFile(caCertPath);
        for (String pem : pemStrs) {
            X509Certificate cert = parseCertPem(pem);
            keystore.setCertificateEntry(cert.getSubjectDN().toString(), cert);
        }
        return keystore;
    }
}

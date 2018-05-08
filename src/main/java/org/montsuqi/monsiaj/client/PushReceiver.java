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
import java.nio.ByteBuffer;
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
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

/**
 *
 * @author mihara
 */
public class PushReceiver implements Runnable {

    static final long WAIT_INIT = 2 * 1000;
    static final long WAIT_MAX = 600 * 1000;
    static final long WAIT_CONN = 10 * 1000;
    static final long IDLE_TIMEOUT = 30 * 1000;
    static final long PING_TIMEOUT = 30 * 1000;

    static final Logger logger = LogManager.getLogger(PushReceiver.class);
    private final URI uri;
    private final String auth;
    private final SslContextFactory sslContextFactory;
    private final Protocol protocol;
    private final BlockingQueue queue;
    private WebSocketClient client;
    private boolean loop;
    private boolean connWarned;

    public PushReceiver(Protocol protocol, BlockingQueue queue) throws URISyntaxException, KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
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
        this.queue = queue;
        client = null;
        loop = true;
        connWarned = false;
    }

    public void stop() {
        loop = false;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception ex) {
                logger.info(ex, ex);
            }
        }
    }

    @Override
    public void run() {
        long waitMs = WAIT_INIT;
        while (loop) {
            synchronized (this) {
                if (this.sslContextFactory != null) {
                    client = new WebSocketClient(sslContextFactory);
                } else {
                    client = new WebSocketClient();
                }
                PusherWebSocket socket = new PusherWebSocket();
                try {
                    wait(waitMs);
                    client.setMaxIdleTimeout(IDLE_TIMEOUT);
                    client.start();
                    ClientUpgradeRequest request = new ClientUpgradeRequest();
                    request.setHeader("Authorization", "Basic " + this.auth);
                    request.setHeader("X-GINBEE-TENANT-ID", "1");
                    request.setHeader("Sec-WebSocket-Version", "13");
                    logger.info("Connecting to : " + this.uri);
                    client.connect(socket, this.uri, request);
                    wait(WAIT_CONN);
                    if (socket.getConnected()) {
                        waitMs = WAIT_INIT;
                        while (!socket.getClosed()) {
                            wait(WAIT_CONN);
                            socket.sendPing();
                        }
                    } else {
                        client.stop();
                        client.destroy();
                        waitMs *= 2;
                        if (waitMs > WAIT_MAX) {
                            waitMs = WAIT_MAX;
                        }
                        logger.info("wait for reconnect: " + waitMs);
                    }
                } catch (PusherPingTimeout ex) {
                    logger.info("websocket ping timeout");
                } catch (Exception ex) {
                    logger.info(ex, ex);
                }
            }
        }
    }

    private class PusherPingTimeout extends Exception {
    }

    private class PusherErrorCommand extends Exception {
    }

    @WebSocket
    public class PusherWebSocket {

        private boolean connected = false;
        private boolean closed = false;
        private Session session = null;
        private long lastPongTime;

        public PusherWebSocket() {
            lastPongTime = System.currentTimeMillis();
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            logger.info("---- onConnect");
            session.setIdleTimeout(IDLE_TIMEOUT);
            try {
                String subStr = "{"
                        + " \"command\"    : \"subscribe\","
                        + " \"req.id\"     : \"" + UUID.randomUUID().toString() + "\","
                        + " \"event\"      : \"*\","
                        + " \"session_id\" : \"" + protocol.getSessionId() + "\""
                        + "}";
                session.getRemote().sendString(subStr);
                String gid = protocol.getGroupId();
                if (gid != null) {
                    subStr = "{"
                            + " \"command\"    : \"subscribe\","
                            + " \"req.id\"     : \"" + UUID.randomUUID().toString() + "\","
                            + " \"event\"      : \"*\","
                            + " \"group_id\" : \"" + gid + "\""
                            + "}";
                    session.getRemote().sendString(subStr);
                }
                connected = true;
                warnReconnect();
                this.session = session;
            } catch (IOException ex) {
                logger.info(ex, ex);
            }
        }

        @OnWebSocketMessage
        public void onMessage(String message) throws PusherErrorCommand {
            logger.info("---- onMessage");
            logger.info(message);
            messageHandler(message);
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.info("---- onClose");
            logger.info(statusCode);
            closed = true;
            warnDisconnect();
        }

        @OnWebSocketError
        public void onError(Session session, Throwable cause) {
            logger.info("---- onError");
            logger.info("Error " + session + " " + cause);
            closed = true;
            warnDisconnect();
        }

        @OnWebSocketFrame
        public void onFrame(Session session, Frame frame) {
            logger.debug("---- onFrame");
            logger.debug(frame);
            if (frame.getOpCode() == 0x0A) {
                lastPongTime = System.currentTimeMillis();
            }
        }

        public boolean getConnected() {
            return this.connected;
        }

        public boolean getClosed() {
            return this.closed;
        }

        public void sendPing() throws PusherPingTimeout {
            try {
                if ((System.currentTimeMillis() - lastPongTime) > PING_TIMEOUT) {
                    throw new PusherPingTimeout();
                }
                session.getRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
            } catch (IOException ex) {
                logger.info(ex, ex);
            }
        }
    }

    private void warnReconnect() {
        if (!connWarned) {
            return;
        }
        connWarned = false;
        try {
            JSONObject obj = new JSONObject();
            obj.put("event", "websocket_reconnect");
            queue.put(obj);
        } catch (InterruptedException ex) {
            logger.error(ex, ex);
        }
    }

    private void warnDisconnect() {
        if (connWarned) {
            return;
        }
        connWarned = true;
        try {
            JSONObject obj = new JSONObject();
            obj.put("event", "websocket_disconnect");
            queue.put(obj);
        } catch (InterruptedException ex) {
            logger.error(ex, ex);
        }        
    }

    private void messageHandler(String message) throws PusherErrorCommand {
        JSONObject obj = new JSONObject(message);
        switch (obj.getString("command")) {
            case "subscribed":
                logger.debug("subject_id:" + obj.getString("sub.id"));
                break;
            case "event":
                JSONObject data = obj.getJSONObject("data");
                try {
                    queue.put(data);
                } catch (InterruptedException ex) {
                    logger.error(ex, ex);
                }
                break;
            case "error":
                throw new PusherErrorCommand();
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

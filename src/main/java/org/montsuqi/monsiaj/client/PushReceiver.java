/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.client;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.HandshakeResponse;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslContextConfigurator;
import org.glassfish.tyrus.client.SslEngineConfigurator;

public class PushReceiver implements Runnable {

    static final long RECONNECT_WAIT_INIT = 1L;
    static final long RECONNECT_WAIT_MAX = 600L;
    static final long IDLE_TIMEOUT = 30 * 1000L;
    static final long PING_TIMEOUT = 30L;
    static final long PING_INTERVAL = 10L;

    static final Logger LOGGER = LogManager.getLogger(PushReceiver.class);
    private final URI uri;
    private final Protocol protocol;
    private final BlockingQueue queue;
    private boolean connWarned;
    private Session session;
    private ClientManager client;
    private ScheduledExecutorService executorService = null;
    private Instant lastPong;
    private long reconnectWait;

    public PushReceiver(Protocol protocol, BlockingQueue queue) throws URISyntaxException, KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, GeneralSecurityException {
        this.protocol = protocol;
        uri = new URI(protocol.getPusherURI());
        this.queue = queue;
        connWarned = false;
        lastPong = null;
        reconnectWait = RECONNECT_WAIT_INIT;
        client = ClientManager.createClient();
        client.setDefaultMaxSessionIdleTimeout(IDLE_TIMEOUT);
        // 自動再接続設定
        ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {
            @Override
            public boolean onDisconnect(CloseReason closeReason) {
                return true;
            }

            @Override
            public boolean onConnectFailure(Exception exception) {
                return true;
            }

            @Override
            public long getDelay() {
                reconnectWait *= 2;
                if (reconnectWait > RECONNECT_WAIT_MAX) {
                    reconnectWait = RECONNECT_WAIT_MAX;
                }
                return reconnectWait;
            }
        };
        client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);

        // PROXY設定(nonProxyHostsには未対応)
        if (!protocol.isForceNoProxy()) {
            String host = System.getProperty("proxyHost");
            String port = System.getProperty("proxyPort");
            if (host != null && port != null) {
                String proxyURI = "https://" + host + ":" + port;
                client.getProperties().put(ClientProperties.PROXY_URI, proxyURI);
            }
        }

        // SSL設定
        switch (protocol.getSslType()) {
            case Protocol.TYPE_SSL_PKCS11:
                throw new java.lang.UnsupportedOperationException("PKCS11 Unsupported");
            case Protocol.TYPE_SSL_PKCS12:
                KeyStore trustKs = SSLSocketFactoryHelper.createCAFtileTrustKeyStore(protocol.getCaCert());
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                String pass = UUID.randomUUID().toString();
                trustKs.store(os, pass.toCharArray());

                SslContextConfigurator sslContextConfigurator = new SslContextConfigurator();
                sslContextConfigurator.setTrustStoreBytes(os.toByteArray());
                sslContextConfigurator.setTrustStorePassword(pass);
                sslContextConfigurator.setTrustStoreType("JKS");
                sslContextConfigurator.setKeyStoreFile(protocol.getCertFile());
                sslContextConfigurator.setKeyStorePassword(protocol.getCertFilePassphrase());
                sslContextConfigurator.setKeyStoreType("PKCS12");
                SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(sslContextConfigurator, true, false, false);
                client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
                break;
            default:
                // do nothing
                break;
        }
    }

    public void stop() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException ex) {
                LOGGER.info(ex, ex);
            }
        }
    }

    @Override
    public void run() {
        try {
            session = client.connectToServer(new PrWebSocketClient(), uri);
        } catch (DeploymentException | IOException ex) {
            java.util.logging.Logger.getLogger(PushReceiver.class.getName()).log(Level.SEVERE, null, ex);
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
            LOGGER.error(ex, ex);
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
            LOGGER.error(ex, ex);
        }
    }

    private void messageHandler(String message) {
        JSONObject obj = new JSONObject(message);
        switch (obj.getString("command")) {
            case "subscribed":
                LOGGER.debug("subject_id:" + obj.getString("sub.id"));
                break;
            case "event":
                JSONObject data = obj.getJSONObject("data");
                try {
                    queue.put(data);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex, ex);
                }
                break;
            case "error":
                LOGGER.error(obj.toString());
                break;
        }
    }

    public static class PrClientConfigurator extends ClientEndpointConfig.Configurator {

        @Override
        public void beforeRequest(Map<String, List<String>> headers) {
            headers.put("X-GINBEE-TENANT-ID", Arrays.asList("1"));
        }

        @Override
        public void afterResponse(HandshakeResponse handshakeResponse) {
            // none
        }
    }

    @ClientEndpoint(configurator = PrClientConfigurator.class)
    public class PrWebSocketClient {

        public PrWebSocketClient() {
            super();
        }

        @OnOpen
        public void onOpen(Session session) {
            LOGGER.info("---- onOpen");
            lastPong = null;
            reconnectWait = RECONNECT_WAIT_INIT;
            try {
                String subStr = "{"
                        + " \"command\"    : \"subscribe\","
                        + " \"req.id\"     : \"" + UUID.randomUUID().toString() + "\","
                        + " \"event\"      : \"*\","
                        + " \"session_id\" : \"" + protocol.getSessionId() + "\""
                        + "}";
                session.getBasicRemote().sendText(subStr);
                String gid = protocol.getGroupId();
                if (gid != null) {
                    subStr = "{"
                            + " \"command\"    : \"subscribe\","
                            + " \"req.id\"     : \"" + UUID.randomUUID().toString() + "\","
                            + " \"event\"      : \"*\","
                            + " \"group_id\" : \"" + gid + "\""
                            + "}";
                    session.getBasicRemote().sendText(subStr);
                }
                warnReconnect();
                // pingを10秒ごとに送る
                executorService = Executors.newScheduledThreadPool(1);
                executorService.scheduleAtFixedRate(() -> {
                    if (session != null && session.isOpen()) {
                        try {
                            if (lastPong != null && Instant.now().getEpochSecond() - lastPong.getEpochSecond() > PING_TIMEOUT) {
                                LOGGER.error("Ping Error");
                                session.close();
                            } else {
                                session.getBasicRemote().sendPing(null);
                                LOGGER.debug("---- Ping");
                            }
                        } catch (IOException ex) {
                            // do nothing
                        }
                    }
                }, PING_INTERVAL, PING_INTERVAL, TimeUnit.SECONDS);
            } catch (IOException ex) {
                LOGGER.error(ex, ex);
            }
        }

        @OnMessage
        public void onMessage(String message) {
            LOGGER.info("---- onMessage\n" + message);
            messageHandler(message);
        }

        @OnMessage
        public void onMessage(PongMessage pongMsg) {
            lastPong = Instant.now();
            LOGGER.debug("---- Pong");
        }

        @OnError
        public void onError(Throwable th) {
            LOGGER.info("---- onError\n" + th.toString());
            lastPong = null;
            warnDisconnect();
            if (executorService != null) {
                executorService.shutdownNow();
                executorService = null;
            }
        }

        @OnClose
        public void onClose(Session session) {
            LOGGER.info("---- onClose");
            lastPong = null;
            warnDisconnect();
            if (executorService != null) {
                executorService.shutdownNow();
                executorService = null;
            }
        }
    }
}

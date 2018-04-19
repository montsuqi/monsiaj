/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.client;

import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.montsuqi.monsiaj.util.GtkStockIcon;
import org.montsuqi.monsiaj.util.Messages;
import org.montsuqi.monsiaj.util.PopupNotify;

/**
 *
 * @author mihara
 */
public class PushHandler implements Runnable {

    static final Logger logger = LogManager.getLogger(PushHandler.class);

    private final Config conf;
    private final Protocol protocol;
    private final BlockingQueue queue;

    public PushHandler(Config conf, Protocol protocol, BlockingQueue queue) {
        this.conf = conf;
        this.protocol = protocol;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                eventHandler((JSONObject) queue.take());
            }
        } catch (InterruptedException ex) {
            logger.error(ex, ex);
        }
    }

    public void eventHandler(JSONObject obj) {
        switch (obj.getString("event")) {
            case "client_data_ready":
                clientDataReadyHandler(obj.getJSONObject("body"));
                break;
            case "announcement":
                announcementHandler(obj.getJSONObject("body"));
                break;
            case "websocket_reconnect":
                PopupNotify.popup(Messages.getString("PushHandler.websocket_reconnect"),
                        Messages.getString("PushHandler.websocket_reconnect_message"),
                        GtkStockIcon.get("gtk-dialog-info"), 30);
                break;
            case "websocket_disconnect":
                PopupNotify.popup(Messages.getString("PushHandler.websocket_disconnect"),
                        Messages.getString("PushHandler.websocket_disconnect_message"),
                        GtkStockIcon.get("gtk-dialog-warning"), 30);
                break;
        }
    }

    public void clientDataReadyHandler(JSONObject obj) {
        switch (obj.getString("type")) {
            case "report":
                Download.printReport(conf, protocol, obj);
                break;
            case "misc":
                Download.downloadFile(conf, protocol, obj);
                break;
        }
    }

    public void announcementHandler(JSONObject obj) {
        PopupNotify.popup(Messages.getString("PushHandler.announcement"),
                obj.getString("message"),
                GtkStockIcon.get("gtk-dialog-info"), 30);
    }

}

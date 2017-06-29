/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import org.json.JSONObject;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class MemcachedConnection {

    private final String CLASS_NAME = this.getClass().getSimpleName();
    private final int VALID_TIME = 0;

    private String ip;
    private int port;

    private MemcachedClient connection;

    public MemcachedConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;

        if (!Log.SHOW_MEMCACHED_LOGS) {
            System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
            Logger.getLogger("net.spy.memcached").setLevel(Level.OFF);
        }

        this.connection = getConnection();
    }

    private MemcachedClient getConnection() {
        Log.showLogMessage(CLASS_NAME, "Connecting to memcached.", Log.INFO_LOG);
        MemcachedClient memcached;

        try {
            memcached = new MemcachedClient(new InetSocketAddress(this.ip, this.port));
            return memcached;
        } catch (IOException | SecurityException | OperationTimeoutException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to connect to memcached. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
            Utilities.shutdown(this.getClass().getSimpleName());
        }

        return null;
    }

    public void registerToMemcached(Server server) {
        try {
            Log.showLogMessage(CLASS_NAME, "Registering server to memcached.", Log.INFO_LOG);

            JSONObject registeredServers = getOnlineServers();

            if (registeredServers.isNull("servers")) {
                Log.showLogMessage(CLASS_NAME, "Initializing memcached servers registry.", Log.INFO_LOG);

                JSONObject serverJSON = new JSONObject();
                serverJSON.append("server", getJSONMemcachedMessage(server));

                this.connection.set("SD_ListServers", this.VALID_TIME, server.toString());
            } else {
                registeredServers.append("servers", getJSONMemcachedMessage(server));
                this.connection.set("SD_ListServers", this.VALID_TIME, registeredServers.toString());
            }

            Log.showLogMessage(CLASS_NAME, "Server registered.", Log.INFO_LOG);
        } catch (SecurityException | OperationTimeoutException e) {
            Log.showLogMessage(CLASS_NAME, "Failed to register to memcached. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        }
    }

    private JSONObject getJSONMemcachedMessage(Server server) {
        JSONObject serverJSON = new JSONObject();

        serverJSON.put("name", server.getServerName());
        serverJSON.put("location", server.getServerIP() + ":" + server.getServerPort());
        for (int year : server.getYears()) {
            serverJSON.append("year", year);
        }
        serverJSON.put("active", true);

        return serverJSON;
    }

    private JSONObject getOnlineServers() {
        String response;
        JSONObject serversOnline = new JSONObject();

        try {
            response = this.connection.get("SD_ListServers").toString();
            serversOnline = new JSONObject(response);
        } catch (NullPointerException e) {
            Log.showLogMessage(CLASS_NAME, "No servers online.", Log.INFO_LOG);
        }

        return serversOnline;
    }
}
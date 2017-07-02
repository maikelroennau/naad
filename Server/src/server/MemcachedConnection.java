/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import utilities.Log;
import utilities.Utilities;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import persistence.DatabaseConsultant;

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

    // =======================================================================//
    // Initialization
    // -----------------------------------------------------------------------//
    

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

    // -----------------------------------------------------------------------//
    // End initialization
    // =======================================================================//
    // =======================================================================//
    // Requisitions
    // -----------------------------------------------------------------------//
    

    public void registerServerToMemcached(Server server) {
        try {
            Log.showLogMessage(CLASS_NAME, "Registering server to memcached.", Log.INFO_LOG);

            JSONObject registeredServers = getOnlineServers();

            if (registeredServers == null | registeredServers.isNull("servers")) {
                Log.showLogMessage(CLASS_NAME, "Initializing memcached servers registry.", Log.INFO_LOG);

                JSONObject serverJSON = new JSONObject();
                serverJSON.append("servers", getJSONMemcachedMessage(server));

                this.connection.set("SD_ListServers", this.VALID_TIME, serverJSON.toString());
            } else {
                registeredServers.append("servers", getJSONMemcachedMessage(server));
                this.connection.set("SD_ListServers", this.VALID_TIME, registeredServers.toString());
            }

            Log.showLogMessage(CLASS_NAME, "Server registered.", Log.INFO_LOG);
        } catch (SecurityException | OperationTimeoutException | JSONException e) {
            Log.showLogMessage(CLASS_NAME, "Failed to register to memcached. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        }
    }

    public void registerAirportsToMemcached(DatabaseConsultant databaseConsultant) {
        try {
            Log.showLogMessage(CLASS_NAME, "Registering airports to memcached.", Log.INFO_LOG);

            JSONObject airports = databaseConsultant.getAirports();
            this.connection.set("SD_Airports", this.VALID_TIME, airports.toString());

            Log.showLogMessage(CLASS_NAME, "Airports registered.", Log.INFO_LOG);
        } catch (SecurityException | OperationTimeoutException | JSONException e) {
            Log.showLogMessage(CLASS_NAME, "Failed to register airports memcached. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
        }
    }

    public void registerCarriersToMemcached(DatabaseConsultant databaseConsultant) {
        try {
            Log.showLogMessage(CLASS_NAME, "Registering carriers to memcached.", Log.INFO_LOG);

            JSONObject carriers = databaseConsultant.getCarriers();
            this.connection.set("SD_Carriers", this.VALID_TIME, carriers.toString());

            Log.showLogMessage(CLASS_NAME, "Carriers registered.", Log.INFO_LOG);
        } catch (SecurityException | OperationTimeoutException | JSONException e) {
            Log.showLogMessage(CLASS_NAME, "Failed to register carriers memcached. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
        }
    }

    public JSONObject getAvailablerYears() {
        JSONObject years = new JSONObject();
        HashSet<Integer> serverYears = new HashSet<>();

        try {
            JSONObject onlineServers = getOnlineServers();
            JSONArray servers = onlineServers.getJSONArray("servers");
            JSONObject json;

            for (int i = 0; i < servers.length(); i++) {
                json = new JSONObject(servers.get(i).toString());

                for (String year : json.get("year").toString().replaceAll("\\[|\\]", "").split(",")) {
                    serverYears.add(Integer.parseInt(year));
                    
                }
            }
            
            for(int year : serverYears) {
                years.append("year", year);
            }

            return years;
        } catch (JSONException e) {
            Log.showLogMessage(CLASS_NAME, "Not possible to get the available years.", Log.ERROR_LOG);
        }

        return years;
    }

    // -----------------------------------------------------------------------//
    // End Requisitions
    // =======================================================================//

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
}

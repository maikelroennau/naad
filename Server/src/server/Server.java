/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class Server {

    private final String CONFIGURATION_FILE = "config.json";

    private String serverName;
    private String serverIP;
    private int serverPort;

    private String memcachedIP;
    private int memcachedport;

    private int[] years;

    // Connection settings
    private JSONObject jsonConfiguration;
    private ServerSocket connection;
    private final int VALID_TIME = 0;

    // =======================================================================//
    // Getters and Setters
    // -----------------------------------------------------------------------//
    
    public String getCONFIGURATION_FILE() {
        return CONFIGURATION_FILE;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getMemcachedIP() {
        return memcachedIP;
    }

    public int getMemcachedport() {
        return memcachedport;
    }

    public int[] getYears() {
        return years;
    }

    // -----------------------------------------------------------------------//
    // End initialization
    // =======================================================================//
    
    // ----
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
    }

    // =======================================================================//
    // Initialization
    // -----------------------------------------------------------------------//
    
    public Server() {
        initializeServerConfiguration();
        putServerOnline();
        registerToMemcached();
        run();
    }

    private void initializeServerConfiguration() {
        String configuration = readConfiguration(CONFIGURATION_FILE);
        this.jsonConfiguration = parseJSONConfigurationFile(configuration);

        if (this.jsonConfiguration == null) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to load configutations", Log.ERROR_LOG);
            Utilities.shutdown(this.getClass().getSimpleName());
        } else {
            setAttibutes(this.jsonConfiguration);
        }

        Log.showLogMessage(this.getClass().getSimpleName(), "Configurations set.", Log.INFO_LOG);
        Utilities.showConfiguration(this);
    }

    private void putServerOnline() {
        try {
            Log.showLogMessage(this.getClass().getSimpleName(), "Initializing server.", Log.INFO_LOG);
            this.connection = new ServerSocket(this.serverPort);
            Log.showLogMessage(this.getClass().getSimpleName(), "Server online.", Log.INFO_LOG);
        } catch (IOException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to put server online. Cause: " + e.getMessage(), Log.ERROR_LOG);
            Utilities.shutdown(this.getClass().getSimpleName());
        }
    }

    private void registerToMemcached() {
        try {
            if (!Log.SHOW_MEMCACHED_LOGS) {
                System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
                Logger.getLogger("net.spy.memcached").setLevel(Level.OFF);
            }

            Log.showLogMessage(this.getClass().getSimpleName(), "Registering server to memcached.", Log.INFO_LOG);

            MemcachedClient memcached = new MemcachedClient(new InetSocketAddress(this.memcachedIP, this.memcachedport));
            JSONObject registeredServers = getOnlineServers(memcached);

            if (registeredServers.isNull("servers")) {
                Log.showLogMessage(this.getClass().getSimpleName(), "Initializing memcached registry.", Log.INFO_LOG);
                JSONObject server = new JSONObject();
                server.append("server", getJSONMemcachedMessage());
                memcached.set("SD_ListServers", this.VALID_TIME, server.toString());
            } else {
                registeredServers.append("servers", getJSONMemcachedMessage());
                memcached.set("SD_ListServers", this.VALID_TIME, registeredServers.toString());
            }

            Log.showLogMessage(this.getClass().getSimpleName(), "Server registered.", Log.INFO_LOG);
        } catch (IOException | SecurityException | OperationTimeoutException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to register to memcached. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
            Utilities.shutdown(this.getClass().getSimpleName());
        }
    }

    public String readConfiguration(String filename) {
        Log.showLogMessage(this.getClass().getSimpleName(), "Reading configuration file.", Log.INFO_LOG);

        String result = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }

            result = sb.toString();
        } catch (IOException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to read configuration file.", Log.ERROR_LOG);
            Utilities.shutdown(this.getClass().getSimpleName());
        }
        return result;
    }

    public void setAttibutes(JSONObject json) {
        this.serverName = json.getString("serverName");
        this.serverPort = json.getInt("portListen");
        this.memcachedIP = json.getString("memcachedServer");
        this.memcachedport = json.getInt("memcachedPort");

        try {
            this.serverIP = InetAddress.getLocalHost().toString().split("/")[1];
        } catch (UnknownHostException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Unable to get local IP address.", Log.ERROR_LOG);
            Utilities.shutdown(this.getClass().getSimpleName());
        }

        String years;
        Object obj = json.get("yearData");
        years = obj.toString();

        ArrayList<String> readYears = new ArrayList<>();
        readYears.addAll(Arrays.asList(years.replace("[", "").replace("]", "").split(",")));

        this.years = new int[readYears.size()];
        for (int i = 0; i < readYears.size(); i++) {
            this.years[i] = Integer.parseInt(readYears.get(i));
        }

        this.jsonConfiguration = getJSONMemcachedMessage();
    }

    // -----------------------------------------------------------------------//
    // End initialization
    // =======================================================================//
    // =======================================================================//
    // Execution
    // -----------------------------------------------------------------------//
    
    private void run() {
        Log.showLogMessage(this.getClass().getSimpleName(), "Receiving requisitions.", Log.INFO_LOG);
        while (true) {
            try {

                Socket clientSocket = this.connection.accept();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        attendRequisition(clientSocket);
                    }
                }).start();
            } catch (IOException e) {
                Log.showLogMessage(this.getClass().getSimpleName(), "Failed to connect with client.", Log.ERROR_LOG);
            }
        }
    }

    public void attendRequisition(Socket clientSocket) {
        Log.showLogMessage(this.getClass().getSimpleName(), "Requisition received from " + clientSocket.getLocalAddress().toString().replace("/", "") + ".", Log.INFO_LOG);

        //PrintWriter pw;
        //BufferedReader br;
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String[] splitedCommand = br.readLine().split("\\s+");
            String command = splitedCommand[0];

            switch (command) {
                case "GETAVAILABLEYEARS":
                    pw.println(getAvailableYears().toString());
                    break;

            }
        } catch (IOException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to attend client requisition. Cause: " + e.getCause().getMessage(), Log.INFO_LOG);
        } finally {

        }
    }

    // -----------------------------------------------------------------------//
    // End exection
    // =======================================================================//
    // =======================================================================//
    // JSON
    // -----------------------------------------------------------------------//
    private JSONObject getJSONMemcachedMessage() {
        JSONObject server = new JSONObject();

        server.put("name", this.serverName);
        server.put("location", this.serverIP + ":" + this.serverPort);
        for (int year : this.years) {
            server.append("year", year);
        }
        server.put("active", true);

        return server;
    }

    public JSONObject getOnlineServers(MemcachedClient memcached) {
        String response = "";
        JSONObject serversOnline = new JSONObject();

        try {
            response = memcached.get("SD_ListServers").toString();
            serversOnline = new JSONObject(response);
        } catch (NullPointerException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "No servers online.", Log.INFO_LOG);
        }

        return serversOnline;
    }

    public JSONObject parseJSONConfigurationFile(String data) {
        try {
            JSONObject json = new JSONObject(data);

            return json;
        } catch (JSONException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to create JSON from file.", Log.ERROR_LOG);
            Utilities.shutdown(this.getClass().getSimpleName());
        }

        return null;
    }

    // -----------------------------------------------------------------------//
    // End JSON
    // =======================================================================//
    // =======================================================================//
    // Requisitions
    // -----------------------------------------------------------------------//
    public JSONObject getAvailableYears() {
        JSONObject json = new JSONObject();

        for (int year : this.years) {
            json.append("years", year);
        }

        System.out.println(json.toString());
        return json;
    }

    // -----------------------------------------------------------------------//
    // End Requisitions
    // =======================================================================//
    // =======================================================================//
    // Utilities
    // -----------------------------------------------------------------------//


    // -----------------------------------------------------------------------//
    // End utilities
    // =======================================================================//
}

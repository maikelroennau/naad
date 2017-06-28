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

    private final String configurationFile = "config.json";

    private String serverName;
    private String serverIP;
    private int serverPort;

    private String memcachedIP;
    private int memcachedport;

    private int[] years;

    private final boolean showServerLogs = true;
    private final boolean showSErverConfigurations = false;
    private final boolean showMemcachedLogs = false;

    private final String ERROR_LOG = "ERROR: ";
    private final String INFO_LOG = "INFO.: ";

    // Connection settings
    private JSONObject jsonConfiguration;
    private ServerSocket connection;
    private final int validTime = 30;

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
        String configuration = readConfiguration(configurationFile);
        this.jsonConfiguration = parseJSONConfigurationFile(configuration);

        if (this.jsonConfiguration == null) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to load configutations", ERROR_LOG);
            shutdown();
        } else {
            setAttibutes(this.jsonConfiguration);
        }

        Log.showLogMessage(this.getClass().getSimpleName(), "Configurations set.", INFO_LOG);
        showConfiguration();
    }

    private void putServerOnline() {
        try {
            Log.showLogMessage(this.getClass().getSimpleName(), "Initializing server.", INFO_LOG);
            this.connection = new ServerSocket(this.serverPort);
            Log.showLogMessage(this.getClass().getSimpleName(), "Server online.", INFO_LOG);
        } catch (IOException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to put server online. Cause: " + e.getMessage(), ERROR_LOG);
            shutdown();
        }
    }

    private void registerToMemcached() {
        try {
            if(!showMemcachedLogs) {
                System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
                Logger.getLogger("net.spy.memcached").setLevel(Level.WARNING);
            }

            Log.showLogMessage(this.getClass().getSimpleName(), "Registering server to memcached.", INFO_LOG);
            
            MemcachedClient memcached = new MemcachedClient(new InetSocketAddress(this.memcachedIP, this.memcachedport));
            JSONObject registeredServers = getOnlineServers(memcached);
            
            if(registeredServers.isNull("servers")) {
                Log.showLogMessage(this.getClass().getSimpleName(), "Initializing memcached registry.", INFO_LOG);
                JSONObject server = new JSONObject();
                server.append("server", getJSONMemcachedMessage());
                memcached.set("SD_ListServers", this.validTime, server.toString());
            } else {
                registeredServers.append("servers", getJSONMemcachedMessage());
                memcached.set("SD_ListServers", this.validTime, registeredServers.toString());
            }

            Log.showLogMessage(this.getClass().getSimpleName(), "Server registered.", INFO_LOG);
        } catch (IOException | SecurityException | OperationTimeoutException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to register to memcached. Cause: " + e.getCause().getMessage(), ERROR_LOG);
            shutdown();
        }
    }
    
    public String readConfiguration(String filename) {
        Log.showLogMessage(this.getClass().getSimpleName(), "Reading configuration file.", INFO_LOG);

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
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to read configuration file.", ERROR_LOG);
            shutdown();
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
            Log.showLogMessage(this.getClass().getSimpleName(), "Unable to get local IP address.", ERROR_LOG);
            shutdown();
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
        Log.showLogMessage(this.getClass().getSimpleName(), "Receiving requisitions.", INFO_LOG);
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
                Log.showLogMessage(this.getClass().getSimpleName(), "Failed to connect with client.", ERROR_LOG);
            }
        }
    }

    public void attendRequisition(Socket clientSocket) {
        Log.showLogMessage(this.getClass().getSimpleName(), "Requisition received from " + clientSocket.getLocalAddress().toString().replace("/", "") + ".", INFO_LOG);
        
        //PrintWriter pw;
        //BufferedReader br;
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String[] splitedCommand = br.readLine().split("\\s+");
            String command = splitedCommand[0];
            
            switch(command) {
                case "GETAVAILABLEYEARS":
                    pw.println(getAvailableYears().toString());
                    break;
                    
                
            }
        } catch (IOException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to attend client requisition. Cause: " + e.getCause().getMessage(), INFO_LOG);
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
        for(int year : this.years) {
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
            Log.showLogMessage(this.getClass().getSimpleName(), "No servers online.", INFO_LOG);
        }
        
        return serversOnline;
    }

    public JSONObject parseJSONConfigurationFile(String data) {
        try {
            JSONObject json = new JSONObject(data);

            return json;
        } catch (JSONException e) {
            Log.showLogMessage(this.getClass().getSimpleName(), "Failed to create JSON from file.", ERROR_LOG);
            shutdown();
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
        
        for(int year : this.years) {
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

    public void showConfiguration() {
        if (showSErverConfigurations) {
            System.out.println("\nConfiguration file: " + configurationFile
                    + "\nServer name.......: " + serverName
                    + "\nPort..............: " + serverPort
                    + "\nMemcached IP......: " + memcachedIP
                    + "\nMemcached port....: " + memcachedport
                    + "\nYears.............: " + Arrays.toString(years).replace("[", "").replace("]", "") + "\n");
        }
    }

    public void shutdown() {
        Log.showLogMessage(this.getClass().getSimpleName(), "Shutting system down.", INFO_LOG);
        System.exit(0);
    }

    // -----------------------------------------------------------------------//
    // End utilities
    // =======================================================================//
}

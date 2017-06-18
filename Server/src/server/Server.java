/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    private final boolean showSErverConfigurations = true;
    private final boolean showMemcachedLogs = false;

    private final String ERROR_LOG = "ERROR: ";
    private final String INFO = "INFO.: ";

    // Connection settings
    private JSONObject jsonConfiguration;
    private ServerSocket connection;
    private final int validTime = 60;

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
            showLogMessage("Failed to load configutations", ERROR_LOG);
            shutdown();
        } else {
            setAttibutes(this.jsonConfiguration);
        }

        showLogMessage("Configurations set.", INFO);
        showConfiguration();
    }

    private void putServerOnline() {
        try {
            showLogMessage("Initializing server.", INFO);
            this.connection = new ServerSocket(this.serverPort);
            showLogMessage("Server online.", INFO);
        } catch (IOException e) {
            showLogMessage("Failed to put server online. Cause: " + e.getMessage(), ERROR_LOG);
            shutdown();
        }
    }

    private void registerToMemcached() {
        try {
            if(showMemcachedLogs) {
                System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
                Logger.getLogger("net.spy.memcached").setLevel(Level.WARNING);
            }

            MemcachedClient memcached = new MemcachedClient(new InetSocketAddress(this.memcachedIP, this.memcachedport));

            if (memcached.get("servers") == null) {
                showLogMessage("Registering to memchached.", INFO);
                memcached.add("servers", this.validTime, this.jsonConfiguration.toString());
                showLogMessage("Server registered to memchached.", INFO);
            } else {
//                memcached.touch("servers", this.validTime);
                showLogMessage("Updating server information to memchached.", INFO);
                memcached.append("servers", this.jsonConfiguration.toString());
                showLogMessage("Successfully updated server information to memchached.", INFO);
            }
        } catch (IOException | SecurityException | OperationTimeoutException e) {
            showLogMessage("Failed to register to memcached. Cause: " + e.getCause().getMessage(), ERROR_LOG);
            shutdown();
        }
    }

    public String readConfiguration(String filename) {
        showLogMessage("Reading configuration file.", INFO);

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
            showLogMessage("Failed to read configuration file.", ERROR_LOG);
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
            showLogMessage("Unable to get local IP address.", ERROR_LOG);
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
        showLogMessage("Receiving requisitions.", INFO);
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
                showLogMessage("Failed to connect with client.", ERROR_LOG);
            }
        }
    }

    public void attendRequisition(Socket clientSocket) {
        showLogMessage("Requisition received from " + clientSocket.getLocalAddress().toString().replace("/", "") + ".", INFO);

    }

    // -----------------------------------------------------------------------//
    // End exection
    // =======================================================================//
    // =======================================================================//
    // JSON
    // -----------------------------------------------------------------------//
    
    private JSONObject getJSONMemcachedMessage() {

        JSONObject json = new JSONObject();

        json.put("name", this.serverName);
        json.put("location", this.serverIP + ":" + this.serverPort);
        json.put("year", Arrays.toString(this.years));
        json.put("active", true);

        return json;
    }

    public JSONObject parseJSONConfigurationFile(String data) {
        try {
            JSONObject json = new JSONObject(data);

            return json;
        } catch (JSONException e) {
            showLogMessage("Failed to create JSON from file.", ERROR_LOG);
            shutdown();
        }

        return null;
    }

    // -----------------------------------------------------------------------//
    // End JSON
    // =======================================================================//
    // =======================================================================//
    // Utilities
    // -----------------------------------------------------------------------//
    
    public void showLogMessage(String message, String type) {
        if (showServerLogs) {
            System.out.println(this.getClass().getSimpleName() + " - " + type + message);
        }
    }

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
        showLogMessage("Shutting system down.", INFO);
        System.exit(0);
    }

    // -----------------------------------------------------------------------//
    // End utilities
    // =======================================================================//
}

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
    private final boolean showSErverConfigurations = true;
    private final boolean showMemcachedLogs = false;

    private final String ERROR_LOG = "ERROR: ";
    private final String INFO = "INFO.: ";

    // Connection settings ---------------------------------------------------//
    
    private JSONObject jsonConfiguration;
    private ServerSocket connection;
    private final int validTime = 60;

    // -----------------------------------------------------------------------//
    
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
        registerAvailableInformation();
        run();
    }

    private void registerAvailableInformation() {
        registerAvailableAirports();
        registerAvailableCarries();
    }

    private void registerAvailableAirports() {
        showLogMessage("Registering available airports.", INFO);
    }

    private void registerAvailableCarries() {
        showLogMessage("Registering available carries.", INFO);
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
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
            if (!showMemcachedLogs) {
                System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
                Logger.getLogger("net.spy.memcached").setLevel(Level.OFF);
            }

            showLogMessage("Connecting to memchached.", INFO);
            MemcachedClient memcached = new MemcachedClient(new InetSocketAddress(this.memcachedIP, this.memcachedport));

            if (memcached.get("servers") == null) {
                showLogMessage("Registering to memchached.", INFO);

                JSONObject registeredServers = new JSONObject();
                registeredServers.append("servers", this.jsonConfiguration);

                memcached.set("servers", this.validTime, registeredServers.toString());
                showLogMessage("Server registered to memchached.", INFO);
            } else {
                showLogMessage("Updating server information to memchached.", INFO);

                JSONObject registeredServers = new JSONObject(memcached.get("servers").toString());
                registeredServers.append("servers", this.jsonConfiguration);

                // Check if the server is already registered, if yes, just touch  or update
                memcached.set("servers", this.validTime, registeredServers.toString());
                showLogMessage("Successfully updated server information to memchached.", INFO);
            }
        } catch (IOException | SecurityException | OperationTimeoutException e) {
            showLogMessage("Failed to register to memcached. Cause: " + e.getCause().getMessage(), ERROR_LOG);
            shutdown();
        }
    }

    private String readConfiguration(String filename) {
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

    private void setAttibutes(JSONObject json) {
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

    private void attendRequisition(Socket clientSocket) {
        showLogMessage("Requisition received from " + clientSocket.getLocalAddress().toString().replace("/", "") + ".", INFO);

        //PrintWriter pw;
        //BufferedReader br;
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String[] splitedCommand = br.readLine().split("\\s+");
            String command = splitedCommand[0];

            switch (command) {
                case "GETAVAILABLEYEARS": //GETAVAILABLEYEARS
                    pw.println(getAvailableYears().toString());
                    break;
            }

            showLogMessage("Requisition from " + clientSocket.getLocalAddress().toString().replace("/", "") + " satisfied.", INFO);
            showLogMessage("Closing connection with " + clientSocket.getLocalAddress().toString().replace("/", ""), INFO);

            pw.close();
            br.close();
        } catch (IOException e) {
            showLogMessage("Failed to attend client requisition. Cause: " + e.getCause().getMessage(), INFO);
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

    private JSONObject parseJSONConfigurationFile(String data) {
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
    // Requisitions
    // -----------------------------------------------------------------------//
    
    private JSONObject getAvailableYears() {
        JSONObject availableYears = new JSONObject();

        for (int year : this.years) {
            availableYears.append("years", year);
        }

//        try {
//            MemcachedClient memcached = new MemcachedClient(new InetSocketAddress(this.memcachedIP, this.memcachedport));
//
//            JSONObject servers = new JSONObject(memcached.get("servers").toString());
//            JSONArray array = servers.getJSONArray("servers");
//            JSONObject server;
//
//            String serverYears[];
//
//            for (int i = 0; i < array.length(); i++) {
//                server = array.getJSONObject(i);
//                serverYears = server.get("year").toString().split(",");
//
//                // Finish the parse o the years registered on memcached.
//
//                for(String year : serverYears) {
//                    year = year.replaceAll("[", "");
//                    year = year.replace("]", "");
//                    availableYears.append("years", Integer.parseInt(year));
//                }
//            }
//        } catch (IOException | SecurityException | OperationTimeoutException e) {
//            showLogMessage("Failed to register to memcached. Cause: " + e.getCause().getMessage(), ERROR_LOG);
//            shutdown();
//        }
        return availableYears;
    }

    // -----------------------------------------------------------------------//
    // End Requisitions
    // =======================================================================//
    // =======================================================================//
    // Utilities
    // -----------------------------------------------------------------------//
    
    private void showLogMessage(String message, String type) {
        if (showServerLogs) {
            System.out.println(this.getClass().getSimpleName() + " - " + type + message);
        }
    }

    private void showConfiguration() {
        if (showSErverConfigurations) {
            System.out.println("\nConfiguration file: " + configurationFile
                    + "\nServer name.......: " + serverName
                    + "\nPort..............: " + serverPort
                    + "\nMemcached IP......: " + memcachedIP
                    + "\nMemcached port....: " + memcachedport
                    + "\nYears.............: " + Arrays.toString(years).replace("[", "").replace("]", "") + "\n");
        }
    }

    private void shutdown() {
        showLogMessage("Shutting system down.", INFO);
        System.exit(0);
    }

    // -----------------------------------------------------------------------//
    // End utilities
    // =======================================================================//
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONObject;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class Server {
    
    private final String CONFIGURATION_FILE = "config.json";
    private final String CLASS_NAME = this.getClass().getSimpleName();

    private String serverName;
    private String serverIP;
    private int serverPort;
    private int[] years;

    private MemcachedConnection memcached;
    private String memcachedIP;
    private int memcachedport;

    // Connection settings
    private JSONObject jsonConfiguration;
    private ServerSocket connection;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
    }
    
    public Server() {
        readServerConfiguration();
        setServerConfiguration();
        putServerOnline();
        registerToMemcached();
        run();
    }

    // =======================================================================//
    // Getters and setters
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

    public int[] getYears() {
        return years;
    }

    public String getMemcachedIP() {
        return memcachedIP;
    }

    public int getMemcachedport() {
        return memcachedport;
    }
   
    // -----------------------------------------------------------------------//
    // End getters and Setters
    // =======================================================================//
    // =======================================================================//
    // Initialization
    // -----------------------------------------------------------------------//

    private void readServerConfiguration() {
        String configuration = Utilities.readConfiguration(CONFIGURATION_FILE);
        this.jsonConfiguration = Utilities.parseJSONConfigurationFile(configuration);

        if (this.jsonConfiguration == null) {
            Log.showLogMessage(CLASS_NAME, "Failed to create configutation object.", Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        }
    }

    private void setServerConfiguration() {
        Log.showLogMessage(CLASS_NAME, "Setting server configuration.", Log.INFO_LOG);

        this.serverName = this.jsonConfiguration.getString("serverName");
        this.serverPort = this.jsonConfiguration.getInt("portListen");
        this.memcachedIP = this.jsonConfiguration.getString("memcachedServer");
        this.memcachedport = this.jsonConfiguration.getInt("memcachedPort");

        try {
            this.serverIP = InetAddress.getLocalHost().toString().split("/")[1];
        } catch (UnknownHostException e) {
            Log.showLogMessage(CLASS_NAME, "Unable to get local IP address.", Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        }

        String years;
        Object obj = this.jsonConfiguration.get("yearData");
        years = obj.toString();

        ArrayList<String> readYears = new ArrayList<>();
        readYears.addAll(Arrays.asList(years.replace("[", "").replace("]", "").split(",")));

        this.years = new int[readYears.size()];
        for (int i = 0; i < readYears.size(); i++) {
            this.years[i] = Integer.parseInt(readYears.get(i));
        }

        Log.showLogMessage(CLASS_NAME, "Server configurations set.", Log.INFO_LOG);
        Utilities.showConfiguration(this);
    }

    private void putServerOnline() {
        try {
            Log.showLogMessage(CLASS_NAME, "Initializing server online.", Log.INFO_LOG);
            this.connection = new ServerSocket(this.serverPort);
            Log.showLogMessage(CLASS_NAME, "Server online.", Log.INFO_LOG);
        } catch (IOException e) {
            Log.showLogMessage(CLASS_NAME, "Failed to put server online. Cause: " + e.getMessage(), Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        }
    }

    private void registerToMemcached() {
        this.memcached = new MemcachedConnection(this.memcachedIP, this.memcachedport);
        this.memcached.registerToMemcached(this);
    }

    // -----------------------------------------------------------------------//
    // End initialization
    // =======================================================================//
    // =======================================================================//
    // Execution
    // -----------------------------------------------------------------------//
    
    private void run() {
        Log.showLogMessage(CLASS_NAME, "Receiving requisitions.", Log.INFO_LOG);
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
                Log.showLogMessage(CLASS_NAME, "Failed to connect with client.", Log.ERROR_LOG);
            }
        }
    }

    public void attendRequisition(Socket clientSocket) {
        Log.showLogMessage(CLASS_NAME, "Requisition received from " + clientSocket.getLocalAddress().toString().replace("/", "") + ".", Log.INFO_LOG);

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
            Log.showLogMessage(CLASS_NAME, "Failed to attend client requisition. Cause: " + e.getCause().getMessage(), Log.INFO_LOG);
        } finally {

        }
    }

    // -----------------------------------------------------------------------//
    // End exection
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
}

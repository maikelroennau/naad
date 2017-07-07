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
import persistence.DatabaseConsultant;
import utilities.Log;
import utilities.UnknownCommandException;
import utilities.Utilities;

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

    private MemcachedConnection memcached = null;
    private String memcachedIP;
    private int memcachedport;

    // Connection settings
    private JSONObject configuration;
    private ServerSocket connection;

    // Database
    private DatabaseConsultant databaseConsultant;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
    }

    public Server() {
        Log.showLogMessage(CLASS_NAME, "Starting sever startup.", Log.INFO_LOG);

        readServerConfiguration();
        setServerConfiguration();
        getDatabaseConsultantConnection();

        putServerOnline();

        registerServerToMemcached();
        registerAirportsToMemcached();
        registerCarriersToMemcached();

        Log.showLogMessage(CLASS_NAME, "Startup finished with success.", Log.INFO_LOG);
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
        this.configuration = Utilities.parseJSONConfigurationFile(configuration);

        if (this.configuration == null) {
            Log.showLogMessage(CLASS_NAME, "Failed to create server configutation object.", Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        }
    }

    private void setServerConfiguration() {
        Log.showLogMessage(CLASS_NAME, "Setting server configuration.", Log.INFO_LOG);

        this.serverName = this.configuration.getString("serverName");
        this.serverPort = this.configuration.getInt("portListen");
        this.memcachedIP = this.configuration.getString("memcachedServer");
        this.memcachedport = this.configuration.getInt("memcachedPort");

        try {
            this.serverIP = InetAddress.getLocalHost().toString().split("/")[1];
        } catch (UnknownHostException e) {
            Log.showLogMessage(CLASS_NAME, "Unable to get local IP address.", Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        }

        String years;
        Object obj = this.configuration.get("yearData");
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

    private void getDatabaseConsultantConnection() {
        Log.showLogMessage(CLASS_NAME, "Establishing connection with the database.", Log.INFO_LOG);
        this.databaseConsultant = new DatabaseConsultant(this.configuration);
        Log.showLogMessage(CLASS_NAME, "Database connection established with success.", Log.INFO_LOG);
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

    private void registerServerToMemcached() {
        if (this.memcached == null) {
            this.memcached = new MemcachedConnection(this.memcachedIP, this.memcachedport);
        }

        this.memcached.registerServerToMemcached(this);
    }

    private void registerAirportsToMemcached() {
        if (this.memcached == null) {
            this.memcached = new MemcachedConnection(this.memcachedIP, this.memcachedport);
        }

        this.memcached.registerAirportsToMemcached(databaseConsultant);
    }

    private void registerCarriersToMemcached() {
        if (this.memcached == null) {
            this.memcached = new MemcachedConnection(this.memcachedIP, this.memcachedport);
        }

        this.memcached.registerCarriersToMemcached(databaseConsultant);
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
        String clientIP = clientSocket.getLocalAddress().toString().replace("/", "") + ".";
        Log.showLogMessage(CLASS_NAME, "Requisition received from " + clientIP, Log.INFO_LOG);

        PrintWriter pw;
        BufferedReader br;

        try {
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            try {
                String[] command = br.readLine().split("\\s+");

                switch (command[0]) {
                    case "GETAVAILABLEYEARS":
                        Log.showLogMessage(CLASS_NAME, "Requisition: " + command[0], Log.INFO_LOG);
                        pw.println(getAvailableYears().toString());
                        break;

                    case "GETAIRPORTS":
                        Log.showLogMessage(CLASS_NAME, "Requisition: " + command[0], Log.INFO_LOG);
                        pw.println(getAvailableAirports().toString());
                        break;

                    case "GETCARRIERS":
                        Log.showLogMessage(CLASS_NAME, "Requisition: " + command[0], Log.INFO_LOG);
                        pw.println(getAvailableCarriers().toString());
                        break;

                    case "GETDELAYDATA":
                        if (command.length <= 1) {
                            throw new UnknownCommandException("GETDELAYDATA needs at least the year parameter.");
                        }

                        Log.showLogMessage(CLASS_NAME, "Requisition: " + command[0], Log.INFO_LOG);
                        pw.println(getDelayData(command));
                        break;

                    default:
                        throw new UnknownCommandException(getUnknownCommandMessage());
                }

                Log.showLogMessage(CLASS_NAME, "Requisition satisfied.", Log.INFO_LOG);
            } catch (IOException e) {
                Log.showLogMessage(CLASS_NAME, "Failed to process client requisition. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
            } catch (UnknownCommandException e) {
                Log.showLogMessage(CLASS_NAME, "Failed to process client requisition. Cause: " + e.getMessage(), Log.ERROR_LOG);
                pw.println(e.getMessage());
            } finally {
                Log.showLogMessage(CLASS_NAME, "Closing connection with client " + clientIP, Log.INFO_LOG);
                br.close();
                pw.close();
            }
        } catch (IOException e) {
            Log.showLogMessage(CLASS_NAME, "Failed to attend client requisition. Cause: " + e.getCause().getMessage(), Log.ERROR_LOG);
        }
    }

    private boolean attendFromMemcached(String[] command, PrintWriter pw) {
        JSONObject delay = this.memcached.getDelayDataStored(command);

        if (delay.toString().equals("{}")) {
            return false;
        } else {
            pw.println(delay);
        }

        return true;
    }

    // -----------------------------------------------------------------------//
    // End exection
    // =======================================================================//
    // =======================================================================//
    // Requisitions
    // -----------------------------------------------------------------------//
    
    public JSONObject getAvailableYears() {
        return memcached.getAvailablerYears();
    }

    public JSONObject getAvailableAirports() {
        return this.databaseConsultant.getAirports();
    }

    public JSONObject getAvailableCarriers() {
        return this.databaseConsultant.getCarriers();
    }

    public JSONObject getDelayData(String searchParameters[]) {
        return this.databaseConsultant.getDelayData(searchParameters);
    }

    public String getUnknownCommandMessage() {
        return "Unknown command.";
    }

    // -----------------------------------------------------------------------//
    // End Requisitions
    // =======================================================================//
}

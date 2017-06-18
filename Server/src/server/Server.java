/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class Server {

    private String configurationFile = "config.json";

    private String serverName;
    private int port;
    private String memcachedIP;
    private int memcachedport;
    private int[] years;

    private final String ERROR_LOG = "ERROR: ";
    private final String INFO = "INFO.: ";

    // ----
    private ServerSocket connection;

    // ----
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
    }

    public Server() {
        initializeServerConfiguration();
        putServerOnline();
        run();
    }

    public void putServerOnline() {
        try {
            showLogMessage("Initializing server.", INFO);
            this.connection = new ServerSocket(this.port);
            showLogMessage("Server online.", INFO);
        } catch (IOException e) {
            showLogMessage("Failed to initialize server online.", ERROR_LOG);
        }
    }

    public void run() {
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

    public void initializeServerConfiguration() {
        String configuration = readConfiguration(configurationFile);
        JSONObject json = parseJSONConfigurationFile(configuration);

        if (json == null) {
            showLogMessage("Failed to load configutations", ERROR_LOG);
            shutdown();
        } else {
            setAttibutes(json);
        }
        
        showLogMessage("Configurations set.", INFO);
        showConfiguration();
    }

    public void shutdown() {
        showLogMessage("Shutting system down.", INFO);
        System.exit(0);
    }

    public void showLogMessage(String message, String type) {
        System.out.println(this.getClass().getSimpleName() + " - " + type + message);
    }

    public void setAttibutes(JSONObject json) {
        this.serverName = json.getString("serverName");
        this.port = json.getInt("portListen");
        this.memcachedIP = json.getString("memcachedServer");
        this.memcachedport = json.getInt("memcachedPort");

        String years;
        Object obj = json.get("yearData");
        years = obj.toString();

        ArrayList<String> readYears = new ArrayList<>();

        for (String year : years.replace("[", "").replace("]", "").split(",")) {
            readYears.add(year);
        }

        this.years = new int[readYears.size()];
        for (int i = 0; i < readYears.size(); i++) {
            this.years[i] = Integer.parseInt(readYears.get(i));
        }
    }

    public String readConfiguration(String filename) {
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
        }
        return result;
    }

    public JSONObject parseJSONConfigurationFile(String data) {
        try {
            JSONObject json = new JSONObject(data);

            return json;
        } catch (JSONException e) {
            showLogMessage("Failed to create JSON from file.", ERROR_LOG);
        }

        return null;
    }

    public void showConfiguration() {
        System.out.println(
                "\nConfiguration file: " + configurationFile
                + "\nServer name.......: " + serverName
                + "\nPort..............: " + port
                + "\nMemcached IP......: " + memcachedIP
                + "\nMemcached port....: " + memcachedport
                + "\nYears.............: " + Arrays.toString(years).replace("[", "").replace("]", "") + "\n");
    }
}

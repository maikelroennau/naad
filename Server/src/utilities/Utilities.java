/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import server.Server;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class Utilities {

    private static final String CLASS_NAME = "Utilities";

    public static String readConfiguration(String filename) {
        Log.showLogMessage(CLASS_NAME, "Reading configuration file.", Log.INFO_LOG);

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
            Log.showLogMessage(CLASS_NAME, "Failed to read configuration file.", Log.ERROR_LOG);
            shutdown(CLASS_NAME);
        }

        return result;
    }

    public static void showConfiguration(Server server) {
        if (Log.SHOW_SERVER_CONFIGURATIONS) {
            System.out.println("\nConfiguration file: " + server.getCONFIGURATION_FILE()
                    + "\nServer name.......: " + server.getServerName()
                    + "\nPort..............: " + server.getServerPort()
                    + "\nMemcached IP......: " + server.getMemcachedIP()
                    + "\nMemcached port....: " + server.getMemcachedport()
                    + "\nYears.............: " + Arrays.toString(server.getYears()).replace("[", "").replace("]", "") + "\n");
        }
    }

    public static JSONObject parseJSONConfigurationFile(String data) {
        try {
            JSONObject json = new JSONObject(data);

            return json;
        } catch (JSONException e) {
            Log.showLogMessage(CLASS_NAME, "Failed to create JSON from file.", Log.ERROR_LOG);
            shutdown(CLASS_NAME);
        }

        return null;
    }
    
    public static String getCurrentTimeAndDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void shutdown(String className) {
        Log.showLogMessage(className, "Shutting system down.", Log.INFO_LOG);
        System.exit(0);
    }
}

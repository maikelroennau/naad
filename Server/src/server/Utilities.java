/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.Arrays;

/**
 *
 * @author 110453310
 */
public class Utilities {

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

    public static void shutdown(String className) {
        Log.showLogMessage(className, "Shutting system down.", Log.INFO_LOG);
        System.exit(0);
    }
}

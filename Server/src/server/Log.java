/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author maikel
 */
public class Log {
    
    public static final boolean showServerLogs = true;
    public static final boolean showServerConfigurations = false;
    public static final boolean showMemcachedLogs = false;
    
    public static final String ERROR_LOG = "ERROR: ";
    public static final String INFO_LOG = "INFO.: ";
    
    public static void showLogMessage(String originClass, String message, String type) {
        if(showServerLogs) {
            System.out.println(originClass + " - " + type + message);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class Log {

    public static final boolean SHOW_SERVER_LOGS = true;
    public static final boolean SHOW_SERVER_CONFIGURATIONS = false;
    public static final boolean SHOW_MEMCACHED_LOGS = false;

    public static final String ERROR_LOG = "ERROR: ";
    public static final String INFO_LOG = "INFO.: ";

    public static void showLogMessage(String className, String message, String type) {
        if (SHOW_SERVER_LOGS) {
            System.out.println(className + " - " + type + message);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import java.sql.Connection;
import java.sql.SQLException;
import org.json.JSONObject;
import utilities.Log;
import utilities.Utilities;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class DatabaseQueryer {

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private String databaseName;
    private String databaseIP;
    private int databasePort;
    private String user;
    private String password;
    private String url;

    // =======================================================================//
    // Initialization
    // -----------------------------------------------------------------------//
    
    public DatabaseQueryer(JSONObject configuration) {
        setDatabaseConfiguration(configuration);
        testConnection();
    }

    private void setDatabaseConfiguration(JSONObject configuration) {
        Log.showLogMessage(CLASS_NAME, "Setting database configuration.", Log.INFO_LOG);

        this.databaseName = configuration.getString("databaseName");
        this.databaseIP = configuration.getString("databaseIP");
        this.databasePort = configuration.getInt("databasePort");
        this.user = configuration.getString("user");
        this.password = configuration.getString("password");

        Log.showLogMessage(CLASS_NAME, "Database configuration set.", Log.INFO_LOG);
    }

    private void testConnection() {
        Log.showLogMessage(CLASS_NAME, "Testing database connection.", Log.INFO_LOG);

        Connection connection;

        try {
            this.url = "jdbc:mysql://" + this.databaseIP + ":" + this.databasePort + "/" + this.databaseName + "?autoReconnect=true&useSSL=false";

            connection = DatabaseConnection.getConnection(this.url, this.user, this.password);
        } catch (SQLException e) {
            Log.showLogMessage(CLASS_NAME, "Not possible to connect with the database. Cause: " + e.getMessage(), Log.ERROR_LOG);
            Utilities.shutdown(CLASS_NAME);
        } finally {
            connection = null;
        }

        Log.showLogMessage(CLASS_NAME, "Database connection working.", Log.INFO_LOG);
    }

    // -----------------------------------------------------------------------//
    // End initialization
    // =======================================================================//
    // =======================================================================//
    // Queries
    // -----------------------------------------------------------------------//
    
    public JSONObject getDelays() {
        Connection connection;

        try {
            connection = DatabaseConnection.getConnection(url, user, password);

            return new JSONObject();
        } catch (SQLException e) {
            Log.showLogMessage(CLASS_NAME, "Not possible to connect with the database. Cause: " + e.getMessage(), Log.ERROR_LOG);
        } finally {
            connection = null;
        }

        return null;
    }

    // -----------------------------------------------------------------------//
    // End queries
    // =======================================================================//
}

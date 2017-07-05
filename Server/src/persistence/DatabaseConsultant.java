/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;
import utilities.Log;
import utilities.Utilities;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class DatabaseConsultant {

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private String databaseName;
    private String databaseIP;
    private int databasePort;
    private String user;
    private String password;
    private String url;

    private final String ARRIVAL_ON_TIME_FLIGHTS = "arrivalOnTimeFlights";
    private final String ARRIVAL_DELAYED_FLIGHTS = "arrivalDelayedFlights";
    private final String ARRIVAL_DELAYED_AVERAGE_TIME = "arrivalDelayedAverageTime";

    private final String DEPARTURE_ON_TIME_FLIGHTS = "departureOnTimeFlights";
    private final String DEPARTURE_DELAYED_FLIGHTS = "departureDelayedFlights";
    private final String DEPARTURE_DELAYED_AVERAGE_TIME = "departureDelayedAverageTime";

    public DatabaseConsultant(JSONObject configuration) {
        setDatabaseConfiguration(configuration);
        testConnection();
    }

    // =======================================================================//
    // Initialization
    // -----------------------------------------------------------------------//
    
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
    
    public JSONObject getAirports() {
        try {
            Connection connection = DatabaseConnection.getConnection(url, user, password);

            try {
                JSONObject airports = new JSONObject();
                JSONObject airport;

                String query = "SELECT * FROM airports;";
                PreparedStatement statement = connection.prepareStatement(query);

                ResultSet queryResult = statement.executeQuery(query);

                while (queryResult.next()) {
                    airport = new JSONObject();

                    airport.put("iata", queryResult.getString("iata"));
                    airport.put("name", queryResult.getString("name"));
                    airport.put("city", queryResult.getString("city"));
                    airport.put("lat", queryResult.getString("lat"));
                    airport.put("lng", queryResult.getString("lng"));

                    airports.append("airports", airport);
                }

                return airports;
            } catch (SQLException e) {
                Log.showLogMessage(CLASS_NAME, "Not possible to execute query. Cause: " + e.getMessage(), Log.ERROR_LOG);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            Log.showLogMessage(CLASS_NAME, "Not possible to connect with the database. Cause: " + e.getMessage(), Log.ERROR_LOG);
        }

        return null;
    }

    public JSONObject getCarriers() {
        try {
            Connection connection = DatabaseConnection.getConnection(url, user, password);

            try {
                JSONObject carriers = new JSONObject();
                JSONObject carrier;

                String query = "SELECT * FROM carriers;";
                PreparedStatement statement = connection.prepareStatement(query);

                ResultSet queryResult = statement.executeQuery(query);

                while (queryResult.next()) {
                    carrier = new JSONObject();

                    carrier.put("code", queryResult.getString("code"));
                    carrier.put("name", queryResult.getString("name"));

                    carriers.append("airports", carrier);
                }

                return carriers;

            } catch (SQLException e) {
                Log.showLogMessage(CLASS_NAME, "Not possible to execute query. Cause: " + e.getMessage(), Log.ERROR_LOG);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            Log.showLogMessage(CLASS_NAME, "Not possible to connect with the database. Cause: " + e.getMessage(), Log.ERROR_LOG);
        }

        return null;
    }

    public JSONObject getDelayData(String searchParameters[]) {
        try {
            Connection connection = DatabaseConnection.getConnection(url, user, password);

            try {
                JSONObject delays = new JSONObject();

                String query = "SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_ar) WHERE year = " + searchParameters[1] + " AND arrival_delay <= 0";
                delays.put(ARRIVAL_ON_TIME_FLIGHTS, getArrivalOnTimeFlights(connection, query));

                query = "SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_ar) WHERE year = " + searchParameters[1] + " AND arrival_delay > 0";
                delays.put(ARRIVAL_DELAYED_FLIGHTS, getArrivalDelayedFlights(connection, query));

                return delays;
            } catch (SQLException e) {
                Log.showLogMessage(CLASS_NAME, "Not possible to execute query. Cause: " + e.getMessage(), Log.ERROR_LOG);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            Log.showLogMessage(CLASS_NAME, "Not possible to connect with the database. Cause: " + e.getMessage(), Log.ERROR_LOG);
        }

        return null;
    }

    private int getArrivalOnTimeFlights(Connection connection, String query) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet queryResult = statement.executeQuery(query);

        queryResult.next();
        int result = queryResult.getInt("COUNT(arrival_delay)");

        return result;
    }

    private int getArrivalDelayedFlights(Connection connection, String query) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet queryResult = statement.executeQuery(query);

        queryResult.next();
        int result = queryResult.getInt("COUNT(arrival_delay)");

        return result;
    }

    // -----------------------------------------------------------------------//
    // End queries
    // =======================================================================//
}

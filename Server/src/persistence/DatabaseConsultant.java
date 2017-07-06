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

    private boolean ARRIVAL = true;

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
    // Startup queries
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

    // -----------------------------------------------------------------------//
    // End startup queries
    // =======================================================================//
    // =======================================================================//
    // Requisition query
    // -----------------------------------------------------------------------//
    
    public JSONObject getDelayData(String searchParamenters[]) {
        JSONObject delays = new JSONObject();

        try {
            Connection connection = DatabaseConnection.getConnection(url, user, password);

            String query = "SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_ar) WHERE ";

            try {
                String builder = "year = " + searchParamenters[1].substring(0, 4) + " ";
                query += builder;
            } catch (Exception e) {
                Log.showLogMessage(CLASS_NAME, "Failed parsing date. Cause: " + e.getMessage(), Log.ERROR_LOG);
            }

            try {
                String builder = "AND carrier = '" + searchParamenters[3] + "' ";
                query += builder;
            } catch (Exception e) {
                Log.showLogMessage(CLASS_NAME, "Carrier was not set to the query.", Log.INFO_LOG);
            }

            try {
                String builder = "AND airport = '" + searchParamenters[2] + "' ";
                query += builder;
            } catch (Exception e) {
                Log.showLogMessage(CLASS_NAME, "Airport was not set to the query.", Log.INFO_LOG);
            }

            try {
                String builder = "AND month = " + searchParamenters[1].substring(4, 6) + " ";
                query += builder;
            } catch (Exception e) {
                Log.showLogMessage(CLASS_NAME, "Month was not set to the query.", Log.INFO_LOG);
            }

            try {
                String builder = "AND day = " + searchParamenters[1].substring(6, 8) + " ";
                query += builder;
            } catch (Exception e) {
                Log.showLogMessage(CLASS_NAME, "Day was not set to the query.", Log.INFO_LOG);
            }

            Log.showLogMessage(CLASS_NAME, "Running query. This can take a few secons, please wait.", Log.INFO_LOG);

            delays.put(ARRIVAL_ON_TIME_FLIGHTS, getOnTimeFlights(connection, query));
            delays.put(ARRIVAL_DELAYED_FLIGHTS, getDelayedFlights(connection, query));
            delays.put(ARRIVAL_DELAYED_AVERAGE_TIME, getDelayedAverageTime(connection, query));
            ARRIVAL = false;
            delays.put(DEPARTURE_ON_TIME_FLIGHTS, getOnTimeFlights(connection, query));
            delays.put(DEPARTURE_DELAYED_FLIGHTS, getDelayedFlights(connection, query));
            delays.put(DEPARTURE_DELAYED_AVERAGE_TIME, getDelayedAverageTime(connection, query));
        } catch (SQLException e) {
            Log.showLogMessage(CLASS_NAME, "Not possible to connect with the database. Cause: " + e.getMessage(), Log.ERROR_LOG);
        }

        return delays;
    }

    // -----------------------------------------------------------------------//
    // End requisition query
    // =======================================================================//
    // =======================================================================//
    // Statistics queries
    // -----------------------------------------------------------------------//
    
    private int getOnTimeFlights(Connection connection, String query) throws SQLException {
        query += "AND arrival_delay <= 0;";
        String location = "arrival_delay";

        if (ARRIVAL) {
            query = query.replace("airport", "destination");
        } else {
            query = query.replace("airport", "origin");
            query = query.replace("arrival_delay", "departure_delay");
            location = "departure_delay";
        }

        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet queryResult = statement.executeQuery(query);

        queryResult.next();
        int result = queryResult.getInt("COUNT(" + location + ")");

        return result;
    }

    private int getDelayedFlights(Connection connection, String query) throws SQLException {
        query += "AND arrival_delay > 0;";
        String location = "arrival_delay";

        if (ARRIVAL) {
            query = query.replace("airport", "destination");
        } else {
            query = query.replace("airport", "origin");
            query = query.replace("arrival_delay", "departure_delay");
            location = "departure_delay";
        }

        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet queryResult = statement.executeQuery(query);

        queryResult.next();
        int result = queryResult.getInt("COUNT(" + location + ")");

        return result;
    }

    private double getDelayedAverageTime(Connection connection, String query) throws SQLException {
        query += "AND arrival_delay > 0;";
        query = query.replace("COUNT", "AVG");
        String location = "arrival_delay";

        if (ARRIVAL) {
            query = query.replace("airport", "destination");
        } else {
            query = query.replace("airport", "origin");
            query = query.replace("arrival_delay", "departure_delay");
            location = "departure_delay";
        }

        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet queryResult = statement.executeQuery(query);

        queryResult.next();
        int result = queryResult.getInt("AVG(" + location + ")");

        return result;
    }

    // -----------------------------------------------------------------------//
    // End statistics queries
    // =======================================================================//
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class DatabaseConnection {

    private static final String CLASS_NAME = "DatabaseConnection";

    public static Connection getConnection(String url, String user, String password) throws SQLException {
        Connection connection;

        try {
            connection = DriverManager.getConnection(url, user, password);

            return connection;
        } catch (SQLException e) {
            throw new SQLException(e.getCause().getMessage());
        }
    }
}

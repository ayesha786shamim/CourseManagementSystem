import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL with trustServerCertificate set to true and integratedSecurity
    private static final String URL = "jdbc:sqlserver://AYESHA786\\SQLEXPRESS;databaseName=CourseManagementSystem;integratedSecurity=true;trustServerCertificate=true;";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Establish the connection
            connection = DriverManager.getConnection(URL);
            System.out.println("Connected to database");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load JDBC driver: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
        return connection;
    }

    public static boolean isConnected() {
        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Database connection is successful!");
                return true;
            } else {
                System.err.println("Failed to establish a database connection.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            return false;
        }
    }
}

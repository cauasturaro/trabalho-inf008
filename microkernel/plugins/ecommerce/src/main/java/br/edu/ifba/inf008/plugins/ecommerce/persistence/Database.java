package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens JDBC connections to the e-commerce MariaDB database provided with the
 * assignment (see docker-compose.yml at the repository root).
 *
 * <p>The MariaDB driver is registered explicitly because this plugin runs
 * under its own {@link java.net.URLClassLoader}: the {@code DriverManager}
 * service discovery that normally auto-registers drivers only scans the
 * application class loader, so without this the driver bundled inside the
 * plugin jar would never be found.</p>
 */
public final class Database {

    private static final String URL = "jdbc:mariadb://localhost:3306/ecommerce_inf008";
    private static final String USER = "inf008";
    private static final String PASSWORD = "inf008";

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver", true, Database.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MariaDB JDBC driver not found in plugin jar.", e);
        }
    }

    private Database() {
    }

    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

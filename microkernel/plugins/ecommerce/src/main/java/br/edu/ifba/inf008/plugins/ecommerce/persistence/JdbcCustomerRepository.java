package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.CustomerType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link CustomerRepository}.
 */
public class JdbcCustomerRepository implements CustomerRepository {

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT id, full_name, email, customer_type FROM customers ORDER BY full_name";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                customers.add(new Customer(
                        resultSet.getLong("id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("email"),
                        CustomerType.valueOf(resultSet.getString("customer_type"))));
            }
            return customers;
        } catch (SQLException e) {
            throw new PersistenceException("Could not load customers.", e);
        }
    }
}

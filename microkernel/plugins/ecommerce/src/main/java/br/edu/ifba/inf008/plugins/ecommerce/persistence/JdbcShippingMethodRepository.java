package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link ShippingMethodRepository}.
 */
public class JdbcShippingMethodRepository implements ShippingMethodRepository {

    @Override
    public List<ShippingMethod> findAll() {
        String sql = "SELECT id, code, name, base_cost, estimated_days, free_shipping_threshold "
                + "FROM shipping_methods ORDER BY base_cost";
        List<ShippingMethod> methods = new ArrayList<>();
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                methods.add(new ShippingMethod(
                        resultSet.getLong("id"),
                        resultSet.getString("code"),
                        resultSet.getString("name"),
                        resultSet.getBigDecimal("base_cost"),
                        resultSet.getInt("estimated_days"),
                        resultSet.getBigDecimal("free_shipping_threshold")));
            }
            return methods;
        } catch (SQLException e) {
            throw new PersistenceException("Could not load shipping methods.", e);
        }
    }
}

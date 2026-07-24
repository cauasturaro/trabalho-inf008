package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ProductRepository}. The available quantity is
 * computed from {@code stock_movements}: INBOUND adds, OUTBOUND and RESERVED
 * subtract.
 */
public class JdbcProductRepository implements ProductRepository {

    private static final String BASE_QUERY =
            "SELECT p.id, p.sku, p.name, p.description, p.unit_price, p.active, "
            + "COALESCE(SUM(CASE m.movement_type "
            + "  WHEN 'INBOUND' THEN m.quantity "
            + "  WHEN 'OUTBOUND' THEN -m.quantity "
            + "  WHEN 'RESERVED' THEN -m.quantity "
            + "  ELSE 0 END), 0) AS available_quantity "
            + "FROM products p "
            + "LEFT JOIN stock_movements m ON m.product_id = p.id "
            + "WHERE p.active = TRUE ";

    private static final String GROUP_BY =
            "GROUP BY p.id, p.sku, p.name, p.description, p.unit_price, p.active ";

    @Override
    public List<Product> findAllActive() {
        String sql = BASE_QUERY + GROUP_BY + "ORDER BY p.name";
        List<Product> products = new ArrayList<>();
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
            return products;
        } catch (SQLException e) {
            throw new PersistenceException("Could not load products.", e);
        }
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        String sql = BASE_QUERY + "AND p.sku = ? " + GROUP_BY;
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sku);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapProduct(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not load product " + sku + ".", e);
        }
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getLong("id"),
                resultSet.getString("sku"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("unit_price"),
                resultSet.getInt("available_quantity"),
                resultSet.getBoolean("active"));
    }
}

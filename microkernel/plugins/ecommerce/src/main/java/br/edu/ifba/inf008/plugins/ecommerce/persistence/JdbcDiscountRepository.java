package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link DiscountRepository}.
 */
public class JdbcDiscountRepository implements DiscountRepository {

    @Override
    public List<Discount> findAllActive() {
        String sql = "SELECT id, code, name, discount_type, value FROM discounts "
                + "WHERE active = TRUE ORDER BY name";
        List<Discount> discounts = new ArrayList<>();
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                discounts.add(new Discount(
                        resultSet.getLong("id"),
                        resultSet.getString("code"),
                        resultSet.getString("name"),
                        DiscountType.valueOf(resultSet.getString("discount_type")),
                        resultSet.getBigDecimal("value")));
            }
            return discounts;
        } catch (SQLException e) {
            throw new PersistenceException("Could not load discounts.", e);
        }
    }
}

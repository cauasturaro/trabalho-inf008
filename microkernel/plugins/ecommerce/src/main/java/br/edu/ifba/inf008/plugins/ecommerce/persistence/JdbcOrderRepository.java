package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.OrderItem;
import br.edu.ifba.inf008.plugins.ecommerce.domain.OrderStatus;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PaymentResult;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * JDBC implementation of {@link OrderRepository}. All the inserts belonging to
 * one order (order, items, discount, payment, stock movements) run inside a
 * single transaction: either everything is persisted or nothing is.
 *
 * <p>Stock is only reduced (OUTBOUND) when the order is confirmed as PAID;
 * a PENDING order reserves stock (RESERVED) instead, mirroring the movement
 * types already present in the seed data.</p>
 */
public class JdbcOrderRepository implements OrderRepository {

    @Override
    public Order save(Order order, ShippingMethod method, Discount discount) {
        try (Connection connection = Database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                long orderId = insertOrder(connection, order, method);
                order.setId(orderId);
                insertOrderItems(connection, orderId, order);
                if (discount != null && order.getDiscountTotal().signum() > 0) {
                    insertOrderDiscount(connection, orderId, discount, order);
                }
                insertPayment(connection, orderId, order);
                insertStockMovements(connection, order);
                connection.commit();
                return order;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not save the order.", e);
        }
    }

    private long insertOrder(Connection connection, Order order, ShippingMethod method)
            throws SQLException {
        String sql = "INSERT INTO orders (customer_id, cart_id, shipping_method_id, status, "
                + "subtotal, discount_total, shipping_total, grand_total) VALUES (?, NULL, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, order.getCustomer().getId());
            statement.setLong(2, method.getId());
            statement.setString(3, order.getStatus().name());
            statement.setBigDecimal(4, order.getSubtotal());
            statement.setBigDecimal(5, order.getDiscountTotal());
            statement.setBigDecimal(6, order.getShippingTotal());
            statement.setBigDecimal(7, order.getGrandTotal());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private void insertOrderItems(Connection connection, long orderId, Order order)
            throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (OrderItem item : order.getItems()) {
                statement.setLong(1, orderId);
                statement.setLong(2, item.getProduct().getId());
                statement.setInt(3, item.getQuantity());
                statement.setBigDecimal(4, item.getUnitPrice());
                statement.setBigDecimal(5, item.getLineTotal());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertOrderDiscount(Connection connection, long orderId, Discount discount,
                                     Order order) throws SQLException {
        String sql = "INSERT INTO order_discounts (order_id, discount_id, amount) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setLong(2, discount.getId());
            statement.setBigDecimal(3, order.getDiscountTotal());
            statement.executeUpdate();
        }
    }

    private void insertPayment(Connection connection, long orderId, Order order)
            throws SQLException {
        PaymentResult result = order.getPaymentResult();
        if (result == null) {
            return;
        }
        String sql = "INSERT INTO payments (order_id, payment_method, status, amount, "
                + "transaction_reference, failure_reason, paid_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setString(2, order.getPaymentMethodCode());
            statement.setString(3, result.getStatus().name());
            statement.setBigDecimal(4, order.getGrandTotal());
            statement.setString(5, result.getTransactionReference());
            statement.setString(6, result.getFailureReason());
            switch (result.getStatus()) {
                case APPROVED:
                    statement.setTimestamp(7, Timestamp.from(Instant.now()));
                    break;
                default:
                    statement.setTimestamp(7, null);
            }
            statement.executeUpdate();
        }
    }

    private void insertStockMovements(Connection connection, Order order) throws SQLException {
        String movementType;
        String reason;
        if (order.getStatus() == OrderStatus.PAID) {
            movementType = "OUTBOUND";
            reason = "Confirmed order";
        } else if (order.getStatus() == OrderStatus.PENDING) {
            movementType = "RESERVED";
            reason = "Pending order";
        } else {
            return; // cancelled / invalid orders do not touch stock
        }

        String sql = "INSERT INTO stock_movements (product_id, movement_type, quantity, reason) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (OrderItem item : order.getItems()) {
                statement.setLong(1, item.getProduct().getId());
                statement.setString(2, movementType);
                statement.setInt(3, item.getQuantity());
                statement.setString(4, reason);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}

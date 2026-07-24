package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PaymentResult;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests against the MariaDB instance from docker-compose. They are
 * skipped automatically when the database is not reachable, so the build stays
 * green in environments without docker.
 */
class JdbcRepositoriesTest {

    private static boolean databaseAvailable;

    @BeforeAll
    static void checkDatabase() {
        try (Connection connection = Database.openConnection()) {
            databaseAvailable = connection.isValid(2);
        } catch (Exception e) {
            databaseAvailable = false;
        }
        assumeTrue(databaseAvailable, "MariaDB not reachable; skipping integration tests.");
    }

    @Test
    void loadsSeedData() {
        List<Product> products = new JdbcProductRepository().findAllActive();
        List<Customer> customers = new JdbcCustomerRepository().findAll();
        List<ShippingMethod> methods = new JdbcShippingMethodRepository().findAll();
        List<Discount> discounts = new JdbcDiscountRepository().findAllActive();

        assertFalse(products.isEmpty());
        assertFalse(customers.isEmpty());
        assertEquals(4, methods.size());
        // Seed has 4 discounts, one inactive.
        assertEquals(3, discounts.size());
        // Laptop: 8 INBOUND - 1 OUTBOUND = 7 available.
        Product laptop = new JdbcProductRepository().findBySku("NB-IDEA-14").orElseThrow(
                () -> new AssertionError("Seed product NB-IDEA-14 not found"));
        assertEquals(7, laptop.getAvailableQuantity());
    }

    @Test
    void savesPaidOrderWithItemsPaymentAndStockMovement() throws Exception {
        JdbcProductRepository products = new JdbcProductRepository();
        Customer customer = new JdbcCustomerRepository().findAll().get(0);
        ShippingMethod method = new JdbcShippingMethodRepository().findAll().get(0);
        Product product = products.findBySku("MS-WIRE-01").orElseThrow(
                () -> new AssertionError("Seed product MS-WIRE-01 not found"));
        int availableBefore = product.getAvailableQuantity();

        Cart cart = new Cart(customer);
        cart.addProduct(product, 2);
        Order order = Order.fromCart(cart);
        order.setStatus(br.edu.ifba.inf008.plugins.ecommerce.domain.OrderStatus.PAID);
        order.setPaymentResult(PaymentResult.approved("test_ref"));
        order.setPaymentMethodCode("CREDIT_CARD");

        long stockMovementWatermark = maxId("stock_movements");
        Order saved = new JdbcOrderRepository().save(order, method, null);
        try {
            assertNotNull(saved.getId());
            assertEquals(1, countRows("order_items", "order_id", saved.getId()));
            assertEquals(1, countRows("payments", "order_id", saved.getId()));
            int availableAfter = products.findBySku("MS-WIRE-01").get().getAvailableQuantity();
            assertEquals(availableBefore - 2, availableAfter);
        } finally {
            cleanupOrder(saved.getId(), stockMovementWatermark);
        }
        assertTrue(databaseAvailable);
    }

    private long maxId(String table) throws Exception {
        try (Connection connection = Database.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COALESCE(MAX(id), 0) FROM " + table)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private int countRows(String table, String column, long value) throws Exception {
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?")) {
            statement.setLong(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private void cleanupOrder(Long orderId, long stockMovementWatermark) throws Exception {
        try (Connection connection = Database.openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM payments WHERE order_id = " + orderId);
            statement.executeUpdate("DELETE FROM order_discounts WHERE order_id = " + orderId);
            statement.executeUpdate("DELETE FROM order_items WHERE order_id = " + orderId);
            statement.executeUpdate("DELETE FROM orders WHERE id = " + orderId);
            statement.executeUpdate("DELETE FROM stock_movements WHERE id > " + stockMovementWatermark);
        }
    }
}

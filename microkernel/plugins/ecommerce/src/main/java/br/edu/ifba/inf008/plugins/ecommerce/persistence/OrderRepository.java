package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;

/**
 * Write access to orders. Saving an order persists, in a single transaction,
 * the order row, its items, the applied discount (if any), the payment
 * attempt and the stock movements resulting from the order.
 */
public interface OrderRepository {

    /**
     * Persists the order and everything attached to it.
     *
     * @param order    the processed order (totals, status and payment result set)
     * @param method   the shipping method chosen at checkout
     * @param discount the discount applied, or {@code null} when none
     * @return the same order with its generated id filled in
     */
    Order save(Order order, ShippingMethod method, Discount discount);
}

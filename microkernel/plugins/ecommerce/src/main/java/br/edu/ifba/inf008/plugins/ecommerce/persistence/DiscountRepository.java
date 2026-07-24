package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;

import java.util.List;

/**
 * Read access to discounts.
 */
public interface DiscountRepository {

    /** Only discounts flagged as active. */
    List<Discount> findAllActive();
}

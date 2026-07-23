package br.edu.ifba.inf008.plugins.ecommerce.domain.discount;

/**
 * How a coupon value is interpreted, matching the {@code discount_type} column.
 */
public enum DiscountType {
    /** {@code value} is a percentage of the subtotal. */
    PERCENTAGE,
    /** {@code value} is a fixed amount subtracted from the subtotal. */
    FIXED_AMOUNT
}

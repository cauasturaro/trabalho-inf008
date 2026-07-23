package br.edu.ifba.inf008.plugins.ecommerce.domain.exception;

/**
 * Thrown when the quantity requested for a product is greater than the
 * quantity currently available in stock.
 */
public class InsufficientStockException extends Exception {

    private final String productSku;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String productSku, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock for product " + productSku
                + ": requested " + requestedQuantity + ", available " + availableQuantity + ".");
        this.productSku = productSku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getProductSku() {
        return productSku;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}

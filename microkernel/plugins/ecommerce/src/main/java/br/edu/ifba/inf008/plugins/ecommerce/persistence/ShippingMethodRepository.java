package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;

import java.util.List;

/**
 * Read access to shipping methods.
 */
public interface ShippingMethodRepository {

    List<ShippingMethod> findAll();
}

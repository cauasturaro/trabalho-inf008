package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;

import java.util.List;

/**
 * Read access to customers.
 */
public interface CustomerRepository {

    List<Customer> findAll();
}

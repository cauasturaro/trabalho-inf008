package br.edu.ifba.inf008.plugins.ecommerce.domain;

import java.util.Objects;

/**
 * A customer of the store. The {@link CustomerType} is relevant to the
 * business rules (e.g. student discount eligibility).
 */
public class Customer {

    private Long id;
    private String fullName;
    private String email;
    private CustomerType customerType;

    public Customer(Long id, String fullName, String email, CustomerType customerType) {
        this.id = id;
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.email = Objects.requireNonNull(email, "email");
        this.customerType = Objects.requireNonNull(customerType, "customerType");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public boolean isStudent() {
        return customerType == CustomerType.STUDENT;
    }

    @Override
    public String toString() {
        return fullName + " (" + customerType + ")";
    }
}

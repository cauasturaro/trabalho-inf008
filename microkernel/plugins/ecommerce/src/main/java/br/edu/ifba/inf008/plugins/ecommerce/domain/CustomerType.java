package br.edu.ifba.inf008.plugins.ecommerce.domain;

/**
 * Category of a customer. Drives which discount policies apply
 * (for instance, {@code STUDENT} is eligible for the student discount).
 */
public enum CustomerType {
    REGULAR,
    STUDENT
}

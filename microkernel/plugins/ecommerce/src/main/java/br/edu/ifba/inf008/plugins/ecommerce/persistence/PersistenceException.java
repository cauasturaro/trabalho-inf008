package br.edu.ifba.inf008.plugins.ecommerce.persistence;

/**
 * Wraps checked {@link java.sql.SQLException}s so callers above the
 * persistence layer deal with a single unchecked exception type and the UI can
 * present a friendly message.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}

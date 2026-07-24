package br.edu.ifba.inf008.plugins.ecommerce.ui;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.BoletoPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.CreditCardPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PixPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.PersistenceException;
import br.edu.ifba.inf008.plugins.ecommerce.service.OrderService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

/**
 * Tab that closes the order: pick discount, shipping and payment method, see
 * the live totals (subtotal - discount + shipping) and confirm. All business
 * rules run in the service/domain layers; handlers here only collect the
 * selection, delegate and translate exceptions into alerts.
 */
public class CheckoutView extends VBox {

    private static final String NO_DISCOUNT = "No discount";

    private final OrderService orderService;
    private final CartSession session;

    private final ComboBox<Object> discountCombo = new ComboBox<>();
    private final ComboBox<ShippingMethod> shippingCombo = new ComboBox<>();
    private final ComboBox<String> paymentCombo = new ComboBox<>();

    private final TextField cardHolderField = new TextField();
    private final TextField cardNumberField = new TextField();
    private final PasswordField cardCodeField = new PasswordField();
    private final TextField cardLimitField = new TextField();
    private final TextField pixKeyField = new TextField();
    private final TextField boletoDocumentField = new TextField();

    private final GridPane cardForm = new GridPane();
    private final GridPane pixForm = new GridPane();
    private final GridPane boletoForm = new GridPane();

    private final Label subtotalLabel = new Label("Subtotal: -");
    private final Label discountLabel = new Label("Discount: -");
    private final Label shippingLabel = new Label("Shipping: -");
    private final Label totalLabel = new Label("Total: -");
    private final Label statusLabel = new Label("");

    public CheckoutView(OrderService orderService, CartSession session) {
        this.orderService = orderService;
        this.session = session;
        buildLayout();
        session.addListener(this::refreshSummary);
        refreshChoices();
    }

    private void buildLayout() {
        setSpacing(10);
        setPadding(new Insets(10));

        discountCombo.setPromptText("Discount");
        discountCombo.setOnAction(event -> refreshSummary());
        shippingCombo.setPromptText("Shipping method");
        shippingCombo.setOnAction(event -> refreshSummary());

        paymentCombo.setItems(FXCollections.observableArrayList(
                "Credit card", "Pix", "Boleto"));
        paymentCombo.getSelectionModel().selectFirst();
        paymentCombo.setOnAction(event -> showSelectedPaymentForm());

        buildPaymentForms();

        Button reloadButton = new Button("Reload data");
        reloadButton.setOnAction(event -> refreshChoices());

        Button confirmButton = new Button("Confirm order");
        confirmButton.setOnAction(event -> confirmOrder());

        GridPane selection = new GridPane();
        selection.setHgap(8);
        selection.setVgap(8);
        selection.addRow(0, new Label("Discount:"), discountCombo);
        selection.addRow(1, new Label("Shipping:"), shippingCombo);
        selection.addRow(2, new Label("Payment:"), paymentCombo);

        VBox summary = new VBox(4, subtotalLabel, discountLabel, shippingLabel, totalLabel);

        getChildren().addAll(selection, cardForm, pixForm, boletoForm,
                summary, new VBox(4, confirmButton, reloadButton), statusLabel);
        showSelectedPaymentForm();
    }

    private void buildPaymentForms() {
        cardHolderField.setPromptText("Card holder name");
        cardNumberField.setPromptText("Card number");
        cardCodeField.setPromptText("CVV");
        cardLimitField.setPromptText("Credit limit (optional, simulation)");
        cardForm.setHgap(8);
        cardForm.setVgap(8);
        cardForm.addRow(0, new Label("Holder:"), cardHolderField);
        cardForm.addRow(1, new Label("Number:"), cardNumberField);
        cardForm.addRow(2, new Label("CVV:"), cardCodeField);
        cardForm.addRow(3, new Label("Limit:"), cardLimitField);

        pixKeyField.setPromptText("Pix key");
        pixForm.setHgap(8);
        pixForm.setVgap(8);
        pixForm.addRow(0, new Label("Pix key:"), pixKeyField);

        boletoDocumentField.setPromptText("Payer document (CPF)");
        boletoForm.setHgap(8);
        boletoForm.setVgap(8);
        boletoForm.addRow(0, new Label("Document:"), boletoDocumentField);
    }

    private void showSelectedPaymentForm() {
        String selected = paymentCombo.getValue();
        cardForm.setVisible("Credit card".equals(selected));
        cardForm.setManaged(cardForm.isVisible());
        pixForm.setVisible("Pix".equals(selected));
        pixForm.setManaged(pixForm.isVisible());
        boletoForm.setVisible("Boleto".equals(selected));
        boletoForm.setManaged(boletoForm.isVisible());
    }

    /** Reloads discounts and shipping methods from the database. */
    public void refreshChoices() {
        try {
            java.util.List<Object> discounts = new java.util.ArrayList<>();
            discounts.add(NO_DISCOUNT);
            discounts.addAll(orderService.listDiscounts());
            discountCombo.setItems(FXCollections.observableArrayList(discounts));
            discountCombo.getSelectionModel().selectFirst();
            shippingCombo.setItems(
                    FXCollections.observableArrayList(orderService.listShippingMethods()));
        } catch (PersistenceException e) {
            error("Could not load checkout data. Is the database running?");
        }
    }

    private void refreshSummary() {
        if (!session.isReady() || session.getCart().isEmpty() || shippingCombo.getValue() == null) {
            subtotalLabel.setText("Subtotal: -");
            discountLabel.setText("Discount: -");
            shippingLabel.setText("Shipping: -");
            totalLabel.setText("Total: -");
            return;
        }
        Order preview = orderService.previewOrder(
                session.getCart(), selectedDiscount(), shippingCombo.getValue());
        subtotalLabel.setText("Subtotal: " + Formats.currency(preview.getSubtotal()));
        discountLabel.setText("Discount: -" + Formats.currency(preview.getDiscountTotal()));
        shippingLabel.setText("Shipping: +" + Formats.currency(preview.getShippingTotal()));
        totalLabel.setText("Total: " + Formats.currency(preview.getGrandTotal()));
    }

    private void confirmOrder() {
        if (!session.isReady() || session.getCart().isEmpty()) {
            warn("Cart is empty. Add products before confirming the order.");
            return;
        }
        if (shippingCombo.getValue() == null) {
            warn("Select a shipping method.");
            return;
        }
        try {
            Order order = orderService.placeOrder(session.getCart(), selectedDiscount(),
                    shippingCombo.getValue(), buildPayment());
            statusLabel.setText("Order #" + order.getId() + " - status: " + order.getStatus());
            new Alert(Alert.AlertType.INFORMATION,
                    "Order #" + order.getId() + " processed with status " + order.getStatus()
                            + ".\nTotal: " + Formats.currency(order.getGrandTotal())).showAndWait();
            session.reset();
        } catch (InvalidPaymentException e) {
            statusLabel.setText("Payment invalid: " + e.getMessage());
            warn("Invalid payment: " + e.getMessage());
        } catch (PersistenceException e) {
            error("Could not save the order. Is the database running?");
        }
    }

    private Discount selectedDiscount() {
        Object selected = discountCombo.getValue();
        return selected instanceof Discount ? (Discount) selected : null;
    }

    /** Builds the {@link Payable} for the selected method from the form fields. */
    private Payable buildPayment() {
        String selected = paymentCombo.getValue();
        if ("Pix".equals(selected)) {
            return new PixPayment(pixKeyField.getText());
        }
        if ("Boleto".equals(selected)) {
            return new BoletoPayment(boletoDocumentField.getText());
        }
        String limitText = cardLimitField.getText();
        BigDecimal limit = limitText == null || limitText.trim().isEmpty()
                ? null : Money.of(limitText.trim());
        return new CreditCardPayment(cardHolderField.getText(), cardNumberField.getText(),
                cardCodeField.getText(), limit);
    }

    private void warn(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void error(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}

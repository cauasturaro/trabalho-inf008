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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab that closes the order: pick discount, shipping and payment method, see
 * the live totals (subtotal - discount + shipping) and confirm. All business
 * rules run in the service/domain layers; handlers here only collect the
 * selection, delegate and translate exceptions into alerts.
 */
public class CheckoutView extends HBox {

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

    private final Label subtotalValue = new Label("—");
    private final Label discountValue = new Label("—");
    private final Label shippingValue = new Label("—");
    private final Label totalValue = new Label("—");
    private final Label statusLabel = new Label("");

    public CheckoutView(OrderService orderService, CartSession session) {
        this.orderService = orderService;
        this.session = session;
        buildLayout();
        Theme.apply(this);
        session.addListener(this::refreshSummary);
        refreshChoices();
    }

    private void buildLayout() {
        setSpacing(16);
        setPadding(new Insets(16));

        getChildren().addAll(buildOptionsColumn(), buildSummaryColumn());
    }

    private VBox buildOptionsColumn() {
        Label title = new Label("Checkout");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Choose discount, shipping and payment");
        subtitle.getStyleClass().add("subtitle");
        VBox heading = new VBox(2, title, subtitle);

        discountCombo.setPromptText("Discount");
        discountCombo.setMaxWidth(Double.MAX_VALUE);
        discountCombo.setOnAction(event -> refreshSummary());
        shippingCombo.setPromptText("Shipping method");
        shippingCombo.setMaxWidth(Double.MAX_VALUE);
        shippingCombo.setOnAction(event -> refreshSummary());

        paymentCombo.setItems(FXCollections.observableArrayList(
                "Credit card", "Pix", "Boleto"));
        paymentCombo.getSelectionModel().selectFirst();
        paymentCombo.setMaxWidth(Double.MAX_VALUE);
        paymentCombo.setOnAction(event -> showSelectedPaymentForm());

        GridPane selection = new GridPane();
        selection.setHgap(12);
        selection.setVgap(10);
        ColumnConstraints labelColumn = new ColumnConstraints(80);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        selection.getColumnConstraints().addAll(labelColumn, fieldColumn);
        selection.addRow(0, new Label("Discount:"), discountCombo);
        selection.addRow(1, new Label("Shipping:"), shippingCombo);
        selection.addRow(2, new Label("Payment:"), paymentCombo);

        Label optionsCaption = new Label("ORDER OPTIONS");
        optionsCaption.getStyleClass().add("section-label");
        VBox optionsCard = new VBox(12, optionsCaption, selection);
        optionsCard.getStyleClass().add("card");

        buildPaymentForms();
        Label paymentCaption = new Label("PAYMENT DETAILS");
        paymentCaption.getStyleClass().add("section-label");
        VBox paymentCard = new VBox(12, paymentCaption, cardForm, pixForm, boletoForm);
        paymentCard.getStyleClass().add("card");
        showSelectedPaymentForm();

        Button reloadButton = new Button("Reload data");
        reloadButton.setOnAction(event -> refreshChoices());
        HBox reloadRow = new HBox(reloadButton);
        reloadRow.setAlignment(Pos.CENTER_LEFT);

        VBox column = new VBox(16, heading, optionsCard, paymentCard, reloadRow);
        HBox.setHgrow(column, Priority.ALWAYS);
        return column;
    }

    private VBox buildSummaryColumn() {
        Label caption = new Label("ORDER SUMMARY");
        caption.getStyleClass().add("section-label");

        GridPane totals = new GridPane();
        totals.setHgap(12);
        totals.setVgap(8);
        ColumnConstraints nameColumn = new ColumnConstraints();
        nameColumn.setHgrow(Priority.ALWAYS);
        totals.getColumnConstraints().add(nameColumn);
        totals.addRow(0, summaryName("Subtotal"), alignRight(subtotalValue, "summary-value"));
        totals.addRow(1, summaryName("Discount"), alignRight(discountValue, "summary-value"));
        totals.addRow(2, summaryName("Shipping"), alignRight(shippingValue, "summary-value"));

        Label totalName = new Label("Total");
        totalName.getStyleClass().add("total-name");
        GridPane totalRow = new GridPane();
        totalRow.setHgap(12);
        ColumnConstraints totalNameColumn = new ColumnConstraints();
        totalNameColumn.setHgrow(Priority.ALWAYS);
        totalRow.getColumnConstraints().add(totalNameColumn);
        totalRow.addRow(0, totalName, alignRight(totalValue, "total-value"));

        Button confirmButton = new Button("Confirm order");
        confirmButton.getStyleClass().add("button-primary");
        confirmButton.setMaxWidth(Double.MAX_VALUE);
        confirmButton.setOnAction(event -> confirmOrder());

        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);

        VBox card = new VBox(12, caption, totals, new Separator(), totalRow,
                confirmButton, statusLabel);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);
        card.setMinWidth(240);
        return new VBox(card);
    }

    private Label summaryName(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("summary-name");
        return label;
    }

    private Label alignRight(Label label, String styleClass) {
        label.getStyleClass().add(styleClass);
        GridPane.setHalignment(label, javafx.geometry.HPos.RIGHT);
        return label;
    }

    private void buildPaymentForms() {
        cardHolderField.setPromptText("Card holder name");
        cardNumberField.setPromptText("Card number");
        cardCodeField.setPromptText("CVV");
        cardLimitField.setPromptText("Credit limit (optional, simulation)");
        configureForm(cardForm);
        cardForm.addRow(0, new Label("Holder:"), cardHolderField);
        cardForm.addRow(1, new Label("Number:"), cardNumberField);
        cardForm.addRow(2, new Label("CVV:"), cardCodeField);
        cardForm.addRow(3, new Label("Limit:"), cardLimitField);

        pixKeyField.setPromptText("Pix key");
        configureForm(pixForm);
        pixForm.addRow(0, new Label("Pix key:"), pixKeyField);

        boletoDocumentField.setPromptText("Payer document (CPF)");
        configureForm(boletoForm);
        boletoForm.addRow(0, new Label("Document:"), boletoDocumentField);
    }

    private void configureForm(GridPane form) {
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints labelColumn = new ColumnConstraints(80);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelColumn, fieldColumn);
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
            List<Object> discounts = new ArrayList<>();
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
            subtotalValue.setText("—");
            discountValue.setText("—");
            shippingValue.setText("—");
            totalValue.setText("—");
            return;
        }
        Order preview = orderService.previewOrder(
                session.getCart(), selectedDiscount(), shippingCombo.getValue());
        subtotalValue.setText(Formats.currency(preview.getSubtotal()));
        discountValue.setText("-" + Formats.currency(preview.getDiscountTotal()));
        shippingValue.setText("+" + Formats.currency(preview.getShippingTotal()));
        totalValue.setText(Formats.currency(preview.getGrandTotal()));
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
            statusLabel.setText("Order #" + order.getId() + " — status: " + order.getStatus());
            Dialogs.info("Order #" + order.getId() + " processed with status " + order.getStatus()
                    + ".\nTotal: " + Formats.currency(order.getGrandTotal()));
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
        Dialogs.warn(message);
    }

    private void error(String message) {
        Dialogs.error(message);
    }
}

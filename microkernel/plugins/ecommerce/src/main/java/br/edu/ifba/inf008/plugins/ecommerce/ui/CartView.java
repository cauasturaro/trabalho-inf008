package br.edu.ifba.inf008.plugins.ecommerce.ui;

import br.edu.ifba.inf008.plugins.ecommerce.domain.CartItem;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InsufficientStockException;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.PersistenceException;
import br.edu.ifba.inf008.plugins.ecommerce.service.OrderService;
import br.edu.ifba.inf008.plugins.ecommerce.service.ProductService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Tab where the cart is assembled: pick a customer, pick a product and a
 * quantity, add or remove lines. Stock validation happens in the domain/service
 * layers; here the {@link InsufficientStockException} is only translated into
 * a friendly alert.
 */
public class CartView extends BorderPane {

    private final ProductService productService;
    private final OrderService orderService;
    private final CartSession session;

    private final ComboBox<Customer> customerCombo = new ComboBox<>();
    private final ComboBox<Product> productCombo = new ComboBox<>();
    private final Spinner<Integer> quantitySpinner = new Spinner<>(1, 999, 1);
    private final TableView<CartItem> table = new TableView<>();
    private final Label subtotalLabel = new Label("—");

    public CartView(ProductService productService, OrderService orderService, CartSession session) {
        this.productService = productService;
        this.orderService = orderService;
        this.session = session;
        buildLayout();
        Theme.apply(this);
        session.addListener(this::refreshCartTable);
        refreshChoices();
    }

    private void buildLayout() {
        customerCombo.setPromptText("Select customer");
        customerCombo.setOnAction(event -> {
            Customer selected = customerCombo.getValue();
            if (selected != null) {
                session.startFor(selected);
            }
        });

        productCombo.setPromptText("Select product");
        quantitySpinner.setPrefWidth(80);

        Button addButton = new Button("+ Add to cart");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(event -> addSelectedProduct());

        Button removeButton = new Button("Remove selected");
        removeButton.getStyleClass().add("button-danger");
        removeButton.setOnAction(event -> removeSelectedItem());

        Button reloadButton = new Button("Reload data");
        reloadButton.setOnAction(event -> refreshChoices());

        Label title = new Label("Cart");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Pick a customer and add products");
        subtitle.getStyleClass().add("subtitle");
        VBox heading = new VBox(2, title, subtitle);

        HBox customerRow = new HBox(8, new Label("Customer:"), customerCombo, reloadButton);
        customerRow.setAlignment(Pos.CENTER_LEFT);
        HBox productRow = new HBox(8, new Label("Product:"), productCombo,
                new Label("Quantity:"), quantitySpinner, addButton, removeButton);
        productRow.setAlignment(Pos.CENTER_LEFT);
        VBox top = new VBox(12, heading, customerRow, productRow);
        top.setPadding(new Insets(16));

        TableColumn<CartItem, String> productColumn = new TableColumn<>("Product");
        productColumn.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(cell.getValue().getProduct().getName()));
        productColumn.setPrefWidth(250);
        TableColumn<CartItem, Number> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(
                cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));
        TableColumn<CartItem, String> unitPriceColumn = new TableColumn<>("Unit price");
        unitPriceColumn.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(Formats.currency(cell.getValue().getUnitPrice())));
        TableColumn<CartItem, String> lineTotalColumn = new TableColumn<>("Line total");
        lineTotalColumn.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(Formats.currency(cell.getValue().getLineTotal())));

        table.getColumns().add(productColumn);
        table.getColumns().add(quantityColumn);
        table.getColumns().add(unitPriceColumn);
        table.getColumns().add(lineTotalColumn);
        table.setPlaceholder(new Label("Cart is empty."));

        subtotalLabel.getStyleClass().add("total-value");
        Label subtotalCaption = new Label("Cart subtotal");
        subtotalCaption.getStyleClass().add("summary-name");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottom = new HBox(8, spacer, subtotalCaption, subtotalLabel);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(12, 16, 16, 16));

        BorderPane.setMargin(table, new Insets(0, 16, 0, 16));
        setTop(top);
        setCenter(table);
        setBottom(bottom);
    }

    private void addSelectedProduct() {
        Product product = productCombo.getValue();
        if (!session.isReady()) {
            warn("Select a customer first.");
            return;
        }
        if (product == null) {
            warn("Select a product first.");
            return;
        }
        try {
            session.addProduct(product.getSku(), quantitySpinner.getValue());
        } catch (InsufficientStockException e) {
            warn(e.getMessage());
        } catch (PersistenceException e) {
            error("Could not read stock data. Is the database running?");
        }
    }

    private void removeSelectedItem() {
        CartItem selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warn("Select a cart line to remove.");
            return;
        }
        session.removeProduct(selected.getProduct());
    }

    /** Reloads customers and products from the database. */
    public void refreshChoices() {
        try {
            customerCombo.setItems(FXCollections.observableArrayList(orderService.listCustomers()));
            productCombo.setItems(FXCollections.observableArrayList(productService.listProducts()));
        } catch (PersistenceException e) {
            error("Could not load data. Is the database running?");
        }
    }

    private void refreshCartTable() {
        table.setItems(FXCollections.observableArrayList(session.getCart().getItems()));
        // Force cell re-rendering: merged lines keep the same CartItem instance,
        // so TableView would otherwise show the stale quantity and line total.
        table.refresh();
        subtotalLabel.setText(Formats.currency(session.getCart().getSubtotal()));
    }

    private void warn(String message) {
        Dialogs.warn(message);
    }

    private void error(String message) {
        Dialogs.error(message);
    }
}

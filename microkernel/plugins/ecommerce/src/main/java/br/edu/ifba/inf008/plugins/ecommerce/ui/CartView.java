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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private final Label subtotalLabel = new Label("Subtotal: -");

    public CartView(ProductService productService, OrderService orderService, CartSession session) {
        this.productService = productService;
        this.orderService = orderService;
        this.session = session;
        buildLayout();
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

        Button addButton = new Button("Add to cart");
        addButton.setOnAction(event -> addSelectedProduct());

        Button removeButton = new Button("Remove selected");
        removeButton.setOnAction(event -> removeSelectedItem());

        Button reloadButton = new Button("Reload data");
        reloadButton.setOnAction(event -> refreshChoices());

        HBox customerRow = new HBox(8, new Label("Customer:"), customerCombo, reloadButton);
        HBox productRow = new HBox(8, new Label("Product:"), productCombo,
                new Label("Quantity:"), quantitySpinner, addButton, removeButton);
        VBox top = new VBox(8, customerRow, productRow);
        top.setPadding(new Insets(10));

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

        subtotalLabel.setPadding(new Insets(10));

        setTop(top);
        setCenter(table);
        setBottom(subtotalLabel);
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
        subtotalLabel.setText("Subtotal: " + Formats.currency(session.getCart().getSubtotal()));
    }

    private void warn(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void error(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}

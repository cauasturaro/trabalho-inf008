package br.edu.ifba.inf008.plugins.ecommerce.ui;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.PersistenceException;
import br.edu.ifba.inf008.plugins.ecommerce.service.ProductService;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tab that lists and searches products. Event handlers only delegate to
 * {@link ProductService}; no business rule lives here.
 */
public class ProductsView extends BorderPane {

    private final ProductService productService;
    private final TableView<Product> table = new TableView<>();
    private final TextField searchField = new TextField();

    public ProductsView(ProductService productService) {
        this.productService = productService;
        buildLayout();
        refresh();
    }

    private void buildLayout() {
        searchField.setPromptText("Search by SKU or name");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(event -> refresh());
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            searchField.clear();
            refresh();
        });
        searchField.setOnAction(event -> refresh());

        Button newProductButton = new Button("New product");
        newProductButton.setOnAction(event -> openNewProductDialog());

        HBox toolbar = new HBox(8, new Label("Products"), searchField, searchButton, clearButton,
                newProductButton);
        toolbar.setPadding(new Insets(10));

        TableColumn<Product, String> skuColumn = new TableColumn<>("SKU");
        skuColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getSku()));
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));
        nameColumn.setPrefWidth(200);
        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(cell.getValue().getDescription()));
        descriptionColumn.setPrefWidth(300);
        TableColumn<Product, String> priceColumn = new TableColumn<>("Unit price");
        priceColumn.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(Formats.currency(cell.getValue().getUnitPrice())));
        TableColumn<Product, Number> stockColumn = new TableColumn<>("Available");
        stockColumn.setCellValueFactory(
                cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getAvailableQuantity()));

        table.getColumns().add(skuColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(descriptionColumn);
        table.getColumns().add(priceColumn);
        table.getColumns().add(stockColumn);

        setTop(toolbar);
        setCenter(table);
    }

    /**
     * Opens the product registration dialog. The handler only collects the
     * fields and delegates to {@link ProductService#registerProduct}; every
     * validation rule lives in the service.
     */
    private void openNewProductDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New product");
        dialog.setHeaderText("Register a new product");

        TextField skuField = new TextField();
        skuField.setPromptText("SKU (e.g. KB-MECH-02)");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField priceField = new TextField();
        priceField.setPromptText("Unit price (e.g. 199.90)");
        Spinner<Integer> quantitySpinner = new Spinner<>(0, 9999, 0);
        quantitySpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("SKU:"), skuField);
        grid.addRow(1, new Label("Name:"), nameField);
        grid.addRow(2, new Label("Description:"), descriptionField);
        grid.addRow(3, new Label("Unit price:"), priceField);
        grid.addRow(4, new Label("Initial stock:"), quantitySpinner);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(button -> {
            if (button != ButtonType.OK) {
                return;
            }
            try {
                productService.registerProduct(skuField.getText(), nameField.getText(),
                        descriptionField.getText(), parsePrice(priceField.getText()),
                        quantitySpinner.getValue());
                refresh();
            } catch (IllegalArgumentException e) {
                new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
            } catch (PersistenceException e) {
                new Alert(Alert.AlertType.ERROR,
                        "Could not save the product. Is the database running?").showAndWait();
            }
        });
    }

    /** Accepts both "199.90" and "199,90"; invalid text becomes null (service rejects). */
    private BigDecimal parsePrice(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Money.of(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Reloads the table from the service, applying the current search text. */
    public void refresh() {
        try {
            List<Product> products = productService.searchProducts(searchField.getText());
            table.setItems(FXCollections.observableArrayList(products));
        } catch (PersistenceException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not load products. Is the database running?").showAndWait();
        }
    }
}

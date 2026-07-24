package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.IUIController;

import br.edu.ifba.inf008.plugins.ecommerce.persistence.JdbcCustomerRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.JdbcDiscountRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.JdbcOrderRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.JdbcProductRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.JdbcShippingMethodRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ProductRepository;
import br.edu.ifba.inf008.plugins.ecommerce.service.CartService;
import br.edu.ifba.inf008.plugins.ecommerce.service.CheckoutService;
import br.edu.ifba.inf008.plugins.ecommerce.service.OrderService;
import br.edu.ifba.inf008.plugins.ecommerce.service.ProductService;
import br.edu.ifba.inf008.plugins.ecommerce.ui.CartSession;
import br.edu.ifba.inf008.plugins.ecommerce.ui.CartView;
import br.edu.ifba.inf008.plugins.ecommerce.ui.CheckoutView;
import br.edu.ifba.inf008.plugins.ecommerce.ui.ProductsView;

import javafx.scene.control.MenuItem;

/**
 * Entry point of the e-commerce plugin.
 *
 * <p>The microkernel loads this class by convention: the jar is named
 * {@code EcommercePlugin.jar} and {@code PluginController} instantiates
 * {@code br.edu.ifba.inf008.plugins.EcommercePlugin}. This class only wires
 * the layers together (repositories, services, views) and registers the tabs
 * and menu items through the {@code IUIController} exposed by the core.</p>
 */
public class EcommercePlugin implements IPlugin {

    @Override
    public boolean init() {
        IUIController ui = ICore.getInstance().getUIController();

        // Persistence layer
        ProductRepository productRepository = new JdbcProductRepository();

        // Application services
        ProductService productService = new ProductService(productRepository);
        CartService cartService = new CartService(productRepository);
        OrderService orderService = new OrderService(
                new CheckoutService(),
                new JdbcOrderRepository(),
                new JdbcCustomerRepository(),
                new JdbcShippingMethodRepository(),
                new JdbcDiscountRepository());

        // UI layer (shared cart session between the cart and checkout tabs)
        CartSession session = new CartSession(cartService);
        ProductsView productsView = new ProductsView(productService);
        CartView cartView = new CartView(productService, orderService, session);
        CheckoutView checkoutView = new CheckoutView(orderService, session);

        ui.createTab("Products", productsView);
        ui.createTab("Cart", cartView);
        ui.createTab("Checkout", checkoutView);

        MenuItem refreshMenuItem = ui.createMenuItem("E-commerce", "Reload data");
        refreshMenuItem.setOnAction(event -> {
            productsView.refresh();
            cartView.refreshChoices();
            checkoutView.refreshChoices();
        });

        return true;
    }
}

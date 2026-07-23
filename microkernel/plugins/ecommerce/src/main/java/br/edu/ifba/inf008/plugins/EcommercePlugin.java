package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.IUIController;

import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;

/**
 * Entry point of the e-commerce plugin.
 *
 * <p>The microkernel loads this class by convention: the jar is named
 * {@code EcommercePlugin.jar} and {@code PluginController} instantiates
 * {@code br.edu.ifba.inf008.plugins.EcommercePlugin}. All the e-commerce
 * functionality (domain, services, persistence and JavaFX views) lives under
 * {@code br.edu.ifba.inf008.plugins.ecommerce} and is wired here.</p>
 */
public class EcommercePlugin implements IPlugin {

    @Override
    public boolean init() {
        IUIController ui = ICore.getInstance().getUIController();

        MenuItem openMenuItem = ui.createMenuItem("E-commerce", "Order processing");
        openMenuItem.setOnAction(event -> System.out.println("E-commerce plugin loaded."));

        // Placeholder tab; replaced by the real views in later phases.
        ui.createTab("E-commerce", new StackPane(new Label("E-commerce plugin ready.")));

        return true;
    }
}

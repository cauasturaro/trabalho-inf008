package br.edu.ifba.inf008.plugins.ecommerce.ui;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;

/**
 * Applies the plugin stylesheet to its views. The stylesheet is also attached
 * to the {@link Scene} once the view is shown, so the surrounding chrome
 * provided by the core (menu bar, tab pane) picks up the same visual theme
 * without any change to the core code.
 */
final class Theme {

    private static final String STYLESHEET =
            Theme.class.getResource("/ecommerce.css").toExternalForm();

    private Theme() {
    }

    static void apply(Parent view) {
        if (!view.getStylesheets().contains(STYLESHEET)) {
            view.getStylesheets().add(STYLESHEET);
        }
        view.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null && !scene.getStylesheets().contains(STYLESHEET)) {
                scene.getStylesheets().add(STYLESHEET);
                moveTabsToTop(scene);
            }
        });
    }

    /**
     * The core places the tab strip at the bottom; move it to the top for a
     * more conventional layout. Done through a scene lookup so the core code
     * stays untouched; deferred with {@link Platform#runLater} because the
     * lookup needs a CSS pass to resolve.
     */
    private static void moveTabsToTop(Scene scene) {
        Platform.runLater(() -> {
            Node tabPane = scene.lookup(".tab-pane");
            if (tabPane instanceof TabPane) {
                ((TabPane) tabPane).setSide(Side.TOP);
            }
        });
    }
}

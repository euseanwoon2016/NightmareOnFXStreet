package com.oodj.vaccspace.utils;

import com.oodj.vaccspace.MainApplication;
import com.oodj.vaccspace.controllers.BaseController;
import com.oodj.vaccspace.mfx.CustomMFXDialog;
import io.github.palexdev.materialfx.controls.MFXStageDialog;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Navigator {
    private static final HashMap<String, Page> routeMap = new HashMap<>() {{
        put("base", new Page("views/base-view.fxml", "No Page"));
        put("login", new Page("views/login-view.fxml", "Login"));
        put("register", new Page("views/register-view.fxml", "Register"));
        put("dashboard", new Page("views/dashboard-view.fxml", "Dashboard"));
        put("new_appointment", new Page("views/new-appointment-view.fxml", "New Appointment"));
        put("view_appointment", new Page("views/view-appointment-view.fxml", "View Appointment"));
        put("vaccines", new Page("views/vaccines-view.fxml", "Vaccines"));
        put("vaccine_centers", new Page("views/vaccine-centers-view.fxml", "Vaccine Centers"));
    }};

    private static Stage primaryStage;
    private static Scene scene = null;
    private static boolean hasBeenInitialized = false;

    /**
     * Initialize the {@link Navigator} with the primary stage and optional initial route
     *
     * @param stage        Initialize the navigator on a specific page.
     * @param initialRoute Optional: Set the initial route.
     */
    public static void init(Stage stage, String initialRoute) {
        if (hasBeenInitialized) return;

        primaryStage = stage;

        String routeToLoad = initialRoute != null ? initialRoute : "base";

        navigate(routeToLoad);

        scene.getStylesheets()
             .add(Objects.requireNonNull(MainApplication.class.getResource("styles/main.css")).toExternalForm());

        primaryStage.show();

        hasBeenInitialized = true;

        setupOnCloseRequest();
    }

    /**
     * Navigate to given in the current window.
     *
     * @param route Route declared in {@link #routeMap}
     */
    public static void navigate(String route) {
        if (tryLoadScene(route, null)) return;

        primaryStage.setScene(scene);
    }

    public static void navigateInContainer(String route, BorderPane container, BorderPaneNodes nodes) {
        Parent root = loadPageFromFXML(route);

        switch (nodes) {
            case BOTTOM -> container.setBottom(root);
            case TOP -> container.setTop(root);
            case LEFT -> container.setLeft(root);
            case RIGHT -> container.setRight(root);
            case CENTER -> container.setCenter(root);
        }
    }

    public static void navigateInContainer(String route, Pane container) {
        Node root = loadPageFromFXML(route);
        container.getChildren().setAll(root);
    }

    /**
     * Opens route in new window
     *
     * @param route Route declared in {@link #routeMap}
     */
    public static void openInNewWindow(String route) {
        Stage newStage = createStage();

        if (tryLoadScene(route, newStage)) return;

        newStage.setScene(scene);
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.show();
    }

    public static void setWindowTitle(Stage targetStage, String title) {
        Stage stage = targetStage;

        if (stage == null) {
            stage = primaryStage;
        }

        stage.setTitle(String.format("%s | VaccSpace", title));
    }

    private static boolean tryLoadScene(String route, Stage newStage) {
        Parent root = loadPageFromFXML(route);

        if (root == null) {
            return true;
        }

        scene = new Scene(root);

        setWindowTitle(newStage, routeMap.get(route).getDisplayName());
        return false;
    }

    public static <T extends BaseController> void showInDialog(Window owner, String route, Object userData) {
        Page destination = routeMap.get(route);

        MFXStageDialog dialog = new MFXStageDialog();
        CustomMFXDialog content = new CustomMFXDialog();

        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(destination.getPath()));
            root = loader.load();

            // This is such a hacky way to do this.
            T controller = loader.getController();
            controller.setStageDialog(dialog);
            controller.setUserData(userData);
            controller.onLoaded();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.printf("Unable to load route '%s'.%n", route);
            return;
        }

        content.setCenter(root);

        dialog.setOwner(owner);

        dialog.setDialog(content);
        dialog.setModality(Modality.APPLICATION_MODAL);

        dialog.getDialog().setOverlayClose(true);

        dialog.setScrimOpacity(0.75);
        dialog.setScrimBackground(true);

        dialog.setAnimate(true);
        dialog.setAnimationMillis(200);

        dialog.setAllowDrag(false);
        dialog.setCenterInOwner(true);

        Region childRegion = (Region) content.getCenter();
        content.setPrefSize(childRegion.getPrefWidth(), childRegion.getPrefHeight());

        dialog.show();
    }

    protected static Parent loadPageFromFXML(String route) {
        Page destination = routeMap.get(route);
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(destination.getPath()));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.printf("Unable to load route '%s'.%n", route);
        }
        return root;
    }

    private static void setupOnCloseRequest() {
        primaryStage.setOnCloseRequest(event -> {
            // Add dispose methods here
            Platform.exit(); // GUI thread
            System.exit(0); // Normal exit, kills JVM
        });
    }

    private static Stage createStage() {
        Stage stage = new Stage();
        stage.getIcons()
             .add(new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("images/vaccine.png"))));
        return stage;
    }
}


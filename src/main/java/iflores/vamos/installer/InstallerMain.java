package iflores.vamos.installer;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class InstallerMain extends Application {

    private static InstallerMain _instance;

    public static void main(String[] args) throws Throwable {
        launch(args);
    }

    public static InstallerMain getInstance() {
        return _instance;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setOnCloseRequest(windowEvent -> System.exit(0));
        stage.getIcons().add(new Image("arrow-down.png"));
        _instance = this;
        stage.setTitle("Vamos! Installer");
        stage.setResizable(false);
        Wizard<VamosWizardState> wizard = new Wizard<>(new VamosWizardState(), IntroStep::new);
        Scene scene = new Scene(wizard, 500, 350);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, t -> {
            if (t.getCode() == KeyCode.ENTER) {
                t.consume();
                wizard.next(null);
            }
        });
        stage.setScene(scene);
        stage.show();
    }
}

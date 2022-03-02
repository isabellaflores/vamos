package iflores.vamos.installer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.InflaterInputStream;

public class InstallVamosStep extends WizardStep<VamosWizardState> {

    private final FinishStep _finishStep;
    private final TextArea _logTextArea = new TextArea();
    private final Node _progressPanel;
    private final Node _failurePanel;
    private final Node _successPanel;

    public InstallVamosStep(Wizard<VamosWizardState> wizard) {
        super(wizard);
        _finishStep = new FinishStep(wizard);
        setTop(new StandardWizardStepHeader(
                "Installing",
                "Please wait while Vamos! is being installed."
        ));
        _progressPanel = makeProgressPanel();
        _failurePanel = makeFailurePanel();
        _successPanel = makeSuccessPanel();
        _logTextArea.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        _logTextArea.setEditable(false);
    }

    private Node makeSuccessPanel() {
        VBox successPanel = new VBox();
        successPanel.setAlignment(Pos.CENTER);
        Label label1 = new Label("Vamos! has been installed.");
        Label label2 = new Label("Click 'Next' to continue.");
        label1.setFont(Font.font("SansSerif", FontWeight.NORMAL, 16));
        label2.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        label2.setPadding(new Insets(40, 0, 0, 0));
        successPanel.getChildren().addAll(label1, label2);
        return successPanel;
    }

    private Node makeFailurePanel() {
        BorderPane failurePanel = new BorderPane();
        Label failureLabel = new Label("Installation Failed.");
        HBox topPanel = new HBox();
        topPanel.setAlignment(Pos.CENTER);
        failureLabel.setPadding(new Insets(10, 10, 10, 10));
        failureLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        Button retryButton = new Button("Retry");
        retryButton.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        topPanel.getChildren().add(failureLabel);
        topPanel.getChildren().add(retryButton);
        failurePanel.setTop(topPanel);
        failurePanel.setCenter(_logTextArea);
        retryButton.setOnAction(e -> doInstall());
        return failurePanel;
    }

    @NotNull
    private Node makeProgressPanel() {
        VBox progressPanel = new VBox();
        progressPanel.setAlignment(Pos.CENTER);
        Label label = new Label("Installing Vamos!");
        Label label3 = new Label("Please wait...");
        label.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        label3.setFont(Font.font("SansSerif", FontWeight.NORMAL, 16));
        progressPanel.getChildren().add(label);
        progressPanel.getChildren().add(label3);
        ProgressIndicator progressIndicator = new ProgressIndicator(-1);
//        progressIndicator.setPrefWidth(200.0);
        VBox.setMargin(progressIndicator, new Insets(20, 0, 0, 0));
        progressPanel.getChildren().add(progressIndicator);
        return progressPanel;
    }

    @Override
    public boolean hasNextStep() {
        return true;
    }

    @Override
    public @NotNull WizardStep<VamosWizardState> getNextStep() {
        return _finishStep;
    }

    @Override
    public void init() {
        doInstall();
    }

    private void doInstall() {
        setCenter(_progressPanel);
        new Thread(
                () -> {
                    long startTime = System.currentTimeMillis();
                    StringBuilder logBuffer = new StringBuilder();
                    boolean ok = false;
                    try {
                        if (!Files.exists(getState().getDestinationFolder())) {
                            Files.createDirectory(getState().getDestinationFolder());
                        }
                        Path exePath = getState().getDestinationFolder().resolve("vamos.exe");
                        SignedMetadata metadata = getState().getSignedMetadataCollection().getMetadata(FileType.PKG);
                        byte[] bytes = metadata.getBytes();
                        byte[] buf = new byte[16384];
                        try (
                                InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
                                OutputStream out = Files.newOutputStream(exePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                        ) {
                            while (true) {
                                int count = in.read(buf);
                                if (count < 0) {
                                    break;
                                }
                                out.write(buf, 0, count);
                            }
                        }
                        if (getState().getCreateDesktopShortcut()) {
                            createShortcut("%HOMEDRIVE%%HOMEPATH%\\Desktop", exePath);
                        }
                        ok = true;
                    } catch (Throwable t) {
                        t.printStackTrace();
                        logBuffer.append(t);
                        logBuffer.append('\n');
                    } finally {
                        long endTime = System.currentTimeMillis();
                        long timeSpent = endTime - startTime;
                        if (timeSpent < 3000) {
                            Utils.sleep(3000 - timeSpent);
                        }
                        if (ok) {
                            Platform.runLater(() -> {
                                setCenter(_successPanel);
                                setNextButtonEnabled(true);
                                next();
                            });
                        } else {
                            Platform.runLater(() -> {
                                _logTextArea.setText(logBuffer.toString());
                                setCenter(_failurePanel);
                            });
                        }
                    }
                }
        ).start();
    }

    private void createShortcut(String linkPath, Path exePath) throws IOException, InterruptedException {
        int resultCode = runWScript(
                "Set oWS = WScript.CreateObject(\"WScript.Shell\")  \n"
                + "sLinkFile = oWS.ExpandEnvironmentStrings(\"" + linkPath + "\\Vamos!.lnk\")\n"
                + "Set oLink = oWS.CreateShortcut(sLinkFile)\n "
                + "oLink.TargetPath = oWS.ExpandEnvironmentStrings(\"" + exePath + "\")\n"
                + "oLink.Save\n"
        );
        if (resultCode != 0) {
            throw new RuntimeException("Error creating shortcut " + linkPath + " (Error code: " + resultCode + ")");
        }
    }

    private int runWScript(String script) throws IOException, InterruptedException {
        Path vbsPath = Files.createTempFile("temp", ".vbs");
        try {
            Files.write(vbsPath, script.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            Process p = Runtime.getRuntime().exec("wscript " + vbsPath);
            return p.waitFor();
        } finally {
            Files.delete(vbsPath);
        }
    }

    @Override
    public boolean isBackButtonEnabled() {
        return false;
    }

    @Override
    public boolean isCancelButtonEnabled() {
        return false;
    }

}

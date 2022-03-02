package iflores.vamos.installer;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.InflaterInputStream;

public class InstallOptionsStep extends WizardStep<VamosWizardState> {

    private final Label _spaceAvailableLabel;
    private final TextField _destinationFolderTextField;
    private final Label _spaceRequiredLabel;
    private final InstallVamosStep _installVamosStep;

    public InstallOptionsStep(Wizard<VamosWizardState> wizard) {
        super(wizard);
        _installVamosStep = new InstallVamosStep(wizard);
        setTop(new StandardWizardStepHeader(
                "Choose Installation Options",
                "Select a folder for Vamos! installation and additional options."
        ));
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        BorderPane contentPanel = new BorderPane();
        contentPanel.setPadding(new Insets(10, 20, 10, 20));

        VBox formPanel = new VBox();
//        formPanel.setPadding(new Insets(20, 30, 0, 30));
        addHeaderLabel(formPanel, "Create Shortcuts");
        CheckBox createDesktopShortcutCheckBox = addCheckBox(formPanel, "Vamos! Desktop Icon", getState().getCreateDesktopShortcut());
        VBox.setMargin(createDesktopShortcutCheckBox, new Insets(5, 0, 0, 10));
        contentPanel.setTop(formPanel);
        setCenter(contentPanel);
        createDesktopShortcutCheckBox.selectedProperty().addListener(
                (observable, oldValue, newValue) ->
                        getState().setCreateDesktopShortcut(newValue)
        );


        Font smallFont = Font.font("SansSerif", FontPosture.REGULAR, 12);
        contentPanel.setTop(formPanel);
        BorderPane bottomPanel = new BorderPane(); // TODO spacing... 10, 15
        BorderPane destinationFolderPanel = new BorderPane(); // TODO spacing... 10, 10
        Path localAppDataPath = Paths.get(System.getenv("LOCALAPPDATA"));
        _destinationFolderTextField = new TextField(localAppDataPath.resolve("Vamos").toString());
        _destinationFolderTextField.setFont(smallFont);
        destinationFolderPanel.setCenter(_destinationFolderTextField);
        Button browseButton = new Button("Browse...");
        browseButton.setFont(smallFont);
        browseButton.setPadding(new Insets(5, 20, 5, 20));
        BorderPane.setMargin(browseButton, new Insets(0, 0, 0, 15));
        destinationFolderPanel.setRight(browseButton);
        GroupBox groupBox = new GroupBox("Destination Folder", destinationFolderPanel);
        bottomPanel.setCenter(groupBox);
        BorderPane spaceAvailablePanel = new BorderPane();
        spaceAvailablePanel.setPadding(new Insets(20, 0, 10, 0));
        _spaceRequiredLabel = new Label();
        _spaceRequiredLabel.setFont(smallFont);
        spaceAvailablePanel.setTop(_spaceRequiredLabel);
        _spaceAvailableLabel = new Label();
        _spaceAvailableLabel.setFont(smallFont);
        spaceAvailablePanel.setBottom(_spaceAvailableLabel);
        bottomPanel.setBottom(spaceAvailablePanel);
        contentPanel.setBottom(bottomPanel);
        setCenter(contentPanel);
        _destinationFolderTextField.textProperty().addListener(observable -> {
            pathChanged();
        });
        pathChanged();

        getState().signedMetadataCollectionProperty().addListener(
                (observable, oldValue, newValue) -> {
                    _spaceRequiredLabel.setText("");
                    // calculate space required
                    SignedMetadata metadata = newValue.getMetadata(FileType.PKG);
                    byte[] bytes = metadata.getBytes();
                    byte[] buf = new byte[16384];
                    int totalSize = 0;
                    try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes))) {
                        while (true) {
                            int count = in.read(buf);
                            if (count < 0) {
                                break;
                            }
                            totalSize += count;
                        }
                        _spaceRequiredLabel.setText("Space required: " + (totalSize / (1024 * 1024)) + " MB");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        );
        setNextButtonEnabled(true);
        browseButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(getScene().getWindow());
            if (selectedDirectory != null) {
                Path selectedPath = selectedDirectory.toPath();
                Path vamosExePath = selectedPath.resolve("vamos.exe");
                if (Files.exists(vamosExePath)) {
                    _destinationFolderTextField.setText(selectedPath.toString());
                }
                else {
                    _destinationFolderTextField.setText(selectedPath.resolve("Vamos").toString());
                }
            }
        });
        _destinationFolderTextField.selectAll();
    }

    private CheckBox addCheckBox(VBox contentPanel, String text, boolean defaultValue) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.setSelected(defaultValue);
        checkBox.setFont(Font.font("SansSerif", FontWeight.NORMAL, 12));
        checkBox.setFocusTraversable(false);
        VBox.setMargin(checkBox, new Insets(2, 10, 2, 0));
        contentPanel.getChildren().add(checkBox);
        return checkBox;
    }

    private void addHeaderLabel(VBox contentPanel, @SuppressWarnings("SameParameterValue") String text) {
        Label label = new Label(text);
        label.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
        VBox.setMargin(label, new Insets(5, 0, 5, 0));
        contentPanel.getChildren().add(label);
    }

    private void pathChanged() {
        boolean ok = false;
        try {
            Path path = Paths.get(_destinationFolderTextField.getText());
            getState().setDestinationFolder(path);
            Path parent = path.getParent();
            path = parent == null ? path : parent;
            if (Files.exists(path)) {
                FileStore fileStore = Files.getFileStore(path);
                if (!fileStore.isReadOnly()) {
                    long usableSpace = fileStore.getUsableSpace();
                    _spaceAvailableLabel.setText("Space available: " + (usableSpace / (1024 * 1024)) + " MB");
                    ok = true;
                }
            }
        } catch (Throwable t) {
//            t.printStackTrace();
        } finally {
            setNextButtonEnabled(ok);
            if (!ok) {
                _spaceAvailableLabel.setText(" ");
            }
        }
    }

    @Override
    public boolean hasNextStep() {
        return true;
    }

    @Override
    public @NotNull WizardStep<VamosWizardState> getNextStep() {
        return _installVamosStep;
    }

    @Override
    public void init() {
    }

}

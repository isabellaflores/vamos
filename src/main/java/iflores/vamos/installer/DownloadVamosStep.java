package iflores.vamos.installer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.bouncycastle.pqc.jcajce.provider.sphincs.BCSphincs256PublicKey;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Base64;

public class DownloadVamosStep extends WizardStep<VamosWizardState> {

    public static final BCSphincs256PublicKey VAMOS_PUBLIC_KEY = Utils.makePublicKeyFromKeyData(
            Base64.getDecoder().decode(
                    "sUYN8/7qZJJFnqq5sD94RzTrUbWEGBZhd4vwBws66vKszQ/JiKaJ5/SgDoamoPr0ugmQ7UVM8N2CK6vls9x9SWp3YNmdI5lWhfmruKLQpKYVsDvdxOhMyJVt66FhmwgkdSL0rCjOY1e+ixbtdbMzrBwjUXCJEar4Sl+WEixDWoTtaxumpQ903cUbKNDHLQ0JcQHYzpWs29Eyg3Qey/iT5eociT4+eiir9kwL3qy5zp0jsk10UUvtVYeY3Rq32uo2XfbtHm/VCieNTTtjCZ5EzbYY4UeGmYVJODnhUma0cjDMU/uwMe+MTRlKhgy1aIHEwAgVzaLBhAUYzhGVlh2m0LVKInwvoFZr7CYVU2Jx7JWXnGYsECOVxZEy" +
                            "Bz12QMkXnemf/KNPg/yLleKCmpu6IS8J6w/xvoxHxgWZfJzPi6BQ70n7Z4IyBb9zFmp+ak7dA6L0K9UUmwVLymKuGO5Du0/vIItRWsKgIqRfEN5xZbrObi0f7nLypFwhJTIGfV3hhE1E0owOvkuGVWNejMdTkadpRF9mf1tZ7+kZ4fl4OOJwJczGUheWq2zWNuh8vVHy70LFmMP5qFdLNAQSQYAjbVQ9Rp0nKUFPbg7nG1BtC1qTYy+IFP8ZyLOxePJUqbdmcbFgCJcFt5QS59H3qblyUm5/giM7r7933PJ05Ewscw+bYRJUwYFxqxFyDgng3D/kYTv8IPCsquutKSfHQ2A6tD0hgAujht0Lktakkr1yIBaUdnPduBaeq4Ei85BjO/2Il" +
                            "5XqJoKgNb6xsrrEEyrjFDMLPGP8cPqAsb7DVzo3E3VejU21yGvcKrnj4aGQKbEHs1TJ5fehF0oj1gCKY0lJ9Cz6pIo52Oqld3unn7Ys9DEQD47caF/XpcJWsujxwhUIBXS1leYMwbPS+8orpZN3aPQht0z3cAKnEc0nTzeHUbEVmNWhCw7muyC4q81V3gtAbj5GJF6Uh2rkJcHyTH8vBApCM59DDhWEuN41dtlDY4Einn1RMT0VlfAMToLhCXfym5VqIJ7oNtgQ06X4OrHFefNWvvHZpd9OZPZ37RBXql/6iNG43AOPQm2PqCzocU0eacgMu3okXCnrdAWTySCcYzV522d+Xep2ow47cOuxWNfATsdOkyROMkxSmxr543l4LRIt3T9wfY" +
                            "KoqARm1yDQIhFqMH4r2k3pW472Q9aclUo/Zbo767r5y55EoHj5kEj62dJrmMCkn4fooxlxEtnBM57yP7Ydm+z2QCqsdLpimj9Ciio59nDMXBqHxXGeKBu+5RmvrovG8IIZe80qF4URYMT5Uju/dRAXvKPWerJNQiVerBN4bbG/HF+zZwbusarO0YWo9hbCoPgRdZKGUplbzuyqWI/LeQ5RVQE95V9ixVPZ8HghKfGAsSFA4uh4KDwe"
            )
    );
    public static final String VAMOS_DOWNLOAD_URL = "http://xrg6uexvj5gooixni7myqv76cdbsq6ie5f2nf4nreym3wuh4zb22cpyd.onion/vamos/vamos";
    private final Node _progressPanel;
    private final Node _failurePanel;
    private final Node _successPanel;
    private Label _progressLabel1;
    private Label _progressLabel2;
    private ProgressBar _progressBar;
    private final InstallOptionsStep _installOptionsStep;

    public DownloadVamosStep(Wizard<VamosWizardState> wizard) {
        super(wizard);
        _installOptionsStep = new InstallOptionsStep(wizard);
        setTop(new StandardWizardStepHeader(
                "Download Vamos!",
                "Fetch the Vamos! executable and check its digital signature."
        ));
        _progressPanel = makeProgressPanel();
        _failurePanel = makeFailurePanel();
        _successPanel = makeSuccessPanel();
    }

    private Node makeFailurePanel() {
        VBox failurePanel = new VBox();
        failurePanel.setAlignment(Pos.CENTER);
        Label label1 = new Label("Download Failed.");
        label1.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        Button retryButton = new Button("Retry");
        retryButton.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        VBox.setMargin(label1, new Insets(0, 0, 5, 0));
        failurePanel.getChildren().add(label1);
        VBox.setMargin(retryButton, new Insets(5, 0, 0, 0));
        failurePanel.getChildren().add(retryButton);
        retryButton.setOnAction(e -> doInstall());
        return failurePanel;
    }

    private Node makeSuccessPanel() {
        VBox successPanel = new VBox();
        successPanel.setAlignment(Pos.CENTER);
        Label label1 = new Label("Vamos! Download Complete");
        Label label2 = new Label("Click 'Next' to continue.");
        label1.setFont(Font.font("SansSerif", FontWeight.NORMAL, 16));
        label2.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        successPanel.getChildren().add(label1);
        successPanel.getChildren().add(label2);
        label2.setPadding(new Insets(20, 0, 0, 0));
        return successPanel;
    }

    private Node makeProgressPanel() {
        VBox progressPanel = new VBox();
        progressPanel.setAlignment(Pos.CENTER);
        Label label = new Label("Downloading Vamos!...");
        label.setPadding(new Insets(0, 0, 15, 0));
        label.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        progressPanel.getChildren().add(label);
        _progressBar = new ProgressBar(-1);
        _progressBar.setPrefWidth(200.0);
        progressPanel.getChildren().add(_progressBar);
        _progressLabel1 = new Label("");
        _progressLabel1.setPadding(new Insets(10, 0, 0, 0));
        _progressLabel2 = new Label("");
        _progressLabel1.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        _progressLabel2.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        progressPanel.getChildren().add(_progressLabel1);
        progressPanel.getChildren().add(_progressLabel2);
        _progressLabel1.setVisible(false);
        return progressPanel;
    }

    @Override
    public boolean hasNextStep() {
        return true;
    }

    @Override
    public @NotNull WizardStep<VamosWizardState> getNextStep() {
        return _installOptionsStep;
    }

    @Override
    public void init() {
        doInstall();
    }

    private void doInstall() {
        _progressLabel1.setText("");
        _progressLabel2.setText("Finding download server...");
        _progressLabel1.setVisible(false);
        _progressBar.setProgress(-1);
        setCenter(_progressPanel);
        new Thread(
                () -> {
                    long startTime = System.currentTimeMillis();
                    SignedMetadataCollection signedMetadataCollection = null;
                    try {
                        signedMetadataCollection = Utils.loadMetadata(
                                getState().getUseTorSocks() ? new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 9150)) : Proxy.NO_PROXY,
                                BigInteger.valueOf(3),
                                new ParsedVamosUrl(VAMOS_PUBLIC_KEY, VAMOS_DOWNLOAD_URL),
                                (fileType, signature) -> Platform.runLater(() -> _progressLabel1.setText("Downloading " + fileType + (signature ? " Signature" : ""))),
                                (progress, max) -> {
                                    double fractionDone = max == 0 ? 1.0 : ((double) progress / (double) max);
                                    Platform.runLater(() -> {
                                        _progressBar.setProgress(fractionDone);
                                        _progressLabel2.setText(NumberFormat.getNumberInstance().format(progress) + " bytes - " + NumberFormat.getPercentInstance().format(fractionDone) + " done");
                                        _progressLabel1.setVisible(true);
                                    });
                                },
                                FileType.PKG
                        );
                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        long endTime = System.currentTimeMillis();
                        long timeSpent = endTime - startTime;
                        if (timeSpent < 3000) {
                            Utils.sleep(3000 - timeSpent);
                        }
                        if (signedMetadataCollection != null) {
                            SignedMetadataCollection finalSignedMetadataCollection = signedMetadataCollection;
                            Platform.runLater(() -> {
                                getState().setSignedMetadataCollection(finalSignedMetadataCollection);
                                setCenter(_successPanel);
                                setNextButtonEnabled(true);
                                next();
                            });
                        } else {
                            Platform.runLater(() -> setCenter(_failurePanel));
                        }
                    }
                }
        ).start();
    }

}

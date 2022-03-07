package iflores.vamos.installer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TorBrowserStep extends WizardStep<VamosWizardState> {

    public static final String TOR_DOWNLOAD_URL = "https://www.torproject.org/download/";
    private final Node _progressPanel;
    private final Node _failurePanel;
    private final Node _successPanel;
    private final DownloadVamosStep _downloadVamosStep;

    public TorBrowserStep(Wizard<VamosWizardState> wizard) {
        super(wizard);
        _downloadVamosStep = new DownloadVamosStep(wizard);
        setTop(new StandardWizardStepHeader(
                "Verify Tor Browser Installation",
                "Tor is used to protect privacy."
        ));
        _progressPanel = makeProgressPanel();
        _failurePanel = makeFailurePanel();
        _successPanel = makeSuccessPanel();
    }

    private Node makeFailurePanel() {
        VBox failurePanel = new VBox();
        failurePanel.setAlignment(Pos.CENTER);
        Label label1 = new Label("Please Launch Tor Browser");
        Label label2 = new Label("and connect to Tor");
        label1.setTextFill(Color.RED);
        label2.setTextFill(Color.RED);
        Label label3 = new Label("Vamos requires Tor Browser");
        Label label4 = new Label("to be running at all times.");
        Hyperlink downloadLink = new Hyperlink("Download TOR Browser");
        downloadLink.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
        downloadLink.setBorder(null);
        label1.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        label2.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        label3.setFont(Font.font("SansSerif", FontWeight.NORMAL, 14));
        label4.setFont(Font.font("SansSerif", FontWeight.NORMAL, 14));
        label2.setPadding(new Insets(0, 0, 15, 0));
        failurePanel.getChildren().add(label1);
        failurePanel.getChildren().add(label2);
        failurePanel.getChildren().add(label3);
        failurePanel.getChildren().add(label4);
        failurePanel.getChildren().add(downloadLink);
        VBox.setMargin(downloadLink, new Insets(10, 0, 0, 0));
        downloadLink.setOnAction(event -> {
            try {
                InstallerMain.getInstance().getHostServices().showDocument(TOR_DOWNLOAD_URL);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        return failurePanel;
    }

    private Node makeSuccessPanel() {
        VBox successPanel = new VBox();
        successPanel.setAlignment(Pos.CENTER);
        Label label1 = new Label("TOR Connection Successful");
        Label label2 = new Label("Click 'Next' to continue.");
        label1.setFont(Font.font("SansSerif", FontWeight.NORMAL, 16));
        label2.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        label2.setPadding(new Insets(20, 0, 0, 0));
        successPanel.getChildren().addAll(label1, label2);
        return successPanel;
    }

    private Node makeProgressPanel() {
        VBox progressPanel = new VBox();
        progressPanel.setAlignment(Pos.CENTER);
        Label label = new Label("Connecting to Tor Browser...");
        label.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        label.setPadding(new Insets(10, 5, 10, 5));
        progressPanel.getChildren().add(label);
        ProgressIndicator progIndicator = new ProgressIndicator();
        progressPanel.getChildren().add(progIndicator);
        return progressPanel;
    }

    @Override
    public boolean hasNextStep() {
        return true;
    }

    @Override
    public @NotNull WizardStep<VamosWizardState> getNextStep() {
        return _downloadVamosStep;
    }

    @Override
    public void init() {
        doInstall();
    }

    private void doInstall() {
        setCenter(_progressPanel);
        final Lock lock = new ReentrantLock();
        final boolean[] done = { false };
        final boolean[] socksLooped = { false };
        final boolean[] directLooped = { false };
        // TOR SOCKS tester thread
        new Thread(() -> {
            while (true) {
                lock.lock();
                try {
                    if (done[0]) {
                        return;
                    }
                }
                finally {
                    lock.unlock();
                }
                try {
                    URL url = new URL("http://localhost:9150");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    try {
                        int responseCode = conn.getResponseCode();
                        String responseMessage = conn.getResponseMessage();
                        if (responseCode == 501 && "Tor is not an HTTP Proxy".equals(responseMessage)) {
                            lock.lock();
                            try {
                                done[0] = true;
                            }
                            finally {
                                lock.unlock();
                            }
                            Platform.runLater(() -> {
                                getState().setUseTorSocks(true);
                                setCenter(_successPanel);
                                setNextButtonEnabled(true);
                                next();
                            });
                            return;
                        }
                    } finally {
                        conn.disconnect();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                finally {
                    lock.lock();
                    try {
                        socksLooped[0] = true;
                    }
                    finally {
                        lock.unlock();
                    }
                    Utils.sleep(1000);
                }
            }
        }).start();
        // TOR non-SOCKS (direct) tester thread
        new Thread(() -> {
            // try up to 3 times
            for (int i = 0; i < 3; i++) {
                lock.lock();
                try {
                    if (done[0]) {
                        return;
                    }
                }
                finally {
                    lock.unlock();
                }
                try {
                    URL url = new URL(DownloadVamosStep.VAMOS_DOWNLOAD_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    try {
                        int responseCode = conn.getResponseCode();
                        if (responseCode == 200) {
                            lock.lock();
                            try {
                                done[0] = true;
                            }
                            finally {
                                lock.unlock();
                            }
                            Platform.runLater(() -> {
                                getState().setUseTorSocks(false);
                                setCenter(_successPanel);
                                setNextButtonEnabled(true);
                                next();
                            });
                            return;
                        }
                    } finally {
                        conn.disconnect();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                finally {
                    lock.lock();
                    try {
                        directLooped[0] = true;
                    }
                    finally {
                        lock.unlock();
                    }
                    Utils.sleep(1000);
                }
            }
        }).start();
        // show failure page after both methods failed at least once
        new Thread(
                () -> {
                    while (true) {
                        lock.lock();
                        try {
                            if (done[0]) {
                                return;
                            }
                            if (socksLooped[0] && directLooped[0]) {
                                Platform.runLater(() -> setCenter(_failurePanel));
                                return;
                            }
                        }
                        finally {
                            lock.unlock();
                        }
                        Utils.sleep(100);
                    }
                }
        ).start();
    }

}

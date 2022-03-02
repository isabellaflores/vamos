package iflores.vamos.installer;

import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.jetbrains.annotations.NotNull;

public class IntroStep extends WizardStep<VamosWizardState> {

    private final TorBrowserStep _torBrowserStep;

    public IntroStep(Wizard<VamosWizardState> wizard) {
        super(wizard);
        _torBrowserStep = new TorBrowserStep(wizard);
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        ImageView imageView = new ImageView("vamos.png");
        setLeft(imageView);
        TextFlow textFlow = new TextFlow();
        addText(textFlow, "Welcome to Vamos! Installer.\n\n", FontWeight.BOLD, 14);
        addText(textFlow, "This wizard will guide you through the installation of Vamos! Before starting the installation, make sure no previous installation of Vamos! is running.\n\n", FontWeight.NORMAL, 12);
        addText(textFlow, "Click 'Next' to continue.", FontWeight.BOLD, 12);
        textFlow.setPadding(new Insets(15, 15, 15, 15));
        setCenter(textFlow);
        setNextButtonEnabled(true);
    }

    private void addText(TextFlow textFlow, String text, FontWeight fontWeight, double fontSize) {
        Text t = new Text(text);
        t.setFont(Font.font("SansSerif", fontWeight, fontSize));
        textFlow.getChildren().add(t);
    }

    @Override
    public boolean hasNextStep() {
        return true;
    }

    @Override
    public @NotNull WizardStep<VamosWizardState> getNextStep() {
        return _torBrowserStep;
    }

    @Override
    public void init() {
    }

}

package iflores.vamos.installer;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
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

public class FinishStep extends WizardStep<VamosWizardState> {


    public FinishStep(Wizard<VamosWizardState> wizard) {
        super(wizard);
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        ImageView imageView = new ImageView("vamos.png");
        setLeft(imageView);
        TextFlow textFlow = new TextFlow();
        addText(textFlow, "Completing Vamos! Setup\n\n", FontWeight.BOLD, 14);
        addText(textFlow, "Vamos! has been installed on your computer.\n\n", FontWeight.NORMAL, 12);
        addText(textFlow, "Click 'Finish' to close Setup.", FontWeight.BOLD, 12);
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
        return false;
    }

    @Override
    public @NotNull WizardStep<VamosWizardState> getNextStep() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init() {
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

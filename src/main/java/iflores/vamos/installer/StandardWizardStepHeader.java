package iflores.vamos.installer;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StandardWizardStepHeader extends VBox {

    public StandardWizardStepHeader(String title, String detail) {
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        Label titleLabel = new Label(title);
        titleLabel.setPadding(new Insets(10, 0, 0, 15));
        titleLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        getChildren().add(titleLabel);
        Label detailLabel = new Label(detail);
        detailLabel.setFont(Font.font("SansSerif", FontWeight.NORMAL, 12));
        detailLabel.setPadding(new Insets(5, 0, 15, 25));
        getChildren().add(detailLabel);
        getChildren().add(new Separator(Orientation.HORIZONTAL));
    }

}

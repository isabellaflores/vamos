package iflores.vamos.installer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public final class GroupBox extends StackPane {

    public GroupBox(String title, Node content) {
        BorderPane contentPane = new BorderPane();
        BorderPane borderPane = new BorderPane();
        HBox textPane = new HBox();
        textPane.setMouseTransparent(true);
        textPane.setAlignment(Pos.TOP_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.setPadding(new Insets(0, 2, 0, 2));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 8));
        titleLabel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        titleLabel.heightProperty().addListener((observable, oldValue, newValue) -> {
            StackPane.setMargin(borderPane, new Insets((double) newValue / 2.0, 0, 0, 0));
            StackPane.setMargin(contentPane, new Insets((double) newValue / 2.0, 0, 0, 0));
        });
        textPane.getChildren().add(titleLabel);
        contentPane.setCenter(content);
        borderPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));
//        _borderPane.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        contentPane.setPadding(new Insets(15, 15, 12, 15));
        getChildren().add(borderPane);
        getChildren().add(contentPane);
        getChildren().add(textPane);
    }

}
package iflores.vamos.installer;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;

public class Wizard<T extends WizardState> extends BorderPane {

    private final Button _backButton;
    private final Button _nextButton;
    private final Button _cancelButton;
    private WizardStep<T> _currentStep;
    private final LinkedList<WizardStep<T>> _stepStack = new LinkedList<>();
    private final ValidityChangeListener _validListener;
    private final T _state;

    public Wizard(T state, Function<Wizard<T>, WizardStep<T>> initialStep) {
        _state = state;
        _backButton = makeButton("< Back", false);
        _nextButton = makeButton("", true);
        _cancelButton = makeButton("Cancel", false);
        _cancelButton.setOnAction(e -> System.exit(0));
        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12, 12, 12, 12));
        buttonBar.getChildren().addAll(
                _backButton,
                _nextButton,
                makeHorizontalSpacer(11),
                _cancelButton
        );
        BorderPane buttonBar2 = new BorderPane();
        buttonBar2.setCenter(buttonBar);
        buttonBar2.setTop(new Separator(Orientation.HORIZONTAL));
        setBottom(buttonBar2);
        _validListener = value -> _nextButton.setDisable(! value);
        _nextButton.setOnAction(e -> next(_currentStep));
        _backButton.setOnAction(e -> back());
        push(initialStep.apply(this));
    }

    private Node makeHorizontalSpacer(int width) {
        Pane pane = new Pane();
        pane.setPrefWidth(width);
        return pane;
    }

    private Button makeButton(String text, boolean blueBorder) {
        Button button = new Button(text);
        button.setFont(Font.font("SansSerif", FontWeight.NORMAL, 11.0));
        int buttonWidth = 40; // should be an even number
        if (blueBorder) {
            button.disabledProperty().addListener((observable, oldValue, newValue) -> {
                setBorder(! newValue, button, buttonWidth);
            });
        }
        setBorder(blueBorder, button, buttonWidth);
        button.setFocusTraversable(false);
        return button;
    }

    private void setBorder(boolean border, Button button, int buttonWidth) {
        button.setPadding(new Insets(2, buttonWidth / 2.0, 2, buttonWidth / 2.0));
        if (border) {
            button.setPadding(new Insets(2, buttonWidth / 2.0, 2, buttonWidth / 2.0));
            button.setBorder(new Border(new BorderStroke(new Color(0.0, 0.471, 0.843, 1.0), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2.0))));
        }
        else {
            button.setPadding(new Insets(3, buttonWidth / 2.0 + 1, 3, buttonWidth / 2.0 + 1));
            button.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1.0))));
        }
    }

    public void push(@NotNull WizardStep<T> step) {
        Objects.requireNonNull(step);
        if (_currentStep != null) {
            _currentStep.removeValidListener(_validListener);
            _stepStack.push(_currentStep);
        }
        _currentStep = step;
        setCenter(_currentStep);
        _currentStep.addValidListener(_validListener);
        _backButton.setVisible(!_stepStack.isEmpty());
        _validListener.validityChanged(_currentStep.isNextButtonEnabled());
        _currentStep.maybeInit();
        _nextButton.setText(_currentStep.hasNextStep() ? "Next >" : "Finish");
        _backButton.setDisable(! _currentStep.isBackButtonEnabled());
        _cancelButton.setDisable(! _currentStep.isCancelButtonEnabled());
    }

    public void back() {
        if (_stepStack.isEmpty()) {
            return;
        }
        if (_currentStep != null) {
            _currentStep.removeValidListener(_validListener);
        }
        _currentStep = _stepStack.pop();
        setCenter(_currentStep);
        _currentStep.addValidListener(_validListener);
        _backButton.setVisible(!_stepStack.isEmpty());
        _validListener.validityChanged(_currentStep.isNextButtonEnabled());
        _currentStep.maybeInit();
    }

    public void next(WizardStep<T> expectedCurrentStep) {
        if (! _nextButton.isDisabled() && (expectedCurrentStep == null || _currentStep == expectedCurrentStep)) {
            if (_currentStep.hasNextStep()) {
                push(_currentStep.getNextStep());
            } else {
                System.exit(0);
            }
        }
    }

    public T getState() {
        return _state;
    }
}

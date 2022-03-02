package iflores.vamos.installer;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public abstract class WizardStep<T extends WizardState> extends BorderPane {

    private boolean _nextButtonEnabled;
    private final Set<ValidityChangeListener> _listeners = new HashSet<>();
    private boolean _initialized;
    private final Wizard<T> _wizard;

    public WizardStep(Wizard<T> wizard) {
        _wizard = wizard;
    }

    public abstract boolean hasNextStep();

    public abstract @NotNull WizardStep<T> getNextStep();

    public final boolean isNextButtonEnabled() {
        return _nextButtonEnabled;
    }

    protected final void setNextButtonEnabled(boolean nextButtonEnabled) {
        _nextButtonEnabled = nextButtonEnabled;
        for (ValidityChangeListener listener : _listeners) {
            listener.validityChanged(nextButtonEnabled);
        }
    }

    public final void addValidListener(ValidityChangeListener listener) {
        _listeners.add(listener);
    }

    public final void removeValidListener(ValidityChangeListener listener) {
        _listeners.remove(listener);
    }

    public abstract void init();

    public final void maybeInit() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException();
        }
        if (_initialized) {
            return;
        }
        _initialized = true;
        init();
    }

    public final Wizard<T> getWizard() {
        return _wizard;
    }

    public final void next() {
        _wizard.next(this);
    }

    public boolean isBackButtonEnabled() {
        return true;
    }

    public boolean isCancelButtonEnabled() {
        return true;
    }

    public final T getState() {
        return getWizard().getState();
    }
}

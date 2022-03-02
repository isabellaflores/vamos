package iflores.vamos.installer;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.nio.file.Path;

public class VamosWizardState implements WizardState {

    private Path _destinationFolder;
    private boolean _createDesktopShortcut = true;
    private final ReadOnlyObjectWrapper<SignedMetadataCollection> _signedMetadataCollection = new ReadOnlyObjectWrapper<>();
    private boolean _useTorSocks;

    public void setDestinationFolder(Path destinationFolder) {
        _destinationFolder = destinationFolder;
    }

    public Path getDestinationFolder() {
        return _destinationFolder;
    }

    public void setCreateDesktopShortcut(boolean createDesktopShortcut) {
        _createDesktopShortcut = createDesktopShortcut;
    }

    public boolean getCreateDesktopShortcut() {
        return _createDesktopShortcut;
    }

    public void setSignedMetadataCollection(SignedMetadataCollection signedMetadataCollection) {
        _signedMetadataCollection.set(signedMetadataCollection);
    }

    public SignedMetadataCollection getSignedMetadataCollection() {
        return _signedMetadataCollection.get();
    }

    public ReadOnlyObjectProperty<SignedMetadataCollection> signedMetadataCollectionProperty() {
        return _signedMetadataCollection.getReadOnlyProperty();
    }

    public void setUseTorSocks(boolean useTorSocks) {
        _useTorSocks = useTorSocks;
    }

    public boolean getUseTorSocks() {
        return _useTorSocks;
    }

}

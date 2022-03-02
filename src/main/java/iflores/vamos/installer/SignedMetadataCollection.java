package iflores.vamos.installer;

import java.util.HashMap;
import java.util.Map;

public class SignedMetadataCollection {

    private final Map<FileType, SignedMetadata> _metadata = new HashMap<>();

    public void add(FileType fileType, SignedMetadata bytes) {
        _metadata.put(fileType, bytes);
    }

    public SignedMetadata getMetadata(FileType fileType) {
        return _metadata.get(fileType);
    }

}

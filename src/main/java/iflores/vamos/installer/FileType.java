package iflores.vamos.installer;

public enum FileType {
    JSON(".json"),
    PKG(".pkg");

    private final String _extension;

    FileType(String extension) {
        _extension = extension;
    }

    public String getExtension() {
        return _extension;
    }

}

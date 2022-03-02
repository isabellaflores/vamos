package iflores.vamos.installer;

public class SignedMetadata {

    private final byte[] _bytes;

    public SignedMetadata(byte[] bytes) {
        _bytes = bytes;
    }

    public byte[] getBytes() {
        return _bytes;
    }

}

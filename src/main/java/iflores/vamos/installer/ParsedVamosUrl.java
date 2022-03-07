package iflores.vamos.installer;

import org.bouncycastle.pqc.jcajce.provider.sphincs.BCSphincs256PublicKey;

public class ParsedVamosUrl {

    private final BCSphincs256PublicKey _publicKey;
    private final String _metadataUrl;

    public ParsedVamosUrl(BCSphincs256PublicKey publicKey, String metadataUrl) {
        if (! metadataUrl.endsWith(".json")) {
            throw new IllegalStateException();
        }
        _publicKey = publicKey;
        _metadataUrl = metadataUrl;
    }

    public BCSphincs256PublicKey getPublicKey() {
        return _publicKey;
    }

    public String getMetadataUrl() {
        return _metadataUrl;
    }

    public String getMetadataUrl(FileType fileType) {
        String metadataUrlPrefix = _metadataUrl.substring(0, _metadataUrl.length() - 5); // remove .json suffix
        return metadataUrlPrefix + fileType.getExtension();
    }

}

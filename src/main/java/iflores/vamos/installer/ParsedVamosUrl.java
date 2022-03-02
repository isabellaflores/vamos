package iflores.vamos.installer;

import org.bouncycastle.pqc.jcajce.provider.sphincs.BCSphincs256PublicKey;

public class ParsedVamosUrl {

    private final BCSphincs256PublicKey _publicKey;
    private final String _metadataUrl;

    public ParsedVamosUrl(BCSphincs256PublicKey publicKey, String metadataUrl) {
        _publicKey = publicKey;
        _metadataUrl = metadataUrl;
    }

    public BCSphincs256PublicKey getPublicKey() {
        return _publicKey;
    }

    public String getMetadataUrlPrefix() {
        return _metadataUrl;
    }

}

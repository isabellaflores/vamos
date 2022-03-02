package iflores.vamos.installer;

import org.bouncycastle.pqc.jcajce.provider.sphincs.SignatureSpi;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

public class MySignatureAlgorithm extends SignatureSpi.withSha512 {

    public void initVerify(PublicKey publicKey) throws InvalidKeyException {
        engineInitVerify(publicKey);
    }

    public void update(byte[] b, int off, int len) throws SignatureException {
        engineUpdate(b, off, len);
    }

    public void update(byte[] b) throws SignatureException {
        engineUpdate(b, 0, b.length);
    }

    public boolean verify(byte[] sigBytes) throws SignatureException {
        return engineVerify(sigBytes);
    }

}

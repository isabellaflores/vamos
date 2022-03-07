package iflores.vamos.installer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import org.bouncycastle.pqc.jcajce.provider.sphincs.BCSphincs256PublicKey;
import org.bouncycastle.pqc.jcajce.spec.SPHINCS256KeyGenParameterSpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Utils {

    public static final String TREE_HASH_ALGORITHM = SPHINCS256KeyGenParameterSpec.SHA512_256;
    public static final ASN1ObjectIdentifier TREE_HASH_ALGORITHM_ASN_1 = NISTObjectIdentifiers.id_sha512_256;
    private static final Gson GSON = new Gson();

    public static void runWithTempFile(String prefix, String suffix, TempFileCallback callback) {
        try {
            Path path = Files.createTempFile(prefix, suffix);
            try {
                callback.accept(path);
            } finally {
                Files.delete(path);
            }
        } catch (Throwable t) {
            throw throwException(t);
        }
    }

    public static <E extends Throwable> RuntimeException throwException(Throwable t) throws E {
        //noinspection unchecked
        throw (E) t;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BCSphincs256PublicKey makePublicKeyFromKeyData(byte[] publicKeyData) {
        return new BCSphincs256PublicKey(
                TREE_HASH_ALGORITHM_ASN_1,
                new SPHINCSPublicKeyParameters(
                        publicKeyData,
                        TREE_HASH_ALGORITHM
                )
        );
    }

    public static SignedMetadataCollection loadMetadata(Proxy proxy, BigInteger currentVersion, ParsedVamosUrl parsedVamosUrl, BiConsumer<FileType, Boolean> fileTypeConsumer, BiConsumer<Integer, Integer> progressConsumer, FileType... fileTypes) {
        if (fileTypeConsumer == null) {
            fileTypeConsumer = (fileType, signature) -> {
            };
        }
        if (progressConsumer == null) {
            progressConsumer = (current, max) -> {
            };
        }
        try {
            SignedMetadataCollection signedMetadataCollection = new SignedMetadataCollection();
            SignedMetadata jsonMetadata = fetchRemoteMetadata(proxy, parsedVamosUrl, FileType.JSON, currentVersion, null, fileTypeConsumer, progressConsumer);
            if (jsonMetadata == null) {
                return null;
            }
            signedMetadataCollection.add(FileType.JSON, jsonMetadata);
            byte[] jsonHash = getSha1Hash(jsonMetadata.getBytes());
            for (FileType fileType : fileTypes) {
                SignedMetadata metadata = fetchRemoteMetadata(proxy, parsedVamosUrl, fileType, null, jsonHash, fileTypeConsumer, progressConsumer);
                if (metadata == null) {
                    return null;
                }
                signedMetadataCollection.add(fileType, metadata);
            }
            return signedMetadataCollection;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static SignedMetadata fetchRemoteMetadata(Proxy proxy, ParsedVamosUrl parsedVamosUrl, FileType fileType, BigInteger currentVersion, byte[] jsonHash, BiConsumer<FileType, Boolean> fileTypeConsumer, BiConsumer<Integer, Integer> progressConsumer) throws Throwable {
        fileTypeConsumer.accept(fileType, false);
        URL metadataUrl = new URL(parsedVamosUrl.getMetadataUrl(fileType));
        byte[] bytes = getUrlContent(proxy, metadataUrl, progressConsumer);
        if (fileType == FileType.JSON) {
            if (currentVersion != null) {
                JsonObject metadataJson = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), JsonObject.class);
                BigInteger version = metadataJson.get("version").getAsBigInteger();
                if (version.compareTo(currentVersion) <= 0) {
                    return null;
                }
            }
        }
        if (jsonHash == null) {
            if (fileType != FileType.JSON) {
                throw new IllegalStateException();
            }
            jsonHash = getSha1Hash(bytes);
        }
        // check signature
        fileTypeConsumer.accept(fileType, true);
        URL signatureUrl = new URL(parsedVamosUrl.getMetadataUrl(fileType) + ".signature");
        byte[] signatureBytes = getUrlContent(proxy, signatureUrl, progressConsumer);
        boolean signatureOk = verifySignature(parsedVamosUrl.getPublicKey(), new ByteArrayInputStream(bytes), fileType, jsonHash, signatureBytes);
        if (!signatureOk) {
            throw new RuntimeException(fileType + " has bad signature");
        }
        fileTypeConsumer.accept(null, false);
        return new SignedMetadata(bytes);
    }

    public static byte[] getUrlContent(Proxy proxy, URL url, BiConsumer<Integer, Integer> progressConsumer) throws Throwable {
        URLConnection conn = url.openConnection(proxy);
        int contentLength = conn.getContentLength();
        if (contentLength == 0) {
            return new byte[0];
        }
        try (InputStream is = conn.getInputStream()) {
            return readAllBytes(is, progress -> progressConsumer.accept(progress, contentLength));
        }
    }

    public static byte[] readAllBytes(InputStream is, Consumer<Integer> progressConsumer) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[16384];
        progressConsumer.accept(0);
        while (true) {
            int count = is.read(buf);
            if (count < 0) {
                break;
            }
            baos.write(buf, 0, count);
            progressConsumer.accept(baos.size());
        }
        return baos.toByteArray();
    }

    public static boolean verifySignature(BCSphincs256PublicKey publicKey, InputStream input, FileType fileType, byte[] jsonFileHash, byte[] signature) throws IOException, SignatureException, InvalidKeyException {
        MySignatureAlgorithm signatureAlgorithm = new MySignatureAlgorithm();
        signatureAlgorithm.initVerify(publicKey);
        signatureAlgorithm.update(jsonFileHash);
        signatureAlgorithm.update((fileType.name() + "\n").getBytes(StandardCharsets.UTF_8));
        final byte[] buf = new byte[16384];
        while (true) {
            int count = input.read(buf);
            if (count < 0) {
                break;
            }
            signatureAlgorithm.update(buf, 0, count);
        }
        return signatureAlgorithm.verify(signature);
    }

    public static byte[] getSha1Hash(byte[] jsonBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(jsonBytes);
        return md.digest();
    }

    public interface TempFileCallback {
        void accept(Path path) throws Throwable;
    }

}

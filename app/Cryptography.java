package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Cryptography {

    private static final String AES_ALGORITHM = "AES";
    private static final String RSA_SIGNATURE_ALGORITHM = "SHA512withRSA";
    private static final int AES_KEY_SIZE = 128;
    private static final int MIN_ENTROPY_BYTE_DIFFERENCE = 8;

    /**
     * Encrypts the given password using AES encryption with the provided SecretKey.
     * Rule: CWE-261: Weak Encoding for Password
     * @param password The password to encrypt.
     * @param secKey The AES SecretKey to use for encryption.
     * @return The encrypted password as a Base64 string, or null on error.
     */
    public String encryptPassword(String password, SecretKey secKey) {
        if (password == null || secKey == null) {
            System.err.println("Password or key is null");
            return null;
        }

        if (!validateKey(secKey) || !checkEntropy(secKey)) {
            System.err.println("key not valid or entropy not sufficent");
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secKey);
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return encode(encrypted);
        } 
        catch (NoSuchAlgorithmException e) {
            PokerLogger.logError("No Such Algorithm Exception: " , e);
            System.err.println("No Such Algorithm Exception: " + e.getMessage());
            return null;
        }
        catch (NoSuchPaddingException e) {
            PokerLogger.logError("No Such Padding Exception: " , e);
            System.err.println("No Such Padding Exception: " + e.getMessage());
            return null;
        }
        catch (InvalidKeyException e) {
            PokerLogger.logError("Invalid Key Exception: " , e);
            System.err.println("Invalid Key Exception: " + e.getMessage());
            return null;
        }
        catch (IllegalBlockSizeException e) {
            PokerLogger.logError("Illegal Block Size Exception: " , e);
            System.err.println("Illegal Block Size Exception: " + e.getMessage());
            return null;
        }
        catch (BadPaddingException e) {
            PokerLogger.logError("Bad Padding Exception: " , e);
            System.err.println("Bad Padding Exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decrypts an encrypted password using AES decryption.
     * @param encrypted The encrypted password as a Base64 string.
     * @param secKey The AES SecretKey to use for decryption.
     * @return The decrypted password as a String, or null on error.
     */
    public String decryptPassword(String encrypted, SecretKey secKey) {
        if (encrypted == null || secKey == null) {
            System.err.println("Password or key is null");
            return null;
        }

        if (!validateKey(secKey)) {
            System.err.println("key not valid");
            return null;
        }

        try {
            byte[] encryptedBytes = decode(encrypted);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secKey);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } 
        catch (NoSuchAlgorithmException e) {
            PokerLogger.logError("No Such Algorithm Exception: " , e);
            System.err.println("No Such Algorithm Exception: " + e.getMessage());
            return null;
        }
        catch (NoSuchPaddingException e) {
            PokerLogger.logError("No Such Padding Exception: " , e);
            System.err.println("No Such Padding Exception: " + e.getMessage());
            return null;
        }
        catch (InvalidKeyException e) {
            PokerLogger.logError("Invalid Key Exception: " , e);
            System.err.println("Invalid Key Exception: " + e.getMessage());
            return null;
        }
        catch (IllegalBlockSizeException e) {
            PokerLogger.logError("Illegal Block Size Exception: " , e);
            System.err.println("Illegal Block Size Exception: " + e.getMessage());
            return null;
        }
        catch (BadPaddingException e) {
            PokerLogger.logError("Bad Padding Exception: " , e);
            System.err.println("Bad Padding Exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Encodes the given byte array to a Base64 string.
     * @param data The byte array to encode.
     * @return The encoded Base64 string.
     */
    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes the given Base64 string to its original byte array.
     * @param data The Base64 encoded string.
     * @return The decoded byte array.
     */
    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    /**
     * Validates the encryption key.
     * Rule: CWE-324: Use of a Key Past its Expiration Date
     * This implementation does not track key creation metadata, so it validates
     * the key structure and returns true for a usable AES key.
     * @param secKey The SecretKey to validate.
     * @return true if the key is structurally valid, false otherwise.
     */
    private boolean validateKey(SecretKey secKey) {
        if (secKey == null) {
            return false;
        }

        byte[] keyBytes = secKey.getEncoded();
        return keyBytes != null && keyBytes.length >= AES_KEY_SIZE / 8;
    }

    /**
     * Hashes a file using SHA-512.
     * Rule: CWE-328: Use of Weak Hash
     * @param file The file to hash.
     * @return The SHA-512 hash as a hex string, or null on error.
     */
    private String hashFile(File file) {
        if (file == null || !file.exists()) {
            System.err.println("File is null or does not exist");
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return bytesToHex(digest.digest());
        } 
        catch (NoSuchAlgorithmException e) {
            PokerLogger.logError("No Such Algorithm Exception: " , e);
             System.err.println("No Such Algorithm Exception: " + e.getMessage());
            return null;
        }
        catch (IOException e) {
            PokerLogger.logError("IO Exception: " , e);
            System.err.println("IO Exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifies the hash of a file using SHA-512.
     * Rule: CWE-328: Use of Weak Hash
     * @param file The file to verify.
     * @param expectedHash The expected SHA-512 hash as a hex string.
     * @return true if the file's hash matches the expected hash, false otherwise.
     */
    private boolean verifyHash(File file, String expectedHash) {
        if (expectedHash == null) {
            return false;
        }
        String actualHash = hashFile(file);
        return actualHash != null && actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Checks if the encryption key has sufficient entropy.
     * Rule: CWE-331: Insufficient Entropy
     * @param secKey The SecretKey to check.
     * @return true if entropy is sufficient, false otherwise.
     */
    private boolean checkEntropy(SecretKey secKey) {
        byte[] keyBytes = secKey == null ? null : secKey.getEncoded();
        if (keyBytes == null || keyBytes.length < AES_KEY_SIZE / 8) {
            return false;
        }

        Set<Byte> uniqueBytes = new HashSet<>();
        for (byte b : keyBytes) {
            uniqueBytes.add(b);
        }
        return uniqueBytes.size() > MIN_ENTROPY_BYTE_DIFFERENCE;
    }

    /**
     * Signs the hashed file using RSA.
     * Rule: CWE-347: Improper Verification of Cryptographic Signature
     * Should be used after hashing.
     * @param hash The hash to sign (as a byte array).
     * @param privateKey The RSA private key.
     * @return The signature as a byte array, or null on error.
     */
    private byte[] sign(byte[] hash, PrivateKey privateKey) {
        if (hash == null || privateKey == null) {
            return null;
        }

        try {
            Signature signature = Signature.getInstance(RSA_SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(hash);
            return signature.sign();
        } 
        catch (NoSuchAlgorithmException e) {
            PokerLogger.logError("No Such Algorithm Exception: " , e);
            System.err.println("No Such Algorithm Exception: " + e.getMessage());
            return null;
        }
        catch (SignatureException e) {
            PokerLogger.logError("Signature Exception: " , e);
            System.err.println("Signature Exception: " + e.getMessage());
            return null;
        }
        catch (InvalidKeyException e) {
            PokerLogger.logError("Invalid Key Exception: " , e);
            System.err.println("Invalid Key Exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifies the cryptographic signature after hash verification.
     * Rule: CWE-347: Improper Verification of Cryptographic Signature
     * @param hash The hash that was signed (as a byte array).
     * @param signatureBytes The signature to verify.
     * @param publicKey The RSA public key.
     * @return true if the signature is valid, false otherwise.
     */
    private boolean verifySignature(byte[] hash, byte[] signatureBytes, PublicKey publicKey) {
        if (hash == null || signatureBytes == null || publicKey == null) {
            return false;
        }

        try {
            Signature signature = Signature.getInstance(RSA_SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(hash);
            return signature.verify(signatureBytes);
        } 
        catch (NoSuchAlgorithmException e) {
            PokerLogger.logError("No Such Algorithm Exception: " , e);
            System.err.println("No Such Algorithm Exception: " + e.getMessage());
            return false;
        }
        catch (SignatureException e) {
            PokerLogger.logError("Signature Exception: " , e);
            System.err.println("Signature Exception: " + e.getMessage());
            return false;
        }
        catch (InvalidKeyException e) {
            PokerLogger.logError("Invalid Key Exception: " , e);
            System.err.println("Invalid Key Exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Protects a file by hashing and then signing it.
     * Calls hashFile() and then sign().
     * @param file The file to protect.
     * @param privateKey The RSA private key for signing.
     * @return The signature as a byte array, or null on error.
     */
    public byte[] protectFile(File file, PrivateKey privateKey) {
        String hashHex = hashFile(file);
        if (hashHex == null) {
            return null;
        }
        byte[] hashBytes = hashHex.getBytes(StandardCharsets.UTF_8);
        return sign(hashBytes, privateKey);
    }

    /**
     * Verifies a file by checking its hash and then verifying its signature.
     * Calls verifyHash() and then verifySignature().
     * @param file The file to verify.
     * @param expectedHash The expected SHA-512 hash as a hex string.
     * @param signatureBytes The RSA signature to verify.
     * @param publicKey The RSA public key.
     * @return true if both hash and signature are valid, false otherwise.
     */
    public boolean verifyFile(File file, String expectedHash, byte[] signatureBytes, PublicKey publicKey) {
        if (!verifyHash(file, expectedHash)) {
            return false;
        }
        byte[] hashBytes = expectedHash.getBytes(StandardCharsets.UTF_8);
        return verifySignature(hashBytes, signatureBytes, publicKey);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

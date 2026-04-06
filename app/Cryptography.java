package app;
//encrypt password, hash file
public class Cryptography {
    public void encryptPassword()
    {
        /*
        Rule: CWE-261: Weak Encoding for Password
            encrypt a password with AES
        */
    }

    public void decryptPassword()
    {
    }

    private void validateKey()
    {
        /*
        CWE-324: Use of a Key Past its Expiration Date
            compare key creation date to current date
            if  the difference is longer the valid period return false else return true
            use this in encryption (before enctyption)
        */
    }

    private void hashFile()
    {
        /*
        CWE-328: Use of Weak Hash
            Hash a file with SHA-512
        */
    }

    private void verifyHash()
    {
        /*
        CWE-328: Use of Weak Hash
            Hash a file with SHA-512
        */
    }

    private void checkEntropy()
    {
        /*
        CWE-331: Insufficient Entropy
            check to see if the encryption key has sufficent entropy
        */
    }

    private void sign()
    {
        /*
        CWE-347: Improper Verification of Cryptographic Signature
            sign using RSA
            use after hashing
        */
    }

    private  void verifySignature()
    {
        /*
        CWE-347: Improper Verification of Cryptographic Signature
            use after hash verificaiton
        */
    }

    public  void protectFile()
    {
        /*
        call hash adn then sign
        */
    }

    public  void verifyFile()
    {
        /*
        call verify hash and then verify signature
        */
    }
}

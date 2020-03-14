package com.example.beepbeep;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
 Title: Java Secure Hashing – MD5, SHA256, SHA512, PBKDF2, BCrypt, SCrypt
 Author: Yuqian Cao, Lokesh Gupta | Java Security
 Date: 2020/03/02
 Code version: N/A
 Availability: https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/

 Title: Convert a string representation of a hex dump to a byte array using Java?
 Author: Yuqian Cao, Dave L.
 Date: 2020/03/02
 Code version: N/A
 Availability: https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
*/

/**
 * A generator that will generate password hash using PBKDF2WithHmacSHA1 algorithm
 */
class SecurePasswordHashGenerator {
    /**
     * Will take in a password in plain text and hash it using PBKDF2WithHmacSHA1 algorithm
     * Used to generate a new password hash
     * @param plaintextPassword password as a string
     * @return salt and hash separated by :
     * @throws NoSuchAlgorithmException PBKDF2WithHmacSHA1 algorithm is unavailable
     * @throws InvalidKeySpecException KeySpec not supported
     */
    static String generateNewStrongPasswordHash(String plaintextPassword) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 5000;
        char[] chars = plaintextPassword.toCharArray();
        byte[] salt = getSalt();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Will take in plain text password and salt to generate a hash
     * @param plaintextPassword password as a string
     * @param saltString hex string of the salt
     * @return salt and hash separated by :
     * @throws NoSuchAlgorithmException PBKDF2WithHmacSHA1 algorithm is unavailable
     * @throws InvalidKeySpecException KeySpec not supported
     */
    static String rehashPassword(String plaintextPassword, String saltString) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 5000;
        char[] chars = plaintextPassword.toCharArray();
        byte[] salt = hexStringToByteArray(saltString);

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Converts a string of hex into a byte array
     * @param hexString string of hex numeber
     * @return converted byte arrau
     */
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Create a hash salt that will increase the security of the hash
     * @return salt
     * @throws NoSuchAlgorithmException PBKDF2WithHmacSHA1 algorithm is unavailable
     */
    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    /**
     * Convert byte array hash into human readable hex
     * @param array hash byte array
     * @return readableHash
     */
    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }
}

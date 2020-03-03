package com.example.beepbeep;

import android.util.Log;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;

public class SecurePasswordHashGeneratorTest {
    @Test
    public void testGenerator(){
        try{
            String hash = SecurePasswordHashGenerator.generateNewStrongPasswordHash("abc");
            String hash2 = SecurePasswordHashGenerator.rehashPassword("abc", hash.substring(0, 32));
            assertEquals(hash, hash2);

        }catch (NoSuchAlgorithmException nsa){
            Log.d("testGeneratorfailed", "NoSuchAlgorithmException");
            fail();
        }catch (InvalidKeySpecException iks){
            Log.d("testGeneratorfailed", "InvalidKeySpecException");
            fail();
        }

    }

}

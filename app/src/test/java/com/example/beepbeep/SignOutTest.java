package com.example.beepbeep;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;

public class SignOutTest {
    @Test
    public void testNow(){
        Context context = mock(Context.class);
        try{
            SignOut.now(context);
        }catch (NullPointerException ignored){

        }
    }
}

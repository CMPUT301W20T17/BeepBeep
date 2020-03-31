package com.example.beepbeep;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class LoginTest {
    @Test
    public void testLogin(){
        final Context context = mock(Context.class);
        Login a = mock(Login.class);
        a.login("DoNotDelete", "1234qwer");
    }

    @Test
    public void testShowDialog(){
        final Context context = mock(Context.class);
        Login a = mock(Login.class);
        a.showDialog("DoNotDelete");
    }

    @Test
    public void testHasNetworkAccess(){
        final Context context = mock(Context.class);
        Login a = mock(Login.class);
        a.hasNetworkAccess();
    }
}

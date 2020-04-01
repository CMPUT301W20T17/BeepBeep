package com.example.beepbeep;

import android.content.Context;

import org.junit.Test;

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

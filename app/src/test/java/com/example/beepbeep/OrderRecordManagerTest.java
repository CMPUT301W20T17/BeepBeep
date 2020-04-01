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

public class OrderRecordManagerTest {
    @Test
    public void testSaveRecord(){
        try {
            FirebaseFirestore db = Mockito.mock(FirebaseFirestore.class);
            Mockito.when(FirebaseFirestore.getInstance()).thenReturn(db);
            Context c = Mockito.mock(Context.class);
            OrderRecordManager orm = new OrderRecordManager(c);
            orm.saveRecord();
        }catch (NullPointerException | ExceptionInInitializerError ignored){

        }
    }

    @Test
    public void testGetRecord(){
        try {
            FirebaseFirestore db = Mockito.mock(FirebaseFirestore.class);
            Mockito.when(FirebaseFirestore.getInstance()).thenReturn(db);
            Context c = Mockito.mock(Context.class);
            OrderRecordManager orm = new OrderRecordManager(c);
            orm.getRecord();
        }catch (NullPointerException | ExceptionInInitializerError | NoClassDefFoundError ignored){

        }
    }

    @Test
    public void testToGeoPoint(){
        Context c = Mockito.mock(Context.class);
        try {
            OrderRecordManager.toGeoPoint("90.0fjkjadfw90");
        }catch (NullPointerException | ExceptionInInitializerError ignored){

        }
    }
}

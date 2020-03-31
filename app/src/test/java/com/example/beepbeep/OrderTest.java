package com.example.beepbeep;

import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class OrderTest {
    @Test
    public void testOrder(){
        String user = "user";
        String DriverID = "DriverID";
        String RiderID = "RiderID";
        String StartTime = "StartTime";
        String FinishTime = "FinishTime";
        double Price = 90;
        GeoPoint PickUpPoint = toGeoPoint("GeoPoint { latitude=53.441197300000006, longitude=-113.54309629999999 }");
        GeoPoint Destination = toGeoPoint("GeoPoint { latitude=53.441197300000006, longitude=-113.54309629999999 }");
        String Type = "Type";

        Order o = new Order(user, DriverID, RiderID, StartTime, FinishTime, Price, PickUpPoint, Destination, Type);

        assertEquals(user, o.getUser());
        assertEquals(DriverID, o.getDriverID());
        assertEquals(RiderID, o.getRiderID());
        assertEquals(StartTime, o.getStartTime());
        assertEquals(FinishTime, o.getFinishTime());
        assertEquals(PickUpPoint, o.getPickupPoint());
        assertEquals(Destination, o.getDestination());
        assertEquals(Type, o.getType());

        o.setUser("a");
        assertEquals("a", o.getUser());
    }

    private static GeoPoint toGeoPoint(String s){
        String pattern = "([-]?\\d*\\.\\d*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);

        double lat = 0;
        double lon = 0;
        if (m.find()) {
            lat = Double.parseDouble(m.group());
        }
        if (m.find()){
            lon = Double.parseDouble(m.group());
        }
        GeoPoint p = new GeoPoint(lat, lon);
        return p;
    }
}

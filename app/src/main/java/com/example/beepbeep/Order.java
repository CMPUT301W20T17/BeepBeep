package com.example.beepbeep;

import com.google.firebase.firestore.GeoPoint;
import java.io.Serializable;

/*
 Title: Order class
 Author: Junqi Zou, Lyuyang Wang
 Date: 2020/03/07
 Last edited: 2020/03/12
 Availability: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection/53335711
*/

/**
 * order class is each order includes user,driver,rider,date,start location and destination
 */

public class Order implements Serializable {
    private String user;
    private String DriverID;
    private String RiderID;
    private String StartTime;
    private String FinishTime;
    private Double Price;
    private GeoPoint PickUpPoint;
    private GeoPoint Destination;
    private String Type;


    /**
     * create order class
     * @param user
     * @param DriverID
     * @param RiderID
     * @param StartTime
     * @param FinishTime
     * @param Price
     * @param PickupPoint
     * @param Destination
     * @param Type
     */
    public Order(String user, String DriverID, String RiderID, String StartTime, String FinishTime, Double Price, GeoPoint PickupPoint, GeoPoint Destination, String Type){
        this.user = user;
        this.DriverID = DriverID;
        this.RiderID = RiderID;
        this.StartTime = StartTime;
        this.FinishTime = FinishTime;
        this.Price = Price;
        this.PickUpPoint = PickupPoint;
        this.Destination = Destination;
        this.Type = Type;
    }
    
    public Order(){}
 
    public String getUser(){return user;}

    public String getDriverID(){return DriverID;}

    public String getRiderID(){return RiderID;}

    public String getStartTime(){return StartTime;}

    public String getFinishTime(){return FinishTime;}

    public Double getPrice(){return Price;}

    public GeoPoint getPickupPoint(){
        /*
        double LAT  = PickupPoint.getLatitude();
        double LONG = PickupPoint.getLongitude();
         */
        return PickUpPoint;
    }

    public GeoPoint getDestination(){
        //double LAT  = Destination.getLatitude();
        //double LONG = Destination.getLongitude();
        return Destination;}

    public String getType(){return Type;}

    public void setUser(String userName){this.user = userName;}

}

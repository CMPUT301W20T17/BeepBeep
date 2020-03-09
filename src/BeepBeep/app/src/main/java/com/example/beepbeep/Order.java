package com.example.beepbeep;

import java.io.Serializable;

/*
 Title: Order class
 Author: Junqi Zhou, Lyuyang Wang
 Date: 2020/03/07
 Code version: N/A
 Availability: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection/53335711
*/

/**
 * order class is each order includes user,driver,rider,date,start location and destination
 */

public class Order implements Serializable {
    private String user;
    private String driver;
    private String rider;
    private String date;
    private String startLocation;
    private String destination;

    public Order(){};

    /**
     * create order class
     * @param user
     * @param driver
     * @param rider
     * @param date
     * @param startLocation
     * @param destination
     */
    public Order(String user, String driver, String rider, String date, String startLocation, String destination){
        this.user = user;
        this.driver = driver;
        this.rider = rider;
        this.date = date;
        this.startLocation = startLocation;
        this.destination = destination;
    }

    public String getUser(){return user;}

    public String getDriver(){return driver;}

    public String getRider(){return rider;}

    public String getDate(){return date;}

    public String getStartLocation(){return startLocation;}

    public String getDestination(){return destination;}

    public void setUser(String user){this.user= user;}

    public void setDriver(String driver){this.driver = driver;}

    public void setRider(String rider){this.rider = rider;}

    public void setDate(String date){this.date = date;}

    public void setStartLocation(String startLocation){this.startLocation = startLocation;}

    public void setDestination(String destination){this.destination = destination;}
}
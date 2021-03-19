package com.example.foodwithfriends;

import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LocationRangeTest extends HomeNavFragment {
    @Test
    public void same_locations() {
        GeoPoint userLocation = new GeoPoint(40.7052632, -73.5441481);
        GeoPoint groupLocation = new GeoPoint(40.7052632, -73.5441481);
        int distance = getDistance(userLocation, groupLocation);
        assertEquals(distance,  1);
    }

    @Test
    public void location_inRange() {
        int maxRange = 20;
        GeoPoint userLocation = new GeoPoint(40.7052632, -73.5441481);
        GeoPoint groupLocation = new GeoPoint(40.691408, -73.5411528);
        int distance = getDistance(userLocation, groupLocation);
        assertEquals(distance <= maxRange, true);
    }

    @Test
    public void location_not_inRange() {
        int maxRange = 1;
        GeoPoint userLocation = new GeoPoint(40.7052632, -73.5441481);
        GeoPoint groupLocation = new GeoPoint(40.6732082, -73.5452249);
        int distance = getDistance(userLocation, groupLocation);
        assertEquals(distance <= maxRange, false);
    }


    @Test
    public void location_barely_inRange() {
        int maxRange = 20;
        GeoPoint userLocation = new GeoPoint(40.250304, -83.036980);
        GeoPoint groupLocation = new GeoPoint(39.998091681131854, -82.98469579036524);
        int distance = getDistance(userLocation, groupLocation);
        assertEquals(distance <= maxRange, true);
    }

    @Test
    public void location_is_17_inRange() {
        int maxRange = 20;
        GeoPoint userLocation = new GeoPoint(40.250304, -83.036980);
        GeoPoint groupLocation = new GeoPoint(39.998091681131854, -82.98469579036524);
        int distance = getDistance(userLocation, groupLocation);
        assertEquals(distance , 17);
    }



    //39.998091681131854, -82.98469579036524
}
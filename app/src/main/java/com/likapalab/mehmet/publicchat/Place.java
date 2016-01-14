package com.likapalab.mehmet.publicchat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mehmet on 23.10.2015.
 */
public class Place {
    private static List<Place> places = new ArrayList<Place>();

    private String name;
    private int distance;

    public Place(String name, int distance){
        this.name = name;
        this.distance = distance;
    }

    public static void clearPlaceList(){places.clear();}

    public static void addPlace(Place place){places.add(place);}

    public static List<Place> getPlaces() {
        return places;
    }

    public String getName() {
        return name;
    }

    public int getDistance() {
        return distance;
    }
}

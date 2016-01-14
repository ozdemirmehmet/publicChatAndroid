package com.likapalab.mehmet.publicchat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Mehmet on 23.10.2015.
 */
public class PlacesService {

    private String CLIENT_ID = "1O1EHDYZQUIYGQNV10H5YORHND3V4MMTDXHF0CPO4EW4LX4Z";
    private String CLIENT_SECRET = "LYZWLOMKWPIL0MJ3ARE1WDCP3I3FBDWNM4HHTWRZFG0HBU53";

    public ArrayList<Place> findPlaces(String earth, double latitude, double longitude){
        String urlString = makeURL(latitude,longitude);

        String name = "";
        int distance;

        try{
            String json = getJSON(urlString);

            Place place_earth = new Place(earth,0);
            Place.addPlace(place_earth);
            JSONObject jsonObject = new JSONObject(json);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray venues = response.getJSONArray("venues");
            String country = venues.getJSONObject(0).getJSONObject("location").getString("country");
            Place place_country = new Place(country,0);
            Place.addPlace(place_country);
            for(int i=0; i<venues.length(); i++){
                JSONObject arrayItems = venues.getJSONObject(i);
                JSONObject location = arrayItems.getJSONObject("location");
                name = arrayItems.getString("name").toString();
                distance = Integer.parseInt(location.getString("distance").toString());
                Place place_item = new Place(name,distance);
                Place.addPlace(place_item);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    protected String getJSON(String url) {
        return getUrlContents(url);
    }

    private String getUrlContents(String theURL){
        StringBuilder content = new StringBuilder();

        try{
            URL url = new URL(theURL);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()),8);
            String line;
            while((line = bufferedReader.readLine()) != null){
                content.append(line + "\n");
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return content.toString();
    }

    private String makeURL(double latitude, double longitude){
        String urlString = "https://api.foursquare.com/v2/venues/search?" +
                "client_id="+CLIENT_ID+"&client_secret="+CLIENT_SECRET
                +"&v=20130815%20&ll="+latitude+","+longitude;
        return urlString;
    }
}

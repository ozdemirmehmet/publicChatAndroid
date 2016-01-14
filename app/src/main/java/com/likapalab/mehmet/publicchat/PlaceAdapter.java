package com.likapalab.mehmet.publicchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mehmet on 23.10.2015.
 */
public class PlaceAdapter extends ArrayAdapter<Place>{

    public PlaceAdapter (Context context, int resource, List<Place> objects){
        super(context,resource,objects);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        if(v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.places_item_template,null);
        }

        Place place = getItem(position);

        if(place != null){
            TextView nametext = (TextView)v.findViewById(R.id.name_text);

            nametext.setText(""+place.getName());
        }

        return v;
    }
}

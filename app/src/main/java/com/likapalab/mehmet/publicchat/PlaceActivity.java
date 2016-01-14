package com.likapalab.mehmet.publicchat;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;

import android.Manifest;


/**
 * Created by Mehmet on 23.10.2015.
 */
public class PlaceActivity extends Activity implements View.OnClickListener {

    ActionBar actionBar;

    LinearLayout backlayout;
    PlaceAdapter placeAdapter;
    ListView placeList;
    TextView welcometext;
    Button backbutton, refreshbutton, backgroundbutton;
    private LocationManager locationManager;
    private Location myLocation;
    private ProgressDialog progressDialog;
    String myUsername, myLocationName, provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        myUsername = this.getIntent().getStringExtra("username");

        placeList = (ListView) findViewById(R.id.place_list);

        backlayout = (LinearLayout) findViewById(R.id.place_back_layout);
        backlayout.setOnClickListener(this);

        backgroundbutton = (Button) findViewById(R.id.background_button);
        backgroundbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    startProgressDialog();
                    currentLocation();
                } else {
                    goGpsDialog();
                }
            }
        });

        actionBar = getActionBar();
        actionBar.setCustomView(R.layout.place_actionbar_view);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionBarBackColor)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        welcometext = (TextView) actionBar.getCustomView().findViewById(R.id.welcome_text);
        welcometext.setText(getResources().getString(R.string.welcome) + " " + myUsername + "!");

        placeAdapter = new PlaceAdapter(getApplicationContext(), R.layout.places_item_template, Place.getPlaces());
        placeList.setAdapter(placeAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setSpeedRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        provider = locationManager.getBestProvider(criteria, true);

        backbutton = (Button) actionBar.getCustomView().findViewById(R.id.back_button);
        refreshbutton = (Button) actionBar.getCustomView().findViewById(R.id.refresh_button);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Back button clicking return MainActivity
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("username", myUsername);
                startActivity(i);
                PlaceActivity.this.finish();
            }
        });

        refreshbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    startProgressDialog();
                    if (ActivityCompat.checkSelfPermission(PlaceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(PlaceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        marshmallowPermissionControl();
                        return;
                    }
                    locationManager.requestLocationUpdates(provider, 0, 0, listener);
                } else {
                    goGpsDialog();
                }
            }
        });


        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !(Place.getPlaces().isEmpty())) {

            startProgressDialog();

            currentLocation();
        } else {
            goGpsDialog();
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void onBackPressed() {
        backbutton.callOnClick();
    }

    private void startProgressDialog() {
        progressDialog = new ProgressDialog(PlaceActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setCancelable(true);
        progressDialog.setMessage(getResources().getString(R.string.loading_places));
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v == backlayout) {
            backgroundbutton.callOnClick();
        }
    }

    private class GetPlaces extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            PlacesService service = new PlacesService();
            service.findPlaces(getResources().getString(R.string.place_world), myLocation.getLatitude(), myLocation.getLongitude());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            listPlaces();
        }
    }

    private void currentLocation() {

        if (!Place.getPlaces().isEmpty()) {
            //List place
            listPlaces();
        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                marshmallowPermissionControl();
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);

            if (location == null) {
                locationManager.requestLocationUpdates(provider, 0, 0, listener);
            } else {
                //get and list place
                myLocation = location;
                new GetPlaces().execute();
            }
        }
    }

    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (ActivityCompat.checkSelfPermission(PlaceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(PlaceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                marshmallowPermissionControl();
                return;
            }
            locationManager.removeUpdates(listener);
            if(progressDialog.isShowing()){
                Place.clearPlaceList();
            }
            currentLocation();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void listPlaces(){
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                placeAdapter.notifyDataSetChanged();
            }
        });
        placeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myLocationName = Place.getPlaces().get(position).getName();

                MainActivity.mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Selection")
                        .setAction("Place")
                        .setLabel(myLocationName)
                        .build()
                );

                Intent i = new Intent(getApplicationContext(),MessageActivity.class);
                i.putExtra("username",myUsername);
                i.putExtra("location",myLocationName);
                startActivity(i);
                PlaceActivity.this.finish();
            }
        });
    }

    private void goGpsDialog(){
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            final AlertDialog.Builder builder = new AlertDialog.Builder(PlaceActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder.setTitle(getResources().getString(R.string.disable_gps_title));
            builder.setMessage(getResources().getString(R.string.disable_gps_message));
            builder.setCancelable(true);
            builder.setPositiveButton(getResources().getString(R.string.disable_gps_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            //setNegativeButton
            builder.show();
        }
    }

    private void marshmallowPermissionControl(){
        new AlertDialog.Builder(PlaceActivity.this)
                .setMessage(R.string.marshmallow_permission_message)
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(PlaceActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Manifest.permission_group.LOCATION.hashCode());
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlaceActivity.this.finish();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

}

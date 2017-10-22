package com.example.majid.dako.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.majid.dako.ConnectivityReceiver;
import com.example.majid.dako.Model.Biker;
import com.example.majid.dako.Model.User;
import com.example.majid.dako.MyApplication;
import com.example.majid.dako.Parser.DirectionsJSONParser;
import com.example.majid.dako.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;

/**
 * Created by Majid on 5/20/2017.
 */


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener, ConnectivityReceiver.ConnectivityReceiverListener {


    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    //Key for Permission
    private static final int MAP_PERMISSION_KEY = 111;
    private static final int REQUEST_SELECT_PLACE = 911;
    public double bikerUserMaxDistance = 2;
    //// Must be Unique and Come from sever
    protected String ID = "2187340";
    //Address Object for get address later
    Address address = null;
    Address currentaddress = null;
    Address destinationaddress = null;
    boolean isInternetAvailabe = false;
    ///Polyline
    Polyline roadpolyline;
    ///map Marker
    MarkerOptions originMarkerOption, destinationMarkerOption, bikerMarkerOption;
    Marker bikerMarker, origiMarker, destinationMaerker;
    //Origin & destination Latitude Longitude
    LatLng origin, destination, biker;
    //Distance & Duration
    String rideDeatailsStr = " ";
    String destinationAddressStr = " ";
    //Layout
    LinearLayout linearLayout;
    TextView raidDetails, currentLocation, fareTextview;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference bikerRef = database.getReference("Biker");
    DatabaseReference userRef = database.getReference("Rider").child(ID);
    DatabaseReference onlineBikerRef = database.getReference("Online");
    DatabaseReference loggedInBikerRef = database.getReference("LoggedIn");
    DatabaseReference onRideBikerRef = database.getReference("OnRide");
    User user;
    DataSnapshot mDataSnapshot;
    LocationManager locationManager;
    List<Biker> bikerList;
    List<Biker> nearBikerList;
    ArrayList<String> onlineBikerKey, loggedInBikerKey, onRideBikerKey;
    //Google Map
    private GoogleMap mMap;
    //To store longitude and latitude from map
    private double longitude;
    private double latitude;
    //Buttons
    private Button buttonCurrent, requestForRide;
    //Google ApiClient
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplayout_activity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing googleapi client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //Initializing views and adding onclick listeners
        linearLayout = (LinearLayout) findViewById(R.id.ridedetailsLayout);
        raidDetails = (TextView) findViewById(R.id.destinationTimeTV);
        fareTextview = (TextView) findViewById(R.id.fareTv);

        requestForRide = (Button) findViewById(R.id.setDirectionBtn);
        currentLocation = (TextView) findViewById(R.id.currentPositionTV);
        requestForRide.setOnClickListener(this);

        //   setDirection.setClickable(false);


    }


    private boolean checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        return isConnected;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        isInternetAvailabe = isConnected;
    }


    public double distanceBtweenBikerAndRaider(LatLng bikerlatlng, LatLng raiderLatlong) {


        double latitude = bikerlatlng.latitude;
        double longitude = bikerlatlng.longitude;
        double distance = 0;
        Location crntLocation = new Location("crntlocation");
        crntLocation.setLatitude(raiderLatlong.latitude);
        crntLocation.setLongitude(raiderLatlong.longitude);

        Location newLocation = new Location("newlocation");
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);


//float distance = crntLocation.distanceTo(newLocation);  in meters
        distance = crntLocation.distanceTo(newLocation) / 1000; // in km


        return distance;
    }


    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();


    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();


    }

    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();
        //    Log.d("Detection", "getcurrentlocation");

        //Check Permission for then android 6.0+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MAP_PERMISSION_KEY);
            return;
        }
        //collect location from location service
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        //enable current position floating button
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            //  Log.d("Detection", "Loaction not null");


            ///User location check that check that user is inside or outside dhaka
            String address = getAddress(new LatLng(latitude, longitude));
            if (address.equalsIgnoreCase("DHAKA")) {
                // Draw map over map fragment
                drawMap();


                locationSearch();


            } else {
                Toast.makeText(this, "  This Service Only Available Inside Dhaka City ", Toast.LENGTH_LONG).show();
            }


        }


    }

    //Function to Draw the map
    private void drawMap() {

        //  Log.d("Detection", "Draw googl map");

        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);
        this.origin = latLng;
        //mMap.setMyLocationEnabled(true);
        //Adding  Origin marker to map
        originMarkerOption = new MarkerOptions()
                .position(latLng)
                .draggable(false) //setting position
                .title("Your Position")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.markerstart));
        origiMarker = mMap.addMarker(originMarkerOption);


        destinationMarkerOption = new MarkerOptions()
                .position(latLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.markerend));

        destinationMaerker = mMap.addMarker(destinationMarkerOption);
        destinationMaerker.setVisible(false);


        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Animating the camera
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));


        Geocoder gc = new Geocoder(this);
        try {
            List<Address> list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);

            currentaddress = list.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentLocation.setText("Current location :   " + currentaddress.getAddressLine(0) + " ");

    }


    //When MAp is ready. This method is called 1st after oncreate
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Log.d("Detection", "on map ready");
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);


        //Moving the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkLocationPermission();

            return;
        }


    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    //Permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MAP_PERMISSION_KEY) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission accepted ", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //   Log.d("Detection", "onconnected");

        //Set current Position after onMapReady
        getCurrentLocation();


    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, connectionResult.toString(), Toast.LENGTH_SHORT).show();
    }

    //Set destination on map long click operation
    @Override
    public void onMapLongClick(LatLng latLng) {

        if (checkConnection()) {


            // Get address From GeoCode Api and Geocode class
            String address = getAddress(latLng);
            // Log.e("Address", address);

            //check address that is inside or outside dhaka city
            if (address.equalsIgnoreCase("DHAKA")) {
                //  Log.e("Address ", address + " inside dhaka ");
                //Clearing all the markers
                this.destination = latLng;


                linearLayout.setVisibility(GONE);


                Geocoder gc = new Geocoder(this);
                try {
                    List<Address> list = gc.getFromLocation(destination.latitude, destination.longitude, 1);

                    destinationaddress = list.get(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                destinationAddressStr = ("Destination : " + destinationaddress.getAddressLine(0) + "    " + destinationaddress.getAddressLine(1));
                markerPosition(latLng, destinationAddressStr);

                if (roadpolyline != null) {
                    roadpolyline.remove();
                }
                drawDirectionPolyLine(this.origin, this.destination);

                //Moving the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                //Animating the camera
                mMap.moveCamera(CameraUpdateFactory.zoomTo(13));

                raidDetails.setText(rideDeatailsStr);
                linearLayout.setVisibility(View.VISIBLE);

            } else {

                //  Log.e("Address " , address + " inside dhaka ");
                Toast.makeText(this, "  This Service Only Available Inside Dhaka City ", Toast.LENGTH_LONG).show();

                if (roadpolyline != null) {
                    roadpolyline.remove();
                }

                //Moving the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                //Animating the camera
                mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                destination = null;
                linearLayout.setVisibility(View.GONE);


            }


        } else {
            Toast.makeText(this, " Please Turn On Your Internet Connection", Toast.LENGTH_SHORT).show();
        }


    }

    //Set Marker position
    public void markerPosition(LatLng latLng, String destinationAddressStr) {

        destinationMaerker.setPosition(latLng);
        destinationMaerker.setTitle(destinationAddressStr);
        destinationMaerker.setVisible(true);

    }


    // Get Address from CeoCoder and return locality from address class
    public String getAddress(LatLng latLng) {

        Geocoder gc = new Geocoder(this);
        try {
            List<Address> list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);

            address = list.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(address.getLocality());
    }


    @Override
    public void onClick(View v) {

        if (checkConnection()) {

            if (v == requestForRide) {
                if (origin != null && destination != null) {

                    refreshLocation();

                }

            }

        } else {
            Toast.makeText(this, " Please Turn On Your Internet Connection", Toast.LENGTH_SHORT).show();
        }


    }

    private void refreshLocation() {


        //Check Permission for then android 6.0+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MAP_PERMISSION_KEY);
            return;
        }
        //collect location from location service
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            this.origin = new LatLng(latitude, longitude);

        }


    }

    // Draw Poly line Over road
    public void drawDirectionPolyLine(LatLng origin, LatLng destination) {

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, destination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);

    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    //A method to download json data from url
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    //Auto Complete Search Location
    public void locationSearch() {

        Log.e("PlaceAutocomplete", "Start");
        //AutoComplete search Filter setup
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .setCountry("BD")
                .build();
        //initialization placeAutocomplete fragment widget
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_fragment);


        autocompleteFragment.setHint("Set Destination here ");
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setMenuVisibility(true);
        autocompleteFragment.setBoundsBias(null);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        Log.e("PlaceAutocomplete", "init");


    }

    @Override
    public void onPlaceSelected(Place place) {

        if (checkConnection()) {

            Log.e("PlaceAutocomplete", "placeSelected");

            String address = getAddress(place.getLatLng());
            if (address.equalsIgnoreCase("DHAKA")) {

                this.destination = place.getLatLng();


                //Moving the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                //Animating the camera
                mMap.moveCamera(CameraUpdateFactory.zoomTo(13));


                destinationAddressStr = ("Destination : " + place.getName() + "    " + place.getAddress());
                markerPosition(destination, destinationAddressStr);

                if (roadpolyline != null) {
                    roadpolyline.remove();
                }
                drawDirectionPolyLine(this.origin, this.destination);

                raidDetails.setText(rideDeatailsStr);
                linearLayout.setVisibility(View.VISIBLE);


            } else {
                //  Log.e("Address " , address + " inside dhaka ");
                Toast.makeText(this, " This Service Only Available Inside Dhaka City", Toast.LENGTH_LONG).show();

                //Moving the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                //Animating the camera
                mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                this.destination = null;
                if (roadpolyline != null) {
                    roadpolyline.remove();
                }
                destinationMaerker.setVisible(false);
                linearLayout.setVisibility(View.GONE);


            }


        } else {
            Toast.makeText(this, " Please Turn On Your Internet Connection ", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onError(Status status) {

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PLACE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                this.onPlaceSelected(place);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                this.onError(status);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    // A class to parse the Google Places in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        String distance = "";
        String duration = "";

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";


            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "Too Short Distance ", Toast.LENGTH_SHORT).show();
                return;
            }


            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {
                        // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;

                    } else if (j == 1) {

                        // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLACK);

            }

            int distanceDouble = (int)Double.parseDouble(distance.toString().replace("km", ""));


            //set Distance and Duration in textview
            raidDetails.setText(" Distance: " + distance + " \n Duration: " + duration);

            fareTextview.setText("Estimate Fare: " + distanceDouble * 17 + "Tk");
            //  fareTextview.setText("  Estimate Fare : "+ Double.parseDouble(distance)*15);


            // Drawing polyline in the Google Map for the i-th route
            roadpolyline = mMap.addPolyline(lineOptions);
        }
    }


}

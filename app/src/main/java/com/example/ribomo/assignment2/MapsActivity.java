package com.example.ribomo.assignment2;

import android.content.DialogInterface;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        SensorEventListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener{
    private GoogleMap mMap;

    SensorManager mSensorManager;
    Sensor sensorType;
    ImageView compassArrowImage;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    HashMap<String, Location> locationsTrack;
    Location mCurrentLocation;
    String mLastUpdateTime;
    private Polyline polyline;
    Button startRecordButton;
    Button stopRecordButton;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Google Api for location Service
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //Sensor Manager for Compass
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sensorType = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        compassArrowImage = (ImageView) findViewById(R.id.compassArrow);
        createLocationRequest();

        startRecordButton = (Button) findViewById(R.id.btnStartRecord);
        stopRecordButton = (Button) findViewById(R.id.btnStopRecord);
        dbHelper = new DBHelper(this);

        locationsTrack = new HashMap<>();
    }

    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, sensorType, SensorManager.SENSOR_DELAY_GAME);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            PolylineOptions polylineOptions = new PolylineOptions();
            polyline = mMap.addPolyline(polylineOptions);
        }
        catch (SecurityException ex){
            Log.e("Map", ex.toString());
        }
    }

    //Calculate Azimuth for compass
    float[] rotMat = new float[9];
    float[] vals = new float[3];
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensorType.getType()){
            SensorManager.getRotationMatrixFromVector(rotMat,
                    event.values);
            SensorManager
                    .remapCoordinateSystem(rotMat,
                            SensorManager.AXIS_X, SensorManager.AXIS_Y,
                            rotMat);
            SensorManager.getOrientation(rotMat, vals);
            compassArrowImage.setRotation((float)radianToDegree(-vals[0]));
//            Log.d("azimuth", Double.toString(radianToDegree(vals[0])));
        }
    }

    public double radianToDegree(float radian){
        double radianDouble = (double) radian;
        return radianDouble*180.0/3.14;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void toggleView(View view){
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title("Your current location!"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }catch (SecurityException ex){
            Log.e("Location", ex.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException ex){
            Log.e("Location", ex.toString());
        }
    }
    protected void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }catch (SecurityException ex){
            Log.e("Location", ex.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        locationsTrack.put(mLastUpdateTime, mCurrentLocation);
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        List<LatLng> points = polyline.getPoints();
        points.add(latLng);
        polyline.setPoints(points);
        Log.d("LocationUpdate", String.valueOf(mLastUpdateTime));
    }

    String staredTime;

    public void startRecord(View view){
        startLocationUpdates();
        isRecordingButtonSwitch();

        staredTime = DateFormat.getTimeInstance().format(new Date());
    }

    public void stopRecord(View view){
        stopLocationUpdates();
        locationsTrack.clear();
        String locationsTrack = "";
        List<LatLng> points = polyline.getPoints();
        for(LatLng location: points){
            locationsTrack += location.latitude + "," + location.longitude + "\n";
        }
        dbHelper.insertData(staredTime, locationsTrack);
        Log.d("saving", locationsTrack);
        polyline.remove();
        PolylineOptions polylineOptions = new PolylineOptions();
        polyline = mMap.addPolyline(polylineOptions);
        isRecordingButtonSwitch();
    }

    public void isRecordingButtonSwitch(){
        if(startRecordButton.isEnabled()){
            startRecordButton.setEnabled(false);
            stopRecordButton.setEnabled(true);
        }else{
            startRecordButton.setEnabled(true);
            stopRecordButton.setEnabled(false);
        }
    }

    public void ListHistoryTrack(View view){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Select a history track to display");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        final Cursor stuff = dbHelper.getData();
        while(stuff.moveToNext()){
            arrayAdapter.add(stuff.getString(0));
        }
        stuff.close();

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Cursor dbStuff = dbHelper.getData();
                dbStuff.moveToPosition(which+1);
                PolylineOptions po = StringToPolyline(dbStuff.getString(1));
                mMap.addPolyline(po);
            }
        });
        builderSingle.show();
    }

    public void clearMap(View view){
        mMap.clear();
        PolylineOptions polylineOptions = new PolylineOptions();
        polyline = mMap.addPolyline(polylineOptions);
    }

    //Convert String stored in database into a Polyline Options class
    public PolylineOptions StringToPolyline(String text){
        String points[] = text.split("\n");
        PolylineOptions result = new PolylineOptions();
        for(String point: points){
            String latlng[] = point.split(",");
            LatLng LatLngPoint = new LatLng(
                    Double.parseDouble(latlng[0])
                    , Double.parseDouble(latlng[1]));
            result.add(LatLngPoint);
            Log.d("getFromDataBase_Lat", latlng[0]);
            Log.d("getFromDataBase_Lng", latlng[1]);
        }
        return result;
    }
}
